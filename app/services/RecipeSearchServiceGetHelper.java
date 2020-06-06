package services;

import com.fasterxml.jackson.databind.JsonNode;
import data.repositories.IngredientNameRepository;
import data.repositories.IngredientTagRepository;
import data.repositories.RecipeSearchRepository;
import data.repositories.SourcePageRepository;
import data.repositories.exceptions.NotFoundException;
import io.seruco.encoding.base62.Base62;
import lombokized.dto.RecipeSearchDto;
import play.libs.Json;
import queryparams.RecipesQueryParams;

import java.math.BigInteger;
import java.util.concurrent.CompletionStage;

class RecipeSearchServiceGetHelper {
    RecipeSearchRepository recipeSearchRepository;
    IngredientNameRepository ingredientNameRepository;
    IngredientTagRepository ingredientTagRepository;
    SourcePageRepository sourcePageRepository;
    LanguageService languageService;

    public CompletionStage<RecipeSearchDto> single(String id) {
        long decodedId = Base62Conversions.decode(id);
        RecipeSearchQueryDtoResolver resolver = new RecipeSearchQueryDtoResolver();
        resolver.setIngredientNameRepository(ingredientNameRepository);
        resolver.setIngredientTagRepository(ingredientTagRepository);
        resolver.setSourcePageRepository(sourcePageRepository);

        return recipeSearchRepository.single(decodedId)
                .thenComposeAsync(entity -> {
                    if (entity == null) {
                        throw new NotFoundException("RecipeSearch not found. decodedId = " + decodedId);
                    }

                    JsonNode queryJson = Json.parse(entity.getQuery());
                    RecipesQueryParams.Params queryParams = Json.fromJson(queryJson, RecipesQueryParams.Params.class);
                    resolver.setQueryParams(queryParams);
                    resolver.setUsedLanguageId(languageService.getLanguageIdOrDefault(queryParams.languageId));
                    return resolver.resolve();
                })
                .thenApplyAsync(RecipeSearchDto::new);
    }
}
