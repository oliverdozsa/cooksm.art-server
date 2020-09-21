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

public class RecipeResultsByQueryTypeNone extends RecipeResultsByQuery {
    public RecipeResultsByQueryTypeNone(RecipesService service) {
        super(service);
    }

    @Override
    public CompletionStage<Result> page(Form<RecipesQueryParams.Params> form, Http.Request request) {
        RecipesResults.Producers producers = new RecipesResults.Producers();
        producers.unauth = this::getRecipesForQueryTypeNone;
        producers.auth = this::getRecipesForQueryTypeNone;
        Function<RecipesQueryParams.Params, CompletionStage<Result>> resultFunction =
                RecipesResults.determine(form.get(), request, producers);

        return pageOrBadRequest(form, resultFunction);
    }

    private CompletionStage<Result> getRecipesForQueryTypeNone(RecipesQueryParams.Params params) {
        CompletionStage<PageDto<RecipeDto>> pageOfRecipesStage = service.pageOfQueryTypeNone(params);
        return getResultFor(pageOfRecipesStage);
    }

    private CompletionStage<Result> getRecipesForQueryTypeNone(RecipesQueryParams.Params params, Long userId) {
        CompletionStage<PageDto<RecipeDto>> pageOfRecipesStage = service.pageOfQueryTypeNone(params, userId);
        return getResultFor(pageOfRecipesStage);
    }
}
