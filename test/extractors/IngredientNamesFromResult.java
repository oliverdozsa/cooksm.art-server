package extractors;

import com.fasterxml.jackson.databind.JsonNode;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static List<String> ingredientNamesAsListOf(Result result) {
        ListOfValuesFromResult<String> values = new ListOfValuesFromResult<String>()
                .converting(n -> n.get("name").asText());

        return values.of(result);
    }

    public static List<Long> ingredientIdsOf(Result result) {
        ListOfValuesFromResult<Long> values = new ListOfValuesFromResult<Long>()
                .select("$.items")
                .converting(n -> n.get("id").asLong());

        return values.of(result);
    }

    public static List<Long> ingredientIdsAsListOf(Result result) {
        ListOfValuesFromResult<Long> values = new ListOfValuesFromResult<Long>()
                .converting(n -> n.get("id").asLong());

        return values.of(result);
    }

    public static Map<String, List<String>> alternativesOfIngredientNames(Result result) {
        List<JsonNode> ingredientNames =  IngredientNamesFromResult.ingredientNameJsonsOf(result);

        Map<String, List<String>> alternatives = new HashMap<>();
        ingredientNames.forEach(i -> alternatives.put(i.get("name").asText(), altNamesJsonToList(i.get("altNames"))));
        return alternatives;
    }

    public static Map<String, List<String>> alternativesOfIngredientNamesAsList(Result result) {
        List<JsonNode> ingredientNames =  IngredientNamesFromResult.ingredientNameAsListJsonsOf(result);
        Map<String, List<String>> alternatives = new HashMap<>();
        ingredientNames.forEach(i -> alternatives.put(i.get("name").asText(), altNamesJsonToList(i.get("altNames"))));
        return alternatives;
    }

    private static List<JsonNode> ingredientNameJsonsOf(Result result) {
        ListOfValuesFromResult<JsonNode> values = new ListOfValuesFromResult<JsonNode>()
                .select("$.items")
                .converting(n -> n);

        return values.of(result);
    }

    private static List<JsonNode> ingredientNameAsListJsonsOf(Result result) {
        ListOfValuesFromResult<JsonNode> values = new ListOfValuesFromResult<JsonNode>()
                .converting(n -> n);

        return values.of(result);
    }

    private static List<String> altNamesJsonToList(JsonNode altName) {
        List<String> result = new ArrayList<>();
        altName.forEach(a -> result.add(a.asText()));
        return result;
    }
}
