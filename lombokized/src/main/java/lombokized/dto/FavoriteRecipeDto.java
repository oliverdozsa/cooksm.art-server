package lombokized.dto;

import lombok.Data;

@Data
public class FavoriteRecipeDto {
    private final Long id;
    private final String name;
    private final String url;
    private final Long recipeId;
}
