package extractors;

import com.fasterxml.jackson.databind.JsonNode;
import play.mvc.Result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static extractors.DataFromResult.toJson;

public class ShoppingListFromResult {
    public static List<String> itemNamesOfShoppingListOf(Result result) {
        ListOfValuesFromResult<String> itemNames = new ListOfValuesFromResult<String>()
                .select("$.items")
                .converting(n -> n.get("name").asText());

        return itemNames.of(result);
    }

    public static List<Long> categoryIdsOfShoppingListOf(Result result) {
        ListOfValuesFromResult<Long> itemCategoryIds = new ListOfValuesFromResult<Long>()
                .select("$.items")
                .converting(n -> n.get("categoryId").asLong());

        return itemCategoryIds.of(result);
    }

    public static Long idOfShoppingListOf(Result result) {
        JsonNode shoppingListJson = toJson(result);
        return shoppingListJson.get("id").asLong();
    }

    public static List<String> shoppingListNamesOf(Result result) {
        ListOfValuesFromResult<String> shoppingListNames = new ListOfValuesFromResult<String>()
                .converting(i -> i.get("name").asText());

        return shoppingListNames.of(result);
    }

    public static List<Long> shoppingListIdsOf(Result result) {
        ListOfValuesFromResult<Long> shoppingListIds = new ListOfValuesFromResult<Long>()
                .converting(i -> i.get("id").asLong());

        return shoppingListIds.of(result);
    }

    public static Map<Long, Boolean> itemStatesOf(Result result) {
        JsonNode shoppingListJson = toJson(result);

        Map<Long, Boolean> itemStates = new HashMap<>();

        shoppingListJson.get("items").forEach(itemJson -> {
            itemStates.put(itemJson.get("id").asLong(), itemJson.get("isCompleted").asBoolean());
        });

        return itemStates;
    }

    public static String shoppingListNameOf(Result result) {
        JsonNode shoppingListJson = toJson(result);
        return shoppingListJson.get("name").asText();
    }
}
