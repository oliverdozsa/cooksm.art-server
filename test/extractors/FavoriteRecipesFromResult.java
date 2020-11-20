package extractors;

import com.fasterxml.jackson.databind.JsonNode;
import play.mvc.Result;

import java.util.List;

import static extractors.DataFromResult.toJson;

public class FavoriteRecipesFromResult {
    public static List<Long> recipeIdsOfFavoriteRecipesOf(Result result) {
        ListOfValuesFromResult<Long> values = new ListOfValuesFromResult<Long>()
                .converting(n -> n.get("recipeId").asLong());
        return values.of(result);
    }

    public static Long recipeIdOfSingleFavoriteRecipeOf(Result result) {
        JsonNode json = toJson(result);
        return json.get("recipeId").asLong();
    }
}
