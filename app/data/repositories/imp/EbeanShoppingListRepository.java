package data.repositories.imp;

import data.entities.ShoppingList;
import data.repositories.ShoppingListRepository;

import java.util.List;

public class EbeanShoppingListRepository implements ShoppingListRepository {
    @Override
    public Long create(Long userId, String name, List<String> items) {
        return null;
    }

    @Override
    public List<ShoppingList> allOfUser(Long userId) {
        return null;
    }

    @Override
    public ShoppingList single(Long userId, Long shoppingListId) {
        // TODO: When getting items from DB, order them by their ID to guarantee elements are returned in the order they
        // TODO: were added.
        return null;
    }

    @Override
    public void delete(Long userId, Long shoppingListId) {

    }

    @Override
    public void rename(Long userId, Long shoppingListId, String newName) {

    }

    @Override
    public void addItems(Long userId, Long shoppingListId, List<String> newItems) {

    }

    @Override
    public void removeItems(Long userId, Long shoppingListId, List<String> itemsToRemove) {

    }

    @Override
    public void completeAnItem(Long userId, Long shoppingListId, String item) {

    }

    @Override
    public void undoAnItem(Long userId, Long shoppingListId, String item) {

    }
}
