package services;

import data.repositories.RecipeSearchRepository;
import lombokized.dto.RecipeSearchDto;
import queryparams.RecipesQueryParams;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class RecipeSearchService {
    @Inject
    private RecipeSearchRepository repository;

    public CompletionStage<RecipeSearchDto> create(RecipesQueryParams.Params query) {
        // TODO
        return null;
    }
}
