package dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class FavoriteRecipeCreateDto {
    private Long recipeId;

    public FavoriteRecipeCreateDto() {
    }

    public FavoriteRecipeCreateDto(Long recipeId) {
        this.recipeId = recipeId;
    }
}
