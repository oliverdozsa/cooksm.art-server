package services;

import data.entities.RecipeSearch;
import data.repositories.IngredientNameRepository;
import data.repositories.IngredientTagRepository;
import data.repositories.RecipeSearchRepository;
import data.repositories.SourcePageRepository;
import dto.RecipeSearchDto;
import io.seruco.encoding.base62.Base62;
import lombokized.repositories.RecipeRepositoryParams;
import play.libs.Json;
import queryparams.RecipesQueryParams;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.runAsync;

public class RecipeSearchService {
    @Inject
    private RecipeSearchRepository recipeSearchRepository;

    @Inject
    private IngredientNameRepository ingredientNameRepository;

    @Inject
    private IngredientTagRepository ingredientTagRepository;

    @Inject
    private SourcePageRepository sourcePageRepository;

    private Base62 base62 = Base62.createInstance();

    public CompletionStage<RecipeSearchDto> create(RecipesQueryParams.Params query, boolean isPermanent) {
        RecipeSearchQueryDtoResolver resolver = new RecipeSearchQueryDtoResolver(query);
        resolver.setIngredientNameRepository(ingredientNameRepository);
        resolver.setIngredientTagRepository(ingredientTagRepository);
        resolver.setSourcePageRepository(sourcePageRepository);

        return runAsync(() -> checkQueryParams(query))
                .thenComposeAsync(v -> resolver.resolve())
                .thenComposeAsync(dto -> {
                    String queryStr = Json.toJson(dto).toString();
                    return recipeSearchRepository.create(queryStr, isPermanent);
                })
                .thenApplyAsync(e -> {
                    byte[] encodedId = base62.encode(BigInteger.valueOf(e.getId()).toByteArray());
                    String encodedIdString = new String(encodedId);
                    return new RecipeSearchDto(encodedIdString, resolver.getDto());
                });
    }

    private void checkQueryParams(RecipesQueryParams.Params query) {
        RecipesQueryParams.SearchMode searchMode = RecipesQueryParams.Params.toEnum(query.searchMode);
        if (searchMode == RecipesQueryParams.SearchMode.COMPOSED_OF_NUMBER) {
            RecipeRepositoryParams.QueryTypeNumber queryTypeNumber = RecipesQueryParamsMapping.toQueryTypeNumber(query);
            RecipeRepositoryQueryCheck.check(queryTypeNumber);
        } else if (searchMode == RecipesQueryParams.SearchMode.COMPOSED_OF_RATIO) {
            RecipeRepositoryParams.QueryTypeRatio queryTypeRatio = RecipesQueryParamsMapping.toQueryTypeRatio(query);
            RecipeRepositoryQueryCheck.check(queryTypeRatio);
        }
    }
}
