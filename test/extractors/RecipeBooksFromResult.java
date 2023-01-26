package extractors;

import com.fasterxml.jackson.databind.JsonNode;
import lombokized.dto.RecipeInRecipeBookSummaryDto;
import play.mvc.Result;

import java.time.Instant;
import java.util.List;

import static extractors.DataFromResult.toJson;

public class RecipeBooksFromResult {
    public static String recipeBookNameOf(Result result) {
        JsonNode json = toJson(result);
        return json.get("name").asText();
    }

    public static Instant lastAccessedDateOfRecipeBookOf(Result result) {
        JsonNode json = toJson(result);
        return Instant.parse(json.get("lastAccessed").asText());
    }

    public static List<String> recipeBookNamesOf(Result result) {
        ListOfValuesFromResult<String> values = new ListOfValuesFromResult<String>()
                .converting(n -> n.get("name").asText());

        return values.of(result);
    }

    public static List<Long> recipeIdsOfRecipeBookOf(Result result) {
        ListOfValuesFromResult<Long> values = new ListOfValuesFromResult<Long>()
                .select("$.recipeSummaries")
                .converting(j -> j.get("id").asLong());

        return values.of(result);
    }

    public static List<Long> recipeBookIdsOf(Result result) {
        ListOfValuesFromResult<Long> values = new ListOfValuesFromResult<Long>()
                .select("$.recipeBookIds")
                .converting(JsonNode::asLong);

        return values.of(result);
    }
}
