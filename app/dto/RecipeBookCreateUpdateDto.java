package dto;

import play.data.validation.Constraints;

public class RecipeBookCreateUpdateDto {
    @Constraints.Required
    @Constraints.MinLength(2)
    public String name;

    @Override
    public String toString() {
        return "RecipeBookCreateUpdateDto{" +
                "name='" + name + '\'' +
                '}';
    }
}
