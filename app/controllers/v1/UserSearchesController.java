package controllers.v1;

import com.fasterxml.jackson.databind.JsonNode;
import dto.UserSearchCreateUpdateDto;
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
    private UserSearchService userSearchService;

    @Inject
    private MessagesApi messagesApi;

    private Function<Throwable, Result> mapException = new DefaultExceptionMapper(logger);
    private Function<Throwable, Result> mapExceptionWithUnpack = e -> mapException.apply(e.getCause());

    private static final Logger.ALogger logger = Logger.of(UserSearchesController.class);

    public CompletionStage<Result> create(Http.Request request) {
        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);
        CompletionStage<Result> errorResult = checkUpdateCreateRequestForErrors(request);
        if (errorResult != null) {
            return errorResult;
        }

        Form<UserSearchCreateUpdateDto> form = formFactory
                .form(UserSearchCreateUpdateDto.class, UserSearchCreateUpdateDto.ValidationGroupForCreate.class)
                .bindFromRequest(request);
        UserSearchCreateUpdateDto dto = form.get();
        logger.info("create(): dto = {}, userId = {}", dto, jwt.getUserId());

        return userSearchService.create(dto, jwt.getUserId())
                .thenApplyAsync(id -> {
                    String location = routes.UserSearchesController.single(id).absoluteURL(request);
                    logger.info("create(): location = {}", location);
                    return created().withHeader(LOCATION, location);
                })
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> single(Long id, Http.Request request) {
        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);
        logger.info("single(): id = {}, userId = {}", id, jwt.getUserId());
        return userSearchService.single(id, jwt.getUserId())
                .thenApplyAsync(dto -> ok(Json.toJson(dto)))
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> patch(Long id, Http.Request request){
        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);
        CompletionStage<Result> errorResult = checkUpdateCreateRequestForErrors(request);
        if (errorResult != null) {
            return errorResult;
        }

        Form<UserSearchCreateUpdateDto> form = formFactory
                .form(UserSearchCreateUpdateDto.class)
                .bindFromRequest(request);

        UserSearchCreateUpdateDto dto = form.get();
        logger.info("patch(): dto = {}, userId = {}, id = {}", dto, jwt.getUserId(), id);
        return userSearchService.patch(id, jwt.getUserId(), dto)
                .thenApplyAsync(success -> (Result) noContent())
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> all(Http.Request request) {
        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);
        logger.info("all(): user id = {}", jwt.getUserId());
        return userSearchService.all(jwt.getUserId())
                .thenApplyAsync(l -> ok(Json.toJson(l)))
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> delete(Long id, Http.Request request) {
        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);
        logger.info("delete(): id = {}, user id = {}", id, jwt.getUserId());
        return userSearchService.delete(id, jwt.getUserId())
                .thenApplyAsync(success -> {
                    if (!success) {
                        return internalServerError("Failed to delete!");
                    }

                    return noContent();
                })
                .exceptionally(mapExceptionWithUnpack);
    }

    private CompletionStage<Result> checkUpdateCreateRequestForErrors(Http.Request request) {
        Form<UserSearchCreateUpdateDto> form = formFactory.form(UserSearchCreateUpdateDto.class).bindFromRequest(request);
        if (form.hasErrors()) {
            return completedFuture(badRequest(form.errorsAsJson()));
        }

        UserSearchCreateUpdateDto dto = form.get();
        JsonNode queryJson = Json.toJson(dto.query);
        RecipesQueryParamsRetrieve retriever = new RecipesQueryParamsRetrieve(formFactory, messagesApi, request, queryJson);
        Form<RecipesQueryParams.Params> paramsForm = retriever.retrieve();

        if (paramsForm.hasErrors()) {
            return completedFuture(badRequest(paramsForm.errorsAsJson()));
        }

        return null;
    }
}
