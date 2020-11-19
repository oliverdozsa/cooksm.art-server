package utils;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import play.mvc.Result;

import static play.test.Helpers.contentAsString;

public class ExtractFromResult {
    public static int itemsSizeOf(Result result) {
        String jsonStr = contentAsString(result);
        JsonNode json = Json.parse(jsonStr);

        return json.get("items").asInt();
    }

    public static int totalCountOf(Result result) {
        String jsonStr = contentAsString(result);
        JsonNode json = Json.parse(jsonStr);

        return json.get("totalCount").asInt();
    }


}
