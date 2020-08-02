package lombokized.dto;

import lombok.Data;

@Data
public class FavoriteRecipeDto {
    private final Long id;
    private final Long recipeId;
}
