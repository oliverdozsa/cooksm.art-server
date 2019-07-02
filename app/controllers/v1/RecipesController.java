package controllers.v1;

import dto.PageDto;
import dto.RecipeDto;
import models.DatabaseExecutionContext;
import models.repositories.RecipeRepository;
import models.repositories.RecipeRepositoryQueryParams;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;
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

    private static final Logger.ALogger logger = Logger.of(RecipesController.class);

    public CompletionStage<Result> pageRecipes(Http.Request request) {
        Form<RecipesControllerQuery.Params> form =
                formFactory.form(RecipesControllerQuery.Params.class, RecipesControllerQuery.VGRecSearchModeComposedOf.class)
                        .bindFromRequest(request);

        if (form.hasErrors()) {
            return completedFuture(badRequest(form.errorsAsJson()));
        } else {
            RecipesControllerQuery.Params params = form.get();

            if(RecipesControllerQuery.SearchMode.getByIntVal(params.searchMode) == RecipesControllerQuery.SearchMode.COMPOSED_OF){
                return getRecipesByGoogIngredients(params);
            } else {
                return completedFuture(badRequest());
            }
        }
    }

    private CompletionStage<Result> getRecipesByGoogIngredients(RecipesControllerQuery.Params params){
        return recipeRepository.pageOfComposedOfIngredients(toRepositoryQueryParamsByGoodIngredientsNumber(params))
                .thenApplyAsync(page -> {
                    List<RecipeDto> dtos = page.getItems()
                            .stream()
                            .map(entity -> DtoMapper.toDto(entity, params.languageId))
                            .collect(Collectors.toList());

                    return ok(toJson(new PageDto<>(dtos, page.getTotalCount())));
                }, httpExecutionContext.current());
    }

    // Converts controller query params to repository query params builder.
    private RecipeRepositoryQueryParams.ByGoodIngredientsNumber.ByGoodIngredientsNumberBuilder toRepositoryQueryParamsBuilder(RecipesControllerQuery.Params params) {
        RecipeRepositoryQueryParams.ByGoodIngredientsNumber.ByGoodIngredientsNumberBuilder builder =
                RecipeRepositoryQueryParams.ByGoodIngredientsNumber.builder();

        if (params.inIngs != null && params.inIngs.size() > 0) {
            builder.includedIngredients(params.inIngs);
        }

        if (params.exIngs != null && params.exIngs.size() > 0) {
            builder.excludedIngredients(params.exIngs);
        }

        if (params.inIngTags != null && params.inIngTags.size() > 0) {
            builder.includedIngredientTags(params.inIngTags);
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


    // Finalizes the repository query params builder for the specific query.
    private RecipeRepositoryQueryParams.ByGoodIngredientsNumber toRepositoryQueryParamsByGoodIngredientsNumber(RecipesControllerQuery.Params params){
        RecipeRepositoryQueryParams.ByGoodIngredientsNumber.ByGoodIngredientsNumberBuilder builder =
                toRepositoryQueryParamsBuilder(params);

        builder.goodIngredients(params.goodIngs);
        builder.goodIngredientsRelation(RecipeRepositoryQueryParams.Relation.fromString(params.goodIngsRel));
        builder.unknownIngredients(params.unknownIngs);
        builder.unknownIngredientRelation(RecipeRepositoryQueryParams.Relation.fromString(params.unknownIngsRel));

        return builder.build();
    }
}
