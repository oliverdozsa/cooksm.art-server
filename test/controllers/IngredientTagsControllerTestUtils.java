package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.ArrayList;
import java.util.List;

public class IngredientTagsControllerTestUtils {
    public static List<Long> toIngredientIdList(JsonNode ingredientListJson) {
        List<Long> values = new ArrayList<>();

        ArrayNode arrayNode = (ArrayNode) ingredientListJson;
        arrayNode.forEach(n -> values.add(n.get("id").asLong()));

        return values;
    }

    public static List<String> toIngredientNames(JsonNode ingredientListJson) {
        List<String> values = new ArrayList<>();

        ArrayNode arrayNode = (ArrayNode) ingredientListJson;
        arrayNode.forEach(n -> values.add(n.get("name").asText()));

        return values;
    }

    public static List<Long> extractTagIds(JsonNode result) {
        List<Long> ids = new ArrayList<>();

        ArrayNode arrayNode = (ArrayNode) result;
        arrayNode.forEach(n -> ids.add(n.get("id").asLong()));

        return ids;
    }
}
