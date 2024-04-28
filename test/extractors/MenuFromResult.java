package extractors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombokized.dto.MenuItemDto;
import play.libs.Json;
import play.mvc.Result;

import java.util.List;

import static extractors.DataFromResult.toJson;

public class MenuFromResult {
    public static String nameOf(Result result) {
        JsonNode json = toJson(result);
        return json.get("name").asText();
    }

    public static List<MenuItemDto> itemsOf(Result result) {
        ListOfValuesFromResult<MenuItemDto> items = new ListOfValuesFromResult<MenuItemDto>()
                .select("$.items")
                .converting(n -> {
                    try {
                        return Json.mapper().treeToValue(n, MenuItemDto.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });

        return items.of(result);
    }
}
