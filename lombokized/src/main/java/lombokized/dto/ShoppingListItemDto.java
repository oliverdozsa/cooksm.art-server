package lombokized.dto;

import lombok.Data;

@Data
public class ShoppingListItemDto {
    private final String name;
    private final Boolean isCompleted;
}
