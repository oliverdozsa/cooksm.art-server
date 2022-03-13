package clients;

import com.typesafe.config.Config;
import dto.ShoppingListAddRemoveItemsDto;
import dto.ShoppingListCreateDto;
import play.Application;
import play.mvc.Result;

public class ShoppingListTestClient {
    private final Application application;
    private final Config config;

    public ShoppingListTestClient(Application application) {
        this.application = application;
        this.config = application.config();
    }

    public Result create(Long userId, ShoppingListCreateDto createRequest) {
        // TODO
        return null;
    }

    public Result allOfUser(Long userId) {
        // TODO
        return null;
    }

    public Result testGetSingle(Long userId, Long shoppingListId) {
        // TODO
        return null;
    }

    public Result testDelete(Long userId, Long shoppingListId) {
        // TODO
        return null;
    }

    public Result addItems(Long userId, Long shoppingListId, ShoppingListAddRemoveItemsDto addItemsRequest) {
        // TODO
        return null;
    }

    public Result removeItems(Long userId, Long shoppingListId, ShoppingListAddRemoveItemsDto addItemsRequest) {
        // TODO
        return null;
    }

    public Result rename(Long userId, Long shoppingListId, String newName) {
        // TODO
        return null;
    }

    public Result completeAnItem(Long userId, Long shoppingListId, String item) {
        // TODO
        return null;
    }

    public Result undoAnItem(Long userId, Long shoppingListId, String item) {
        // TODO
        return null;
    }
}
