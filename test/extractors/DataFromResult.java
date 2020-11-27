package extractors;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import play.mvc.Result;

import static play.test.Helpers.contentAsString;

public class DataFromResult {
    public static int itemsSizeOf(Result result) {
        JsonNode json = toJson(result);
        return json.get("items").size();
    }

    public static int totalCountOf(Result result) {
        JsonNode json = toJson(result);
        return json.get("totalCount").asInt();
    }

    public static int statusOf(Result result) {
        return result.status();
    }

    public static int sizeAsJsonOf(Result result) {
        return toJson(result).size();
    }

    public static String jwtOf(Result result) {
        JsonNode json = toJson(result);
        return json.get("jwtAuthToken").asText();
    }

    static JsonNode toJson(Result result) {
        String jsonStr = contentAsString(result);
        return Json.parse(jsonStr);
    }
}
