package services;

import data.repositories.ShoppingListRepository;
import dto.ShoppingListAddRemoveItemsDto;
import dto.ShoppingListCompleteUndoItemDto;
import dto.ShoppingListCreateDto;
import dto.ShoppingListRenameDto;
import lombokized.dto.ShoppingListDto;
import lombokized.dto.ShoppingListListElementDto;
import play.Logger;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class ShoppingListService {
    @Inject
    private ShoppingListRepository repository;

    private static final Logger.ALogger logger = Logger.of(ShoppingListService.class);

    public CompletionStage<Long> create(Long userId, ShoppingListCreateDto shoppingListCreateRequest) {
        logger.info("create(): userId = {}, shoppingListCreateRequest = {}", userId, shoppingListCreateRequest);
        // TODO
        return null;
    }

    public CompletionStage<List<ShoppingListListElementDto>> allOfUser(Long userId) {
        logger.info("allOfUser(): userId = {}", userId);
        // TODO
        return null;
    }

    public CompletionStage<ShoppingListDto> single(Long userId, Long shoppingListId) {
        logger.info("single(): userId = {}, shoppingListId = {}", userId, shoppingListId);

        // TODO: When getting items from DB, order them by their ID to guarantee elements are returned in the order they
        // TODO: were added.
        return null;
    }

    public CompletionStage<Void> delete(Long userId, Long shoppingListId) {
        logger.info("delete(): userId = {}, shoppingListId = {}", userId, shoppingListId);
        // TODO
        return null;
    }

    public CompletionStage<Void> rename(Long userId, Long shoppingListId, ShoppingListRenameDto renameDto) {
        logger.info("rename(): userId = {}, shoppingListId = {}, renameDto = {}",
                userId, shoppingListId, renameDto);

        // TODO
        return null;
    }

    public CompletionStage<Void> addItems(Long userId, Long shoppingListId, ShoppingListAddRemoveItemsDto addRemoveItemsDto) {
        logger.info("addItems(): userId = {}, shoppingListId = {}, addRemoveItemsDto = {}",
                userId, shoppingListId, addRemoveItemsDto);

        // TODO
        return null;
    }

    public CompletionStage<Void> removeItems(Long userId, Long shoppingListId, ShoppingListAddRemoveItemsDto addRemoveItemsDto) {
        logger.info("userId = {}, shoppingListId = {}, addRemoveItemsDto = {}",
                userId, shoppingListId, addRemoveItemsDto);
        // TODO
        return null;
    }

    public CompletionStage<Void> completeItem(Long userId, Long shoppingListId, ShoppingListCompleteUndoItemDto completeRequest) {
        logger.info("completeItem(): userId = {}, shoppingListId = {}, completeRequest = {}",
                userId, shoppingListId, completeRequest);
        // TODO
        return null;
    }

    public CompletionStage<Void> undoItem(Long userId, Long shoppingListId, ShoppingListCompleteUndoItemDto undoRequest) {
        logger.info("undoItem(): userId = {}, shoppingListId = {}, undoRequest = {}",
                userId, shoppingListId, undoRequest);
        // TODO
        return null;
    }
}
