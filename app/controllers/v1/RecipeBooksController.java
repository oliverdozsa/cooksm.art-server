package controllers.v1;

import dto.RecipeBookCreateUpdateDto;
import dto.RecipeBookRecipesCreateUpdateRemoveDto;
import lombokized.dto.RecipeBookDto;
import lombokized.dto.RecipeBookWithRecipesDto;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import security.SecurityUtils;
import security.VerifiedJwt;
import services.RecipeBooksService;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class RecipeBooksController extends Controller {
    @Inject
    private FormFactory formFactory;

    @Inject
    private RecipeBooksService service;

    private Function<Throwable, Result> mapException = new DefaultExceptionMapper(logger);
    private Function<Throwable, Result> mapExceptionWithUnpack = e -> mapException.apply(e.getCause());

    private static final Logger.ALogger logger = Logger.of(RecipeBooksController.class);

    public CompletionStage<Result> create(Http.Request request) {
        logger.info("create()");

        Form<RecipeBookCreateUpdateDto> form =
                formFactory.form(RecipeBookCreateUpdateDto.class).bindFromRequest(request);

        if (form.hasErrors()) {
            return completedFuture(badRequest(form.errorsAsJson()));
        }

        RecipeBookCreateUpdateDto dto = form.get();
        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);

        return service.create(dto, jwt.getUserId())
                .thenApplyAsync(id -> {
                    String location = routes.RecipeBooksController.single(id).absoluteURL(request);
                    logger.info("create(): created recipe book with id: {}", id);
                    return created().withHeader(LOCATION, location);
                }).exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> all(Http.Request request) {
        logger.info("all");

        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);

        return service.all(jwt.getUserId())
                .thenApplyAsync(this::toResult)
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> single(Long id, Http.Request request) {
        logger.info("single(): id = {}", id);

        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);
        return service.single(jwt.getUserId(), id)
                .thenApplyAsync(this::toResult)
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> update(Long id, Http.Request request) {
        logger.info("update(): id = {}", id);

        Form<RecipeBookCreateUpdateDto> form = formFactory.form(RecipeBookCreateUpdateDto.class)
                .bindFromRequest(request);

        if (form.hasErrors()) {
            return completedFuture(badRequest(form.errorsAsJson()));
        }

        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);

        return service.update(jwt.getUserId(), id, form.get())
                .thenApplyAsync(v -> (Result) noContent())
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> delete(Long id, Http.Request request) {
        logger.info("delete(): id = {}", id);

        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);

        return service.delete(jwt.getUserId(), id)
                .thenApplyAsync(v -> (Result) noContent())
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> addRecipes(Long id, Http.Request request) {
        logger.info("addRecipes(): id = {}", id);

        Form<RecipeBookRecipesCreateUpdateRemoveDto> form = formFactory.form(RecipeBookRecipesCreateUpdateRemoveDto.class)
                .bindFromRequest(request);
        if (form.hasErrors()) {
            return completedFuture(badRequest(form.errorsAsJson()));
        }

        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);

        return service.addRecipes(jwt.getUserId(), id, form.get())
                .thenApplyAsync(v -> (Result) noContent())
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> removeRecipes(Long id, Http.Request request) {
        logger.info("removeRecipes(): id = {}", id);

        Form<RecipeBookRecipesCreateUpdateRemoveDto> form = formFactory.form(RecipeBookRecipesCreateUpdateRemoveDto.class)
                .bindFromRequest(request);
        if (form.hasErrors()) {
            return completedFuture(badRequest(form.errorsAsJson()));
        }

        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);

        return service.removeRecipes(jwt.getUserId(), id, form.get())
                .thenApply(v -> (Result) noContent())
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> recipesOf(Long id, Http.Request request) {
        logger.info("recipesOf(): id = {}", id);

        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);

        return service.recipesOf(jwt.getUserId(), id)
                .thenApplyAsync(this::toResult)
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> updateRecipes(Long id, Http.Request request) {
        logger.info("updateRecipes(): id = {}", id);

        Form<RecipeBookRecipesCreateUpdateRemoveDto> form = formFactory.form(RecipeBookRecipesCreateUpdateRemoveDto.class)
                .bindFromRequest(request);
        if (form.hasErrors()) {
            return completedFuture(badRequest(form.errorsAsJson()));
        }

        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);

        return service.updateRecipes(jwt.getUserId(), id, form.get())
                .thenApplyAsync(v -> (Result) noContent())
                .exceptionally(mapExceptionWithUnpack);
    }

    private Result toResult(List<RecipeBookDto> dtoList) {
        return ok(Json.toJson(dtoList));
    }

    private Result toResult(RecipeBookDto dto) {
        return ok(Json.toJson(dto));
    }

    private Result toResult(RecipeBookWithRecipesDto dto) {
        return ok(Json.toJson(dto));
    }
}
