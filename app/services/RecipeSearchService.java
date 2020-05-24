package services;

import com.fasterxml.jackson.databind.JsonNode;
import data.repositories.IngredientNameRepository;
import data.repositories.IngredientTagRepository;
import data.repositories.RecipeSearchRepository;
import data.repositories.SourcePageRepository;
import data.repositories.exceptions.NotFoundException;
import io.seruco.encoding.base62.Base62;
import lombokized.dto.RecipeSearchDto;
import lombokized.repositories.RecipeRepositoryParams;
import play.Logger;
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

    private static final Logger.ALogger logger = Logger.of(RecipeSearchService.class);

    public CompletionStage<RecipeSearchDto> get(String id) {
        byte[] idBytes = base62.decode(id.getBytes());
        long decodedId = new BigInteger(idBytes).longValue();
        RecipeSearchQueryDtoResolver resolver = new RecipeSearchQueryDtoResolver();
        resolver.setIngredientNameRepository(ingredientNameRepository);
        resolver.setIngredientTagRepository(ingredientTagRepository);
        resolver.setSourcePageRepository(sourcePageRepository);

        return recipeSearchRepository.read(decodedId)
                .thenComposeAsync(entity -> {
                    if (entity == null) {
                        throw new NotFoundException("RecipeSearch not found. decodedId = " + decodedId);
                    }

                    JsonNode queryJson = Json.parse(entity.getQuery());
                    RecipesQueryParams.Params queryParams = Json.fromJson(queryJson, RecipesQueryParams.Params.class);
                    resolver.setQueryParams(queryParams);
                    return resolver.resolve();
                })
                .thenApplyAsync(RecipeSearchDto::new);
    }

    public CompletionStage<String> create(RecipesQueryParams.Params query, boolean isPermanent) {
        query.offset = null;
        query.limit = null;

        return runAsync(() -> checkQueryParams(query))
                .thenComposeAsync(dto -> {
                    String queryStr = Json.toJson(query).toString();
                    return recipeSearchRepository.create(queryStr, isPermanent);
                })
                .thenApplyAsync(id -> {
                    byte[] encodedId = base62.encode(BigInteger.valueOf(id).toByteArray());
                    return new String(encodedId);
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
