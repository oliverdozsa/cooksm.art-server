package extractors;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import play.mvc.Result;

import java.util.List;

import static play.test.Helpers.contentAsString;

public class RecipesFromResult {
    public static List<Long> recipeIdsOf(Result result) {
        ListOfValuesFromResult<Long> values = new ListOfValuesFromResult<Long>()
                .select("$.items")
                .converting(n -> n.get("id").asLong());

        return values.of(result);
    }

    public static Long singleRecipeIdOf(Result result) {
        String jsonStr = contentAsString(result);
        JsonNode json = Json.parse(jsonStr);

        return json.get("id").asLong();
    }
}
