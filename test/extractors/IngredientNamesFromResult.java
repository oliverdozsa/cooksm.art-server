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
}
