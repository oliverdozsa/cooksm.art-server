package lombokized.dto;

import lombok.Data;

@Data
public class MenuItemDto {
    private final RecipeDto recipe;
    private final Integer group;
    private final Integer order;
}
