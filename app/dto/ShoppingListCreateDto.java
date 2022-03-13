package dto;

import play.data.validation.Constraints;

import java.util.List;

public class ShoppingListCreateDto {
    @Constraints.Required
    @Constraints.MinLength(2)
    public String name;

    public List<@Constraints.MinLength(2) String> items;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "ShoppingListCreateDto{" +
                "name='" + name + '\'' +
                ", items=" + items +
                '}';
    }
}
