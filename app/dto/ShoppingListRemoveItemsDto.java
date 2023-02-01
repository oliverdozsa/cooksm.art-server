package dto;

import play.data.validation.Constraints;

import javax.validation.constraints.Size;
import java.util.List;

public class ShoppingListRemoveItemsDto {
    @Constraints.Required
    @Size(max = 150)
    public List<Long> itemIds;

    public List<Long> getItemIds() {
        return itemIds;
    }

    public void setItemIds(List<Long> itemIds) {
        this.itemIds = itemIds;
    }

    @Override
    public String toString() {
        return "ShoppingListRemoveItemsDto{" +
                "itemIds=" + itemIds +
                '}';
    }
}
