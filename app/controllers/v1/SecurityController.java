package controllers.v1;

import com.typesafe.config.Config;
import dto.UserCreateUpdateDto;
import dto.UserInfoDto;
import dto.UserSocialLoginDto;
import models.repositories.UserRepository;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import security.JwtValidator;
import security.SecurityUtils;
import security.SocialTokenVerifier;
import security.VerifiedJwt;

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
    private FormFactory formFactory;

    @Inject
    private HttpExecutionContext httpExecutionContext;

    @Inject
    private JwtValidator jwt;

    @Inject
    private Config config;

    private Function<Throwable, Result> defaultExceptionMapper = new DefaultExceptionMapper(logger);
    private Function<Throwable, Result> mapException = t -> {
        if (t instanceof TokenVerificiationException) {
            logger.warn("Unauthorized!", t);
            return unauthorized();
        } else {
            return defaultExceptionMapper.apply(t);
        }
    };
    private Function<Throwable, Result> mapExceptionWithUnpack = e -> mapException.apply(e.getCause());

    private static final Logger.ALogger logger = Logger.of(SecurityController.class);

    public CompletionStage<Result> loginThroughGoogle(Http.Request request) {
        return loginThroughSocial(googleVerifier, request);
    }

    public CompletionStage<Result> loginThroughFacebook(Http.Request request) {
        return loginThroughSocial(facebookVerifier, request);
    }

    public CompletionStage<Result> renew(Http.Request request) {
        VerifiedJwt verifiedJwt = SecurityUtils.getFromRequest(request);

        return supplyAsync(() -> {
            String token = jwt.create(verifiedJwt.getUserId());
            // TODO: get user by id, then add rest of dto data.
            return new Result(NOT_IMPLEMENTED);
        }, httpExecutionContext.current());
    }

    private CompletionStage<Result> loginThroughSocial(SocialTokenVerifier verifier, Http.Request request) {
        Form<UserSocialLoginDto> form = formFactory.form(UserSocialLoginDto.class).bindFromRequest(request);

        if (form.hasErrors()) {
            return completedFuture(badRequest(form.errorsAsJson()));
        }

        UserSocialLoginDto dto = form.get();

        return verify(verifier, dto.getToken())
                .thenCompose(o -> repository.createOrUpdate(convertFrom(dto)))
                .thenApplyAsync(id -> {
                    String token = jwt.create(id);
                    UserInfoDto resultDto = new UserInfoDto(token, dto.getEmail(), dto.getFullName());
                    return ok(Json.toJson(resultDto));
                }, httpExecutionContext.current())
                .exceptionally(mapExceptionWithUnpack);
    }

    private UserCreateUpdateDto convertFrom(UserSocialLoginDto dto) {
        return new UserCreateUpdateDto(dto.getEmail(), dto.getFullName());
    }

    private CompletionStage<Void> verify(SocialTokenVerifier verifier, String token) {
        return verifier.verify(token)
                .thenApplyAsync(isValid -> {
                    if (!isValid) {
                        String msg = String.format("Failed to verify token with %s", verifier.getClass().getCanonicalName());
                        throw new TokenVerificiationException(msg);
                    }
                    return null;
                }, httpExecutionContext.current());
    }

    private static class TokenVerificiationException extends RuntimeException {
        public TokenVerificiationException(String message) {
            super(message);
        }
    }
}
