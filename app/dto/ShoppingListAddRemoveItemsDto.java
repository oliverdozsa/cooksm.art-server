package dto;

import play.data.validation.Constraints;

import java.util.List;

public class ShoppingListAddRemoveItemsDto {
    public List<@Constraints.MinLength(2) String> items;

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "ShoppingListAddRemoveItemsDto{" +
                "items=" + items +
                '}';
    }
}
