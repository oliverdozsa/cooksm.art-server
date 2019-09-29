package dto;

import lombok.Data;

@Data
public class FavoriteRecipeCreateDto {
    private Long recipeId;

    public FavoriteRecipeCreateDto() {
    }

    public FavoriteRecipeCreateDto(Long recipeId) {
        this.recipeId = recipeId;
    }
}
