package extractors;

import com.fasterxml.jackson.databind.JsonNode;
import play.mvc.Result;

import java.time.Instant;

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
}
