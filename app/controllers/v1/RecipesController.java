package controllers.v1;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import dto.PageDto;
import dto.RecipeDto;
import models.DatabaseExecutionContext;
import models.entities.Recipe;
import models.repositories.Page;
import models.repositories.RecipeRepository;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.ValidationError;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import scala.util.Either;
import scala.util.Left;
import scala.util.Right;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;

import static controllers.v1.RecipeControllerQueryMapping.*;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.libs.Json.toJson;

public class RecipesController extends Controller {
    @Inject
    private RecipeRepository recipeRepository;

    @Inject
    private FormFactory formFactory;

    @Inject
    private HttpExecutionContext httpExecutionContext;

    @Inject
    private DatabaseExecutionContext dbExecutionContext;

    @Inject
    private Config config;

    private Function<Throwable, Result> mapException = t -> {
        logger.error("Internal Error!", t.getCause());

        if (t.getCause() instanceof IllegalArgumentException) {
            return badRequest(Json.toJson(new ValidationError("", t.getMessage()).messages()));
        }

        return internalServerError();
    };

    private static final Logger.ALogger logger = Logger.of(RecipesController.class);

    public CompletionStage<Result> pageRecipes(Http.Request request) {
        Either<JsonNode, RecipesControllerQuery.SearchMode> jsonNodeOrSearchMode =
                retreiveSearchMode(request);

        if (jsonNodeOrSearchMode.isLeft()) {
            // Request has error
            JsonNode errorJson = jsonNodeOrSearchMode.left().get();
            return completedFuture(badRequest(errorJson));
        } else {
            RecipesControllerQuery.SearchMode searchMode = jsonNodeOrSearchMode.right().get();
            return refineRequestBy(searchMode, request);
        }
    }

    private CompletionStage<Result> getRecipesByGoodIngredientsNumber(RecipesControllerQuery.Params params) {
        return recipeRepository.pageOfByGoodIngredientsNumber(toGoodIngredientsNumberParams(params))
                .thenApplyAsync(page -> toResult(page, params.languageId), httpExecutionContext.current())
                .exceptionally(mapException);
    }

    private CompletionStage<Result> getRecipesByGoodIngredientsRatio(RecipesControllerQuery.Params params) {
        return recipeRepository.pageOfByGoodIngredientsRatio(toGoodIngredientsRatioParams(params))
                .thenApplyAsync(page -> toResult(page, params.languageId), httpExecutionContext.current())
                .exceptionally(mapException);
    }

    private CompletionStage<Result> getRecipesAll(RecipesControllerQuery.Params params) {
        return recipeRepository.pageOfAll(toCommonParams(params))
                .thenApplyAsync(page -> toResult(page, params.languageId), httpExecutionContext.current())
                .exceptionally(mapException);
    }



    private Result toResult(Page<Recipe> page, Long languageId) {
        Long usedLanguageId = languageId == null ? getDefaultLanguageId() : languageId;
        List<RecipeDto> dtos = page.getItems()
                .stream()
                .map(entity -> DtoMapper.toDto(entity, usedLanguageId))
                .collect(Collectors.toList());

        return ok(toJson(new PageDto<>(dtos, page.getTotalCount())));
    }

    private CompletionStage<Result> refineRequestBy(RecipesControllerQuery.SearchMode searchMode, Http.Request request) {
        if (searchMode == RecipesControllerQuery.SearchMode.COMPOSED_OF) {
            Form<RecipesControllerQuery.Params> params = formFactory.form(RecipesControllerQuery.Params.class, RecipesControllerQuery.VGRecSearchModeComposedOf.class)
                    .bindFromRequest(request);
            return pageOrBadRequest(params, this::getRecipesByGoodIngredientsNumber);
        } else if (searchMode == RecipesControllerQuery.SearchMode.COMPOSED_OF_RATIO) {
            Form<RecipesControllerQuery.Params> params = formFactory.form(RecipesControllerQuery.Params.class, RecipesControllerQuery.VGRecSearchModeComposedOfRatio.class)
                    .bindFromRequest(request);
            return pageOrBadRequest(params, this::getRecipesByGoodIngredientsRatio);
        } else if (searchMode == RecipesControllerQuery.SearchMode.NONE) {
            Form<RecipesControllerQuery.Params> params = formFactory.form(RecipesControllerQuery.Params.class)
                    .bindFromRequest(request);
            return pageOrBadRequest(params, this::getRecipesAll);
        } else {
            return completedFuture(badRequest());
        }
    }

    private <T> CompletionStage<Result> pageOrBadRequest(Form<T> form, Function<T, CompletionStage<Result>> resultProducer) {
        if (form.hasErrors()) {
            return completedFuture(badRequest(form.errorsAsJson()));
        } else {
            T formValue = form.get();
            return resultProducer.apply(formValue);
        }
    }

    private Long getDefaultLanguageId() {
        return config.getLong("openrecipes.default.languageid");
    }

    private Either<JsonNode, RecipesControllerQuery.SearchMode> retreiveSearchMode(Http.Request request) {
        // Get form without groups to access search mode.
        Form<RecipesControllerQuery.Params> form =
                formFactory.form(RecipesControllerQuery.Params.class)
                        .bindFromRequest(request);

        if (form.hasErrors()) {
            return new Left<>(form.errorsAsJson());
        } else {
            Integer searchModeValue = form.get().searchMode;
            RecipesControllerQuery.SearchMode searchMode;
            if (searchModeValue == null) {
                searchMode = RecipesControllerQuery.SearchMode.NONE;
            } else {
                searchMode = RecipesControllerQuery.SearchMode.getByIntVal(searchModeValue);
            }

            return new Right<>(searchMode);
        }

    }
}
