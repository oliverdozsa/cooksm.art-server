package extractors;

import com.fasterxml.jackson.databind.JsonNode;
import play.mvc.Result;

import java.util.List;

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

    public static List<String> ingredientNamesOfSingleIngredientTagOfResult(Result result){
        ListOfValuesFromResult<String> values = new ListOfValuesFromResult<String>()
                .select("$.ingredients")
                .converting(n -> n.get("name").asText());

        return values.of(result);
    }
}
