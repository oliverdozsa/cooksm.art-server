package services;

import data.repositories.RecipeSearchRepository;
import dto.RecipeSearchDto;
import lombokized.repositories.RecipeRepositoryParams;
import queryparams.RecipesQueryParams;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class RecipeSearchService {
    @Inject
    private RecipeSearchRepository repository;

    public CompletionStage<RecipeSearchDto> create(RecipesQueryParams.Params query) {
        return supplyAsync(() -> {
            // TODO
            return null;
        });
    }

    private void checkQueryParams(RecipesQueryParams.Params query) {
        RecipesQueryParams.SearchMode searchMode = RecipesQueryParams.Params.toEnum(query.searchMode);
        if (searchMode == RecipesQueryParams.SearchMode.COMPOSED_OF_NUMBER) {
            RecipeRepositoryParams.QueryTypeNumber queryTypeNumber = RecipesQueryParamsMapping.toQueryTypeNumber(query);
            RecipeRepositoryQueryCheck.check(queryTypeNumber);
        } else if (searchMode == RecipesQueryParams.SearchMode.COMPOSED_OF_RATIO){
            RecipeRepositoryParams.QueryTypeRatio queryTypeRatio = RecipesQueryParamsMapping.toQueryTypeRatio(query);
            RecipeRepositoryQueryCheck.check(queryTypeRatio);
        }
    }
}
