package extractors;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import play.mvc.Result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static extractors.DataFromResult.toJson;
import static play.test.Helpers.contentAsString;

public class IngredientTagsFromResult {
    public static List<Long> ingredientIdsOfIngredientTagOf(Result result, int atIndex) {
        ListOfValuesFromResult<Long> values = new ListOfValuesFromResult<Long>()
                .select("$.items[" + atIndex + "].ingredients")
                .converting(JsonNode::asLong);

        return values.of(result);
    }


    public static List<Long> ingredientTagIdsOf(Result result) {
        ListOfValuesFromResult<Long> values = new ListOfValuesFromResult<Long>()
                .select("$.items")
                .converting(n -> n.get("id").asLong());

        return values.of(result);
    }

    public static List<String> ingredientNamesOfSingleIngredientTagOf(Result result) {
        ListOfValuesFromResult<String> values = new ListOfValuesFromResult<String>()
                .select("$.ingredients")
                .converting(n -> n.get("name").asText());

        return values.of(result);
    }

    public static List<Long> ingredientIdsOfSingleIngredientTagOf(Result result) {
        ListOfValuesFromResult<Long> values = new ListOfValuesFromResult<Long>()
                .select("$.ingredients")
                .converting(n -> n.get("id").asLong());

        return values.of(result);
    }

    public static String singleIngredientTagNameOf(Result result) {
        JsonNode json = toJson(result);
        return json.get("name").asText();
    }

    public static List<String> conflictingUserSearchNamesOf(Result result) {
        ListOfValuesFromResult<String> values = new ListOfValuesFromResult<String>()
                .select("$.usersearches")
                .converting(n -> n.get("name").asText());

        return values.of(result);
    }

    public static List<Long> idsOfUserDefinedOnlyTagsOf(Result result) {
        ListOfValuesFromResult<Long> values = new ListOfValuesFromResult<Long>()
                .converting(n -> n.get("id").asLong());

        return values.of(result);
    }

    public static List<Long> ingredientTagIdsAsListOf(Result result) {
        ListOfValuesFromResult<Long> values = new ListOfValuesFromResult<Long>()
                .converting(n -> n.get("id").asLong());

        return values.of(result);
    }

    public static Map<Long, String> ingredientTagNamesByTagIds(Result result) {
        Map<Long, String> namesByTagIds = new HashMap<>();

        JsonNode jsonTags = toJson(result);

        jsonTags.forEach(tagJson -> {
            namesByTagIds.put(tagJson.get("id").asLong(), tagJson.get("name").asText());
        });

        return namesByTagIds;
    }
}
