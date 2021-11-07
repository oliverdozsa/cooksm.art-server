package services;

import com.fasterxml.jackson.databind.JsonNode;
import data.entities.RecipeSearch;
import data.repositories.*;
import data.repositories.exceptions.NotFoundException;
import io.seruco.encoding.base62.Base62;
import lombokized.dto.RecipeSearchDto;
import lombokized.dto.RecipeSearchQueryDto;
import play.libs.Json;
import queryparams.RecipesQueryParams;

import java.math.BigInteger;
import java.util.concurrent.CompletionStage;

class RecipeSearchServiceGetHelper {
    RecipeSearchRepository recipeSearchRepository;
    IngredientNameRepository ingredientNameRepository;
    IngredientTagRepository ingredientTagRepository;
    SourcePageRepository sourcePageRepository;
    RecipeBookRepository recipeBookRepository;
    LanguageService languageService;

    public RecipeSearchDto single(String id) {
        long decodedId = Base62Conversions.decode(id);
        RecipeSearchQueryDtoResolver resolver = new RecipeSearchQueryDtoResolver();
        resolver.setIngredientNameRepository(ingredientNameRepository);
        resolver.setIngredientTagRepository(ingredientTagRepository);
        resolver.setSourcePageRepository(sourcePageRepository);
        resolver.setRecipeBookRepository(recipeBookRepository);

        RecipeSearch recipeSearch = recipeSearchRepository.single(decodedId);

        if (recipeSearch == null) {
            throw new NotFoundException("RecipeSearch not found. decodedId = " + decodedId);
        }

        JsonNode queryJson = Json.parse(recipeSearch.getQuery());
        RecipesQueryParams.Params queryParams = Json.fromJson(queryJson, RecipesQueryParams.Params.class);
        resolver.setQueryParams(queryParams);
        resolver.setUsedLanguageId(languageService.getLanguageIdOrDefault(queryParams.languageId));
        RecipeSearchQueryDto searchQueryDto = resolver.resolve();

        return new RecipeSearchDto(searchQueryDto);
    }
}
