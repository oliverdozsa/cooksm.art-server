package extractors;

import com.fasterxml.jackson.databind.JsonNode;
import play.mvc.Result;

import java.util.List;

import static extractors.DataFromResult.toJson;

public class MenuFromResult {
    public static String nameOf(Result result) {
        JsonNode json = toJson(result);
        return json.get("name").asText();
    }

    public static List<JsonNode> itemsOf(Result result) {
        ListOfValuesFromResult<JsonNode> items = new ListOfValuesFromResult<JsonNode>()
                .select("$.items")
                .converting(n -> n);

        return items.of(result);
    }

    public static List<Long> menuIdsOf(Result result) {
        ListOfValuesFromResult<Long> values = new ListOfValuesFromResult<Long>()
                .converting(n -> n.get("id").asLong());

        return values.of(result);
    }
}
