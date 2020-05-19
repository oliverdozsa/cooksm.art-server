package controllers.v1;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import lombokized.dto.PageDto;
import lombokized.dto.RecipeDto;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.ValidationError;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import queryparams.RecipesQueryParams;
import scala.util.Either;
import scala.util.Left;
import scala.util.Right;
import services.RecipesService;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.libs.Json.toJson;


public class RecipesController extends Controller {
    @Inject
    private FormFactory formFactory;

    @Inject
    private HttpExecutionContext executionContext;

    @Inject
    private Config config;

    @Inject
    private RecipesService service;

    private Function<Throwable, Result> mapException = t -> {
        logger.error("Internal Error!", t.getCause());

        if (t.getCause() instanceof IllegalArgumentException) {
            return badRequest(Json.toJson(new ValidationError("", t.getMessage()).messages()));
        }

        return internalServerError();
    };

    private static final Logger.ALogger logger = Logger.of(RecipesController.class);

    public CompletionStage<Result> pageRecipes(Http.Request request) {
        Either<JsonNode, RecipesQueryParams.SearchMode> jsonNodeOrSearchMode =
                retrieveSearchMode(request);

        if (jsonNodeOrSearchMode.isLeft()) {
            // Request has error
            logger.warn("pageRecipes(): request has error!");
            JsonNode errorJson = jsonNodeOrSearchMode.left().get();
            return completedFuture(badRequest(errorJson));
        } else {
            RecipesQueryParams.SearchMode searchMode = jsonNodeOrSearchMode.right().get();
            logger.info("pageRecipes(): searchMode = {}", searchMode);
            return refineRequestBy(searchMode, request);
        }
    }

    public CompletionStage<Result> singleRecipe(Long id, Long languageId) {
        return service.single(id, languageId)
                .thenApplyAsync(this::toResult, executionContext.current())
                .exceptionally(mapException);
    }

    private CompletionStage<Result> refineRequestBy(RecipesQueryParams.SearchMode searchMode, Http.Request request) {
        if (searchMode == RecipesQueryParams.SearchMode.COMPOSED_OF_NUMBER) {
            Form<RecipesQueryParams.Params> form = formFactory.form(RecipesQueryParams.Params.class, RecipesQueryParams.VGRecSearchModeComposedOf.class)
                    .bindFromRequest(request);
            return pageOrBadRequest(form, this::getRecipesForQueryTypeNumber);
        } else if (searchMode == RecipesQueryParams.SearchMode.COMPOSED_OF_RATIO) {
            Form<RecipesQueryParams.Params> form = formFactory.form(RecipesQueryParams.Params.class, RecipesQueryParams.VGRecSearchModeComposedOfRatio.class)
                    .bindFromRequest(request);
            return pageOrBadRequest(form, this::getRecipesForQueryTypeRatio);
        } else if (searchMode == RecipesQueryParams.SearchMode.NONE) {
            Form<RecipesQueryParams.Params> form = formFactory.form(RecipesQueryParams.Params.class)
                    .bindFromRequest(request);
            return pageOrBadRequest(form, this::getRecipesForQueryTypeNone);
        } else {
            logger.warn("refineRequestBy(): unknown search mode! searchMode = {}", searchMode);
            ValidationError ve = new ValidationError("", "Unkown search mode!");
            return completedFuture(badRequest(Json.toJson(ve.messages())));
        }
    }

    private CompletionStage<Result> getRecipesForQueryTypeNumber(RecipesQueryParams.Params params) {
        return service.pageOfQueryTypeNumber(params)
                .thenApplyAsync(this::toResult, executionContext.current())
                .exceptionally(mapException);
    }

    private CompletionStage<Result> getRecipesForQueryTypeRatio(RecipesQueryParams.Params params) {
        return service.pageOfQueryTypeRatio(params)
                .thenApplyAsync(this::toResult, executionContext.current())
                .exceptionally(mapException);
    }

    private CompletionStage<Result> getRecipesForQueryTypeNone(RecipesQueryParams.Params params) {
        return service.pageOfQueryTypeNone(params)
                .thenApplyAsync(this::toResult)
                .exceptionally(mapException);
    }

    private Result toResult(PageDto<RecipeDto> pageDto) {
        return ok(toJson(pageDto));
    }

    private Result toResult(RecipeDto dto) {
        if (dto == null) {
            return notFound();
        } else {
            return ok(toJson(dto));
        }
    }

    private <T> CompletionStage<Result> pageOrBadRequest(Form<T> form, Function<T, CompletionStage<Result>> resultProducer) {
        if (form.hasErrors()) {
            logger.warn("pageOrBadRequest(): form has error!");
            return completedFuture(badRequest(form.errorsAsJson()));
        } else {
            T formValue = form.get();
            logger.info("pageOrBadRequest(): formValue = {}", formValue);
            return resultProducer.apply(formValue);
        }
    }

    private Either<JsonNode, RecipesQueryParams.SearchMode> retrieveSearchMode(Http.Request request) {
        // Get form without groups to access search mode.
        Form<RecipesQueryParams.Params> form =
                formFactory.form(RecipesQueryParams.Params.class)
                        .bindFromRequest(request);

        if (form.hasErrors()) {
            return new Left<>(form.errorsAsJson());
        } else {
            String searchModeStr = form.get().searchMode;
            RecipesQueryParams.SearchMode searchMode = RecipesQueryParams.Params.toEnum(searchModeStr);

            return new Right<>(searchMode);
        }
    }
}
