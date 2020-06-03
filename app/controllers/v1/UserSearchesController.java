package controllers.v1;

import com.fasterxml.jackson.databind.JsonNode;
import dto.UserSearchCreateDto;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import queryparams.RecipesQueryParams;
import security.SecurityUtils;
import security.VerifiedJwt;
import services.UserSearchService;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class UserSearchesController extends Controller {
    @Inject
    private FormFactory formFactory;

    @Inject
    private HttpExecutionContext httpExecutionContext;

    @Inject
    private UserSearchService userSearchService;

    @Inject
    private MessagesApi messagesApi;

    private Function<Throwable, Result> mapException = new DefaultExceptionMapper(logger);
    private Function<Throwable, Result> mapExceptionWithUnpack = e -> mapException.apply(e.getCause());

    private static final Logger.ALogger logger = Logger.of(UserSearchesController.class);

    public CompletionStage<Result> create(Http.Request request) {
        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);
        Form<UserSearchCreateDto> form = formFactory.form(UserSearchCreateDto.class).bindFromRequest(request);
        if (form.hasErrors()) {
            return completedFuture(badRequest(form.errorsAsJson()));
        }

        UserSearchCreateDto dto = form.get();
        logger.info("create(): dto = {}, userId = {}", dto, jwt.getUserId());
        JsonNode queryJson = Json.toJson(dto.query);
        RecipesQueryParamsRetrieve retriever = new RecipesQueryParamsRetrieve(formFactory, messagesApi, request, queryJson);
        Form<RecipesQueryParams.Params> paramsForm = retriever.retrieve();

        if (paramsForm.hasErrors()) {
            return completedFuture(badRequest(paramsForm.errorsAsJson()));
        }

        return userSearchService.create(dto, jwt.getUserId())
                .thenApplyAsync(id -> {
                    String location = routes.UserSearchesController.single(id).absoluteURL(request);
                    logger.info("create(): location = {}", location);
                    return created().withHeader(LOCATION, location);
                }, httpExecutionContext.current())
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> single(Long id, Http.Request request) {
        // TODO: Should return name and query content
        return null;
    }

    public CompletionStage<Result> update(Long id, Http.Request request) {
        // TODO: Input: name, and query
        return null;
    }

    public CompletionStage<Result> all(Http.Request request) {
        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);
        logger.info("all(): user id = {}", jwt.getUserId());
        return userSearchService.all(jwt.getUserId())
                .thenApplyAsync(l -> ok(Json.toJson(l)), httpExecutionContext.current())
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> delete(Long id, Http.Request request) {
        // TODO
        return null;
    }
}
