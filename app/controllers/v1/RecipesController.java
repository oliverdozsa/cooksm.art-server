package controllers.v1;

import com.typesafe.config.Config;
import dto.PageDto;
import dto.RecipeDto;
import models.DatabaseExecutionContext;
import models.entities.Recipe;
import models.repositories.Page;
import models.repositories.RecipeRepository;
import models.repositories.RecipeRepositoryQueryParams;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.ValidationError;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        // Get form without groups to access search mode.
        Form<RecipesControllerQuery.Params> form =
                formFactory.form(RecipesControllerQuery.Params.class)
                        .bindFromRequest(request);

        if (form.hasErrors()) {
            return completedFuture(badRequest(form.errorsAsJson()));
        } else {
            Integer searchModeValue = form.get().searchMode;
            RecipesControllerQuery.SearchMode searchMode;
            if (searchModeValue == null) {
                searchMode = RecipesControllerQuery.SearchMode.NONE;
            } else {
                searchMode = RecipesControllerQuery.SearchMode.getByIntVal(searchModeValue);
            }

            return refineRequestBy(searchMode, request);
        }
    }

    private CompletionStage<Result> getRecipesByGoodIngredientsNumber(RecipesControllerQuery.Params params) {
        return recipeRepository.pageOfByGoodIngredientsNumber(toGoodIngredeintsNumberQueryParams(params))
                .thenApplyAsync(page -> toResult(page, params.languageId), httpExecutionContext.current())
                .exceptionally(mapException);
    }

    private CompletionStage<Result> getRecipesByGoodIngredientsRatio(RecipesControllerQuery.Params params) {
        return recipeRepository.pageOfByGoodIngredientsRatio(toGoodIngredientsRatioQueryParams(params))
                .thenApplyAsync(page -> toResult(page, params.languageId), httpExecutionContext.current())
                .exceptionally(mapException);
    }

    private CompletionStage<Result> getRecipesAll(RecipesControllerQuery.Params params) {
        return recipeRepository.pageOfAll(toCommonBuilder(params).build())
                .thenApplyAsync(page -> toResult(page, params.languageId), httpExecutionContext.current())
                .exceptionally(mapException);
    }


    private RecipeRepositoryQueryParams.OfGoodIngredientsNumber toGoodIngredeintsNumberQueryParams(RecipesControllerQuery.Params params) {
        RecipeRepositoryQueryParams.Common.CommonBuilder commonBuilder =
                toCommonBuilder(params);

        RecipeRepositoryQueryParams.WithIncludedIngredients.WithIncludedIngredientsBuilder
                withIncludedIngredientsBuilder = toWithIncludedIngredientsBuilder(params);

        RecipeRepositoryQueryParams.OfGoodIngredientsNumber.OfGoodIngredientsNumberBuilder ofGoodIngredientsNumberBuilder =
                RecipeRepositoryQueryParams.OfGoodIngredientsNumber.builder();

        ofGoodIngredientsNumberBuilder.common(commonBuilder.build());
        ofGoodIngredientsNumberBuilder.recipesWithIncludedIngredients(withIncludedIngredientsBuilder.build());
        ofGoodIngredientsNumberBuilder.goodIngredients(params.goodIngs);
        ofGoodIngredientsNumberBuilder.goodIngredientsRelation(RecipeRepositoryQueryParams.Relation.fromString(params.goodIngsRel));
        ofGoodIngredientsNumberBuilder.unknownIngredients(params.unknownIngs);
        ofGoodIngredientsNumberBuilder.unknownIngredientsRelation(RecipeRepositoryQueryParams.Relation.fromString(params.unknownIngsRel));

        return ofGoodIngredientsNumberBuilder.build();
    }

    private RecipeRepositoryQueryParams.OfGoodIngredientsRatio toGoodIngredientsRatioQueryParams(RecipesControllerQuery.Params params) {
        RecipeRepositoryQueryParams.Common.CommonBuilder commonBuilder =
                toCommonBuilder(params);

        RecipeRepositoryQueryParams.WithIncludedIngredients.WithIncludedIngredientsBuilder
                withIncludedIngredientsBuilder = toWithIncludedIngredientsBuilder(params);

        RecipeRepositoryQueryParams.OfGoodIngredientsRatio.OfGoodIngredientsRatioBuilder ofGoodIngredientsRatioBuilder =
                RecipeRepositoryQueryParams.OfGoodIngredientsRatio.builder();

        ofGoodIngredientsRatioBuilder.common(commonBuilder.build());
        ofGoodIngredientsRatioBuilder.goodIngredientsRatio(params.goodIngsRatio);
        ofGoodIngredientsRatioBuilder.recipesWithIncludedIngredients(withIncludedIngredientsBuilder.build());

        return ofGoodIngredientsRatioBuilder.build();
    }

    // Converts controller query params to repository query params builder.
    private RecipeRepositoryQueryParams.Common.CommonBuilder toCommonBuilder(RecipesControllerQuery.Params params) {
        RecipeRepositoryQueryParams.Common.CommonBuilder builder = RecipeRepositoryQueryParams.Common.builder();

        if (params.exIngs != null && params.exIngs.size() > 0) {
            builder.excludedIngredients(params.exIngs);
        }

        if (params.exIngTags != null && params.exIngTags.size() > 0) {
            builder.excludedIngredientTags(params.exIngTags);
        }

        builder.limit(params.limit);
        builder.offset(params.offset);
        builder.maximumNumberOfIngredients(params.maxIngs);
        builder.minimumNumberOfIngredients(params.minIngs);
        builder.nameLike(params.nameLike);
        builder.orderBy(params.orderBy);
        builder.orderBySort(params.orderBySort);
        builder.sourcePageIds(params.sourcePages);

        return builder;
    }

    private RecipeRepositoryQueryParams.WithIncludedIngredients.WithIncludedIngredientsBuilder
    toWithIncludedIngredientsBuilder(RecipesControllerQuery.Params params) {
        RecipeRepositoryQueryParams.WithIncludedIngredients.WithIncludedIngredientsBuilder
                builder = RecipeRepositoryQueryParams.WithIncludedIngredients.builder();

        if (params.inIngs != null && params.inIngs.size() > 0) {
            builder.includedIngredients(params.inIngs);
        }

        if (params.inIngTags != null && params.inIngTags.size() > 0) {
            builder.includedIngredientTags(params.inIngTags);
        }

        return builder;
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

    private Long getDefaultLanguageId(){
        return config.getLong("openrecipes.default.languageid");
    }
}
