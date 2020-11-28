package extractors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import play.mvc.Result;
import utils.Base62Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static extractors.DataFromResult.toJson;

public class UserSearchesFromResult {
    public static String nameOfSingleUserSearchOf(Result result){
        JsonNode json = toJson(result);
        return json.get("name").asText();
    }

    public static Long idOfSingleUserSearchOf(Result result){
        JsonNode json = toJson(result);
        return json.get("id").asLong();
    }

    public static String recipeSearchIdOfSingleUserSearchOf(Result result){
        JsonNode json = toJson(result);
        return json.get("searchId").asText();
    }

    public static List<String> namesOfUserSearchesOf(Result result) {
        List<String> names = new ArrayList<>();
        ArrayNode json = (ArrayNode) toJson(result);

        json.forEach(n -> names.add(n.get("name").asText()));

        return names;
    }

    public static List<Long> searchIdsOfUserSearchesOf(Result result) {
        List<String> names = new ArrayList<>();
        ArrayNode json = (ArrayNode) toJson(result);

        json.forEach(n -> names.add(n.get("searchId").asText()));

        return names.stream().map(Base62Utils::decode)
                .collect(Collectors.toList());
    }
}
