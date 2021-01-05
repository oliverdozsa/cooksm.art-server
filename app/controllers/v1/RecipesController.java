package controllers.v1;

import com.typesafe.config.Config;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.ValidationError;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import queryparams.RecipesQueryParams;
import services.RecipesService;
import services.reciperesults.RecipeResultsByQuery;
import services.reciperesults.RecipeResultsByQueryTypeNone;
import services.reciperesults.RecipeResultsByQueryTypeNumber;
import services.reciperesults.RecipeResultsByQueryTypeRatio;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.libs.Json.toJson;


public class RecipesController extends Controller {
    @Inject
    private FormFactory formFactory;

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
        RecipesQueryParamsRetrieve retriever = new RecipesQueryParamsRetrieve(formFactory, request);
        Form<RecipesQueryParams.Params> form = retriever.retrieve();

        if (form.hasErrors()) {
            logger.warn("pageRecipes(): form has error!");
            return completedFuture(badRequest(form.errorsAsJson()));
        } else {
            logger.info("pageRecipes()");
            return refineRequest(form, request);
        }
    }

    public CompletionStage<Result> singleRecipe(Long id, Long languageId) {
        logger.info("singleRecipe(): id = {}, languageId = {}", id, languageId);
        return service.single(id, languageId)
                .thenApplyAsync(dto -> {
                    if (dto == null) {
                        return notFound();
                    } else {
                        return ok(toJson(dto));
                    }
                })
                .exceptionally(mapException);
    }

    private CompletionStage<Result> refineRequest(Form<RecipesQueryParams.Params> form, Http.Request request) {
        String searchModeStr = form.get().searchMode;
        RecipesQueryParams.SearchMode searchMode = RecipesQueryParams.Params.toEnum(searchModeStr);
        RecipeResultsByQuery resultsByQuery;

        if (searchMode == RecipesQueryParams.SearchMode.COMPOSED_OF_NUMBER) {
            resultsByQuery = new RecipeResultsByQueryTypeNumber(service);
        } else if (searchMode == RecipesQueryParams.SearchMode.COMPOSED_OF_RATIO) {
            resultsByQuery = new RecipeResultsByQueryTypeRatio(service);
        } else if (searchMode == RecipesQueryParams.SearchMode.NONE) {
            resultsByQuery = new RecipeResultsByQueryTypeNone(service);
        } else {
            logger.warn("refineRequestBy(): unknown search mode! searchMode = {}", searchMode);
            ValidationError ve = new ValidationError("", "Unkown search mode!");
            return completedFuture(badRequest(Json.toJson(ve.messages())));
        }

        return resultsByQuery.page(form, request);
    }
}
