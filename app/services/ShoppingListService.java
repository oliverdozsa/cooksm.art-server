package services;

import data.DatabaseExecutionContext;
import data.entities.ShoppingList;
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
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public class ShoppingListService {
    @Inject
    private ShoppingListRepository repository;

    @Inject
    private DatabaseExecutionContext dbExecContext;

    private static final Logger.ALogger logger = Logger.of(ShoppingListService.class);

    public CompletionStage<Long> create(Long userId, ShoppingListCreateDto shoppingListCreateRequest) {
        logger.info("create(): userId = {}, shoppingListCreateRequest = {}", userId, shoppingListCreateRequest);

        return supplyAsync(() -> repository.create(userId, shoppingListCreateRequest.name, shoppingListCreateRequest.items),
                dbExecContext);
    }

    public CompletionStage<List<ShoppingListListElementDto>> allOfUser(Long userId) {
        logger.info("allOfUser(): userId = {}", userId);
        return supplyAsync(() -> repository.allOfUser(userId), dbExecContext)
                .thenApply(ShoppingListService::toDtoList);
    }

    public CompletionStage<ShoppingListDto> single(Long userId, Long shoppingListId) {
        logger.info("single(): userId = {}, shoppingListId = {}", userId, shoppingListId);
        return supplyAsync(() -> repository.single(userId, shoppingListId), dbExecContext)
                .thenApply(DtoMapper::toDto);
    }

    public CompletionStage<Void> delete(Long userId, Long shoppingListId) {
        logger.info("delete(): userId = {}, shoppingListId = {}", userId, shoppingListId);
        return runAsync(() -> repository.delete(userId, shoppingListId), dbExecContext);
    }

    public CompletionStage<Void> rename(Long userId, Long shoppingListId, ShoppingListRenameDto renameDto) {
        logger.info("rename(): userId = {}, shoppingListId = {}, renameDto = {}",
                userId, shoppingListId, renameDto);
        return runAsync(() -> repository.rename(userId, shoppingListId, renameDto.getNewName()), dbExecContext);
    }

    public CompletionStage<Void> addItems(Long userId, Long shoppingListId, ShoppingListAddRemoveItemsDto addRemoveItemsDto) {
        logger.info("addItems(): userId = {}, shoppingListId = {}, addRemoveItemsDto = {}",
                userId, shoppingListId, addRemoveItemsDto);
        return runAsync(() -> repository.addItems(userId, shoppingListId, addRemoveItemsDto.getItems()), dbExecContext);
    }

    public CompletionStage<Void> removeItems(Long userId, Long shoppingListId, ShoppingListAddRemoveItemsDto addRemoveItemsDto) {
        logger.info("userId = {}, shoppingListId = {}, addRemoveItemsDto = {}",
                userId, shoppingListId, addRemoveItemsDto);
        return runAsync(() -> repository.removeItems(userId, shoppingListId, addRemoveItemsDto.getItems()), dbExecContext);
    }

    public CompletionStage<Void> completeItem(Long userId, Long shoppingListId, ShoppingListCompleteUndoItemDto completeRequest) {
        logger.info("completeItem(): userId = {}, shoppingListId = {}, completeRequest = {}",
                userId, shoppingListId, completeRequest);
        return runAsync(() -> repository.completeAnItem(userId, shoppingListId, completeRequest.getName()), dbExecContext);
    }

    public CompletionStage<Void> undoItem(Long userId, Long shoppingListId, ShoppingListCompleteUndoItemDto undoRequest) {
        logger.info("undoItem(): userId = {}, shoppingListId = {}, undoRequest = {}",
                userId, shoppingListId, undoRequest);
        return runAsync(() -> repository.undoAnItem(userId, shoppingListId, undoRequest.getName()), dbExecContext);
    }

    private static List<ShoppingListListElementDto> toDtoList(List<ShoppingList> entities) {
        return entities.stream()
                .map(DtoMapper::toShoppingListListElementDto)
                .collect(Collectors.toList());
    }
}
