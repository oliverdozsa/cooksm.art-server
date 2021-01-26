package dto;

import java.util.ArrayList;
import java.util.List;

public class RecipeBookRecipesCreateUpdateDto {
    public List<Long> recipeIds;

    public RecipeBookRecipesCreateUpdateDto() {
        recipeIds = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "RecipeBookRecipesCreateUpdateDto{" +
                "recipeIds=" + recipeIds +
                '}';
    }
}
