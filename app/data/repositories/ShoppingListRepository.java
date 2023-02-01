package data.repositories;

import data.entities.ShoppingList;
import dto.ShoppingListItemRequestDto;

import java.util.List;

public interface ShoppingListRepository {
    Long create(Long userId, String name, List<ShoppingListItemRequestDto> items);
    List<ShoppingList> allOfUser(Long userId);
    ShoppingList single(Long userId, Long shoppingListId);
    void delete(Long userId, Long shoppingListId);
    void rename(Long userId, Long shoppingListId, String newName);
    void addItems(Long userId, Long shoppingListId, List<ShoppingListItemRequestDto> newItems);
    void removeItems(Long userId, Long shoppingListId, List<Long> itemsToRemove);
    void completeAnItem(Long userId, Long shoppingListId, Long itemId);
    void undoAnItem(Long userId, Long shoppingListId, Long itemId);
}
