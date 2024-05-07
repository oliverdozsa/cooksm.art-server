package lombokized.dto;

import lombok.Data;

@Data
public class MenuItemDto {
    private final RecipeDto recipeDto;
    private final Integer group;
    private final Integer order;
}
