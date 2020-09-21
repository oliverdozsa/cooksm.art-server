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

public class RecipeResultsByQueryTypeNumber extends RecipeResultsByQuery {
    public RecipeResultsByQueryTypeNumber(RecipesService service) {
        super(service);
    }

    @Override
    public CompletionStage<Result> page(Form<RecipesQueryParams.Params> form, Http.Request request) {
        RecipesResults.Producers producers = new RecipesResults.Producers();
        producers.unauth = this::getRecipesForQueryTypeNumber;
        producers.auth = this::getRecipesForQueryTypeNumber;
        Function<RecipesQueryParams.Params, CompletionStage<Result>> resultFunction =
                RecipesResults.determine(form.get(), request, producers);

        return pageOrBadRequest(form, resultFunction);
    }

    private CompletionStage<Result> getRecipesForQueryTypeNumber(RecipesQueryParams.Params params) {
        CompletionStage<PageDto<RecipeDto>> pageOfRecipesStage = service.pageOfQueryTypeNumber(params);
        return getResultFor(pageOfRecipesStage);
    }

    private CompletionStage<Result> getRecipesForQueryTypeNumber(RecipesQueryParams.Params params, Long userId) {
        CompletionStage<PageDto<RecipeDto>> pageOfRecipesStage = service.pageOfQueryTypeNumber(params, userId);
        return getResultFor(pageOfRecipesStage);
    }
}
