package lombokized.dto;

import lombok.Data;

@Data
public class ShoppingListListElementDto {
    private final Long id;
    private final String name;
}
