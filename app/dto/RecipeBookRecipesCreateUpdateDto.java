package dto;

import play.data.validation.Constraints;

import java.util.List;

public class RecipeBookRecipesCreateUpdateDto {
    @Constraints.Required
    public List<Long> recipeIds;

    @Override
    public String toString() {
        return "RecipeBookRecipesCreateUpdateDto{" +
                "recipeIds=" + recipeIds +
                '}';
    }
}
