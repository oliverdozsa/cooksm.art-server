package extractors;

import com.fasterxml.jackson.databind.JsonNode;
import play.mvc.Result;

import java.util.List;

public class IngredientNamesFromResult {
    public static List<String> alternativeIngredientNamesOf(Result result, int atItemIndex) {
        ListOfValuesFromResult<String> values = new ListOfValuesFromResult<String>()
                .select("$.items[" + atItemIndex + "].altNames")
                .converting(JsonNode::asText);

        return values.of(result);
    }

    public static List<String> ingredientNamesOf(Result result) {
        ListOfValuesFromResult<String> values = new ListOfValuesFromResult<String>()
                .select("$.items")
                .converting(n -> n.get("name").asText());

        return values.of(result);
    }

    public static List<Long> ingredientNameIdsOf(Result result) {
        ListOfValuesFromResult<Long> values = new ListOfValuesFromResult<Long>()
                .select("$.items")
                .converting(n -> n.get("name").asLong());

        return values.of(result);
    }
}
