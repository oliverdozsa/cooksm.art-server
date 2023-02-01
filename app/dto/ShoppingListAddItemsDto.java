package dto;

import play.data.validation.Constraints;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.List;

public class ShoppingListAddItemsDto {
    @Constraints.Required
    @Size(max = 150)
    @Valid
    public List<ShoppingListItemRequestDto> items;

    public List<ShoppingListItemRequestDto> getItems() {
        return items;
    }

    public void setItems(List<ShoppingListItemRequestDto> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "ShoppingListAddRemoveItemsDto{" +
                "items=" + items +
                '}';
    }
}
