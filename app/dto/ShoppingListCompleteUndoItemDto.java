package dto;

import play.data.validation.Constraints;

public class ShoppingListCompleteUndoItemDto {
    @Constraints.Required
    private Long itemId;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    @Override
    public String toString() {
        return "ShoppingListCompleteUndoItemDto{" +
                "itemId=" + itemId +
                '}';
    }
}

