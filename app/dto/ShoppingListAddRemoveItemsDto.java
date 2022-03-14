package dto;

import play.data.validation.Constraints;

import javax.validation.constraints.Size;
import java.util.List;

public class ShoppingListAddRemoveItemsDto {
    @Constraints.Required
    @Size(max = 150)
    public List<@Constraints.MinLength(2) @Constraints.MaxLength(100) String> items;

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
