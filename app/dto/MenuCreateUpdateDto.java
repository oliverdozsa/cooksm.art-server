package dto;

import play.data.validation.Constraints;

import java.util.List;

public class MenuCreateUpdateDto {
    @Constraints.Required
    @Constraints.MinLength(2)
    public String name;

    @Constraints.Required
    public List<Item> items;

    public static class Item {
        @Constraints.Required
        public Long recipeId;

        @Constraints.Required
        @Constraints.Min(1)
        public Integer group;

        @Constraints.Required
        @Constraints.Min(1)
        public Integer order;
    }
}
