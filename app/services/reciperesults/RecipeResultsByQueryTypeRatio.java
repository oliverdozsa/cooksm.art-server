package services.reciperesults;

import lombokized.dto.PageDto;
import lombokized.dto.RecipeDto;
import play.data.Form;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import play.mvc.Result;
import queryparams.RecipesQueryParams;
import services.RecipesService;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class RecipeResultsByQueryTypeRatio extends RecipeResultsByQuery {
    public RecipeResultsByQueryTypeRatio(RecipesService service, HttpExecutionContext executionContext) {
        super(service, executionContext);
    }

    @Override
    public CompletionStage<Result> page(Form<RecipesQueryParams.Params> form, Http.Request request) {
        RecipesResults.Producers producers = new RecipesResults.Producers();
        producers.unauth = this::getRecipesForQueryTypeRatio;
        producers.auth = this::getRecipesForQueryTypeRatio;
        Function<RecipesQueryParams.Params, CompletionStage<Result>> resultFunction =
                RecipesResults.determine(form, request, producers);

        return pageOrBadRequest(form, resultFunction);
    }

    private CompletionStage<Result> getRecipesForQueryTypeRatio(RecipesQueryParams.Params params) {
        CompletionStage<PageDto<RecipeDto>> pageOfRecipesStage = service.pageOfQueryTypeRatio(params);
        return getResultFor(pageOfRecipesStage);
    }

    private CompletionStage<Result> getRecipesForQueryTypeRatio(RecipesQueryParams.Params params, Long userId) {
        CompletionStage<PageDto<RecipeDto>> pageOfRecipesStage = service.pageOfQueryTypeRatio(params, userId);
        return getResultFor(pageOfRecipesStage);
    }
}
