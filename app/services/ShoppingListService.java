package services;

import dto.ShoppingListAddRemoveItemsDto;
import dto.ShoppingListCompleteUndoItemDto;
import dto.ShoppingListCreateDto;
import dto.ShoppingListRenameDto;
import lombokized.dto.ShoppingListDto;
import lombokized.dto.ShoppingListListElementDto;
import play.Logger;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class ShoppingListService {
    private static final Logger.ALogger logger = Logger.of(ShoppingListService.class);

    public CompletionStage<Long> create(Long userId, ShoppingListCreateDto shoppingListCreateRequest) {
        // TODO
        return null;
    }

    public CompletionStage<List<ShoppingListListElementDto>> allOfUser(Long userId) {
        // TODO
        return null;
    }

    public CompletionStage<ShoppingListDto> single(Long userId, Long shoppingListId) {
        // TODO: When getting items from DB, order them by their ID to guarantee elements are returned in the order they
        // TODO: were added.
        return null;
    }

    public CompletionStage<Void> delete(Long userId, Long shoppingListId) {
        // TODO
        return null;
    }

    public CompletionStage<Void> rename(Long userId, Long shoppingListId, ShoppingListRenameDto renameDto) {
        // TODO
        return null;
    }

    public CompletionStage<Void> addItems(Long userId, Long shoppingListId, ShoppingListAddRemoveItemsDto addRemoveItemsDto) {
        // TODO
        return null;
    }

    public CompletionStage<Void> removeItems(Long userId, Long shoppingListId, ShoppingListAddRemoveItemsDto addRemoveItemsDto) {
        // TODO
        return null;
    }

    public CompletionStage<Void> completeItem(Long userId, Long shoppingListId, ShoppingListCompleteUndoItemDto completeRequest) {
        // TODO
        return null;
    }

    public CompletionStage<Void> undoItem(Long userId, Long shoppingListId, ShoppingListCompleteUndoItemDto completeRequest) {
        // TODO
        return null;
    }
}
