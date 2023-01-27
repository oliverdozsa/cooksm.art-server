package dto;

import java.util.ArrayList;
import java.util.List;

public class RecipeBookRecipesCreateUpdateRemoveDto {
    public List<Long> recipeIds;

    public RecipeBookRecipesCreateUpdateRemoveDto() {
        recipeIds = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "RecipeBookRecipesCreateUpdateDto{" +
                "recipeIds=" + recipeIds +
                '}';
    }
}
