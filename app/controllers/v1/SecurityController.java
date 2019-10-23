package controllers.v1;

import com.typesafe.config.Config;
import dto.UserSocialLoginDto;
import models.repositories.UserRepository;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import security.SocialTokenVerifier;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.CompletionStage;

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
    private Config config;

    public CompletionStage<Result> loginThroughGoogle(Http.Request request) {
        Form<UserSocialLoginDto> form = formFactory.form(UserSocialLoginDto.class).bindFromRequest(request);

        if (form.hasErrors()) {
            return completedFuture(badRequest(form.errorsAsJson()));
        }

        UserSocialLoginDto dto = form.get();
// TODO: compose: check first with verifier, then create.
//        return repository.doesExist(dto.getEmail())
//                .thenComposeAsync(exists ->
//                }, httpExecutionContext.current());

        return completedFuture(new Result(NOT_IMPLEMENTED));
    }

    public CompletionStage<Result> loginThroughFacebook(Http.Request request) {
        return completedFuture(new Result(NOT_IMPLEMENTED));
    }
}
