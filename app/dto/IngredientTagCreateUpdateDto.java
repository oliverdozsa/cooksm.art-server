package dto;

import play.data.validation.Constraints;

import java.util.List;

public class IngredientTagCreateUpdateDto {
    @Constraints.Required
    @Constraints.MinLength(2)
    public String name;

    @Constraints.Required
    public List<Long> ingredientIds;

    @Override
    public String toString() {
        return "IngredientTagCreateUpdateDto{" +
                "name='" + name + '\'' +
                ", ingredientIds=" + ingredientIds +
                '}';
    }
}
