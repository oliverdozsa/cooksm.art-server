package dto;

import play.data.validation.Constraints;

public class ShoppingListCompleteUndoItemDto {
    @Constraints.MinLength(2)
    @Constraints.MaxLength(100)
    public String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ShoppingListCompleteUndoItemDto{" +
                "name='" + name + '\'' +
                '}';
    }
}

