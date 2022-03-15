package lombokized.dto;

import lombok.Data;

import java.util.List;

@Data
public class ShoppingListDto {
    private final Long id;
    private final String name;
    private final List<ShoppingListItemDto> items;
}
