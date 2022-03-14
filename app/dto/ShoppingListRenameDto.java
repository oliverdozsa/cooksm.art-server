package dto;

import play.data.validation.Constraints;

public class ShoppingListRenameDto {
    @Constraints.Required
    @Constraints.MinLength(2)
    @Constraints.MaxLength(100)
    public String newName;

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }
}
