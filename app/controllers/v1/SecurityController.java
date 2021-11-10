package controllers.v1;

import com.typesafe.config.Config;
import data.DatabaseExecutionContext;
import data.entities.User;
import data.repositories.UserRepository;
import lombokized.dto.UserCreateUpdateDto;
import lombokized.dto.UserInfoDto;
import lombokized.dto.UserSocialLoginDto;
import lombokized.security.VerifiedFacebookUserInfo;
import lombokized.security.VerifiedGoogleUserInfo;
import lombokized.security.VerifiedUserInfo;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import security.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.supplyAsync;

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
    @Named("Dev")
    private SocialTokenVerifier devVerifier;

    @Inject
    private FormFactory formFactory;

    @Inject
    private JwtCenter jwtCenter;

    @Inject
    private Config config;

    @Inject
    private DatabaseExecutionContext dbExecContext;

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

    public CompletionStage<Result> loginThroughDev(Http.Request request) {
        logger.info("loginThroughDev()");
        return loginThroughSocial(devVerifier, request);
    }

    public CompletionStage<Result> renew(Http.Request request) {
        VerifiedJwt verifiedJwt = SecurityUtils.getFromRequest(request);
        logger.info("renew(): user id = {}", verifiedJwt.getUserId());
        return supplyAsync(() -> {
            User user = repository.byId(verifiedJwt.getUserId());

            String token = jwtCenter.create(user.getId());
            UserInfoDto resultDto = new UserInfoDto(token, user.getEmail(), user.getFullName());
            return ok(Json.toJson(resultDto));
        }, dbExecContext)
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> deregister(Http.Request request) {
        VerifiedJwt verifiedJwt = SecurityUtils.getFromRequest(request);
        Long userId = verifiedJwt.getUserId();
        logger.info("deregister(): user id = {}", userId);

        return supplyAsync(() -> {
            repository.delete(userId);
            return (Result) noContent();
        }, dbExecContext)
                .exceptionally(mapExceptionWithUnpack);
    }

    private CompletionStage<Result> loginThroughSocial(SocialTokenVerifier verifier, Http.Request request) {
        Form<UserSocialLoginDto> form = formFactory.form(UserSocialLoginDto.class).bindFromRequest(request);

        if (form.hasErrors()) {
            logger.warn("loginThroughSocial(): form has errors! errors = {}", form.errorsAsJson().toPrettyString());
            return completedFuture(badRequest(form.errorsAsJson()));
        }

        UserSocialLoginDto dto = form.get();
        logger.info("loginThroughSocial()");
        return verifier.verify(dto.getToken())
                .thenCompose(this::toResult)
                .exceptionally(mapExceptionWithUnpack);
    }

    private CompletionStage<Result> toResult(VerifiedUserInfo verifiedUserInfo) {
        return supplyAsync(() -> {
            User user = repository.createOrUpdate(convertFrom(verifiedUserInfo));

            String token = jwtCenter.create(user.getId());
            UserInfoDto result = new UserInfoDto(token, verifiedUserInfo.getEmail(), verifiedUserInfo.getFullName());
            return ok(Json.toJson(result));
        }, dbExecContext);
    }

    private UserCreateUpdateDto convertFrom(VerifiedUserInfo info) {
        if (info instanceof VerifiedGoogleUserInfo) {
            String socialId = info.getSocialId();
            return new UserCreateUpdateDto(info.getEmail(), info.getFullName(), socialId, null);
        }

        if (info instanceof VerifiedFacebookUserInfo) {
            String socialId = info.getSocialId();
            return new UserCreateUpdateDto(info.getEmail(), info.getFullName(), null, socialId);
        }

        return null;
    }
}
