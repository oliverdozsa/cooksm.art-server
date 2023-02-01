package lombokized.dto;

import lombok.Data;

@Data
public class ShoppingListItemDto {
    private final Long id;
    private final String name;
    private final Boolean isCompleted;
    private final Long categoryId;
}
