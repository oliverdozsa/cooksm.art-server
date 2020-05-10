package controllers.v1;

import com.typesafe.config.Config;
import lombokized.dto.UserCreateUpdateDto;
import lombokized.dto.UserInfoDto;
import lombokized.dto.UserSocialLoginDto;
import lombokized.security.VerifiedFacebookUserInfo;
import lombokized.security.VerifiedGoogleUserInfo;
import lombokized.security.VerifiedUserInfo;
import models.repositories.UserRepository;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import security.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class SecurityController extends Controller {
    @Inject
    private UserRepository repository;

    @Inject
    @Named("Google")
    private SocialTokenVerifier googleVerifier;

    @Inject
    @Named("Facebook")
    private SocialTokenVerifier facebookVerifier;

    @Inject
    private FormFactory formFactory;

    @Inject
    private HttpExecutionContext httpExecutionContext;

    @Inject
    private JwtCenter jwtCenter;

    @Inject
    private Config config;

    private Function<Throwable, Result> defaultExceptionMapper = new DefaultExceptionMapper(logger);
    private Function<Throwable, Result> mapException = t -> {
        if (t instanceof TokenVerificationException) {
            logger.warn("Unauthorized!", t);
            return unauthorized();
        } else {
            return defaultExceptionMapper.apply(t);
        }
    };
    private Function<Throwable, Result> mapExceptionWithUnpack = e -> mapException.apply(e.getCause());

    private static final Logger.ALogger logger = Logger.of(SecurityController.class);

    public CompletionStage<Result> loginThroughGoogle(Http.Request request) {
        logger.info("loginThroughGoogle()");
        return loginThroughSocial(googleVerifier, request);
    }

    public CompletionStage<Result> loginThroughFacebook(Http.Request request) {
        logger.info("loginThroughGoogle()");
        return loginThroughSocial(facebookVerifier, request);
    }

    public CompletionStage<Result> renew(Http.Request request) {
        VerifiedJwt verifiedJwt = SecurityUtils.getFromRequest(request);
        logger.info("renew(): user id = {}", verifiedJwt.getUserId());
        return repository.byId(verifiedJwt.getUserId())
                .thenApplyAsync(user -> {
                    String token = jwtCenter.create(user.getId());
                    UserInfoDto resultDto = new UserInfoDto(token, user.getEmail(), user.getFullName());
                    return ok(Json.toJson(resultDto));
                }, httpExecutionContext.current())
                .exceptionally(mapExceptionWithUnpack);
    }

    private CompletionStage<Result> loginThroughSocial(SocialTokenVerifier verifier, Http.Request request) {
        Form<UserSocialLoginDto> form = formFactory.form(UserSocialLoginDto.class).bindFromRequest(request);

        if (form.hasErrors()) {
            logger.warn("loginThroughSocial(): form has errors!");
            return completedFuture(badRequest(form.errorsAsJson()));
        }

        UserSocialLoginDto dto = form.get();
        logger.info("loginThroughSocial(): token = " + verifier.verify(dto.getToken()));
        return verifier.verify(dto.getToken())
                .thenCompose(this::toResult)
                .exceptionally(mapExceptionWithUnpack);
    }

    private CompletionStage<Result> toResult(VerifiedUserInfo verifiedUserInfo) {
        return repository.createOrUpdate(convertFrom(verifiedUserInfo))
                .thenApplyAsync(id -> {
                    String token = jwtCenter.create(id);
                    UserInfoDto result = new UserInfoDto(token, verifiedUserInfo.getEmail(), verifiedUserInfo.getFullName());
                    return ok(Json.toJson(result));
                }, httpExecutionContext.current());
    }

    private UserCreateUpdateDto convertFrom(VerifiedUserInfo info) {
        if (info instanceof VerifiedGoogleUserInfo) {
            String socialId = ((VerifiedGoogleUserInfo) info).getSocialId();
            return new UserCreateUpdateDto(info.getEmail(), info.getFullName(), socialId, null);
        }

        if (info instanceof VerifiedFacebookUserInfo) {
            String socialId = ((VerifiedFacebookUserInfo) info).getSocialId();
            return new UserCreateUpdateDto(info.getEmail(), info.getFullName(), null, socialId);
        }

        return null;
    }
}
