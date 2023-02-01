package dto;

import play.data.validation.Constraints;

public class ShoppingListItemRequestDto {
    @Constraints.Required
    @Constraints.MinLength(2)
    @Constraints.MaxLength(100)
    public String name;

    @Constraints.Required
    public Long categoryId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    @Override
    public String toString() {
        return "ShoppingListItemDto{" +
                "name='" + name + '\'' +
                ", categoryId=" + categoryId +
                '}';
    }
}
