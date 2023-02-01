package controllers.v1;

import dto.*;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import security.SecurityUtils;
import security.VerifiedJwt;
import services.ShoppingListService;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Http.HeaderNames.LOCATION;
import static play.mvc.Results.*;

public class ShoppingListController {
    @Inject
    private FormFactory formFactory;

    @Inject
    private ShoppingListService service;

    private Function<Throwable, Result> mapException = new DefaultExceptionMapper(logger);
    private Function<Throwable, Result> mapExceptionWithUnpack = e -> mapException.apply(e.getCause());

    private static final Logger.ALogger logger = Logger.of(ShoppingListController.class);

    public CompletionStage<Result> create(Http.Request request) {
        logger.info("create()");

        Form<ShoppingListCreateDto> shoppingListCreateForm = formFactory.form(ShoppingListCreateDto.class)
                .bindFromRequest(request);

        if (shoppingListCreateForm.hasErrors()) {
            logger.warn("create(): Form has errors! errors = {}", shoppingListCreateForm.errorsAsJson());
            return completedFuture(badRequest(shoppingListCreateForm.errorsAsJson()));
        }

        ShoppingListCreateDto shoppingListCreateRequest = shoppingListCreateForm.get();
        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);

        return service.create(jwt.getUserId(), shoppingListCreateRequest)
                .thenApply(id -> {
                    String location = routes.ShoppingListController.single(id).absoluteURL(request);
                    logger.info("create(): Created shopping list with id: {}", id);
                    return created().withHeader(LOCATION, location);
                })
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> allOfUser(Http.Request request) {
        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);

        logger.info("allOfUser(): userId = {}", jwt.getUserId());

        return service.allOfUser(jwt.getUserId())
                .thenApply(shoppingListsOfUser -> ok(Json.toJson(shoppingListsOfUser)))
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> single(Long id, Http.Request request) {
        logger.info("single(): id = {}", id);

        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);
        return service.single(jwt.getUserId(), id)
                .thenApply(shoppingList -> ok(Json.toJson(shoppingList)))
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> delete(Long id, Http.Request request) {
        logger.info("delete(): id = {}", id);

        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);

        return service.delete(jwt.getUserId(), id)
                .thenApply(v -> (Result) noContent())
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> rename(Long id, Http.Request request) {
        logger.info("rename(): id = {}", id);

        Form<ShoppingListRenameDto> renameRequestForm = formFactory.form(ShoppingListRenameDto.class)
                .bindFromRequest(request);

        if (renameRequestForm.hasErrors()) {
            logger.warn("rename(): form has errors! errors = {}", renameRequestForm.errorsAsJson());
            return completedFuture(badRequest(renameRequestForm.errorsAsJson()));
        }

        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);

        return service.rename(jwt.getUserId(), id, renameRequestForm.get())
                .thenApply(v -> (Result) noContent())
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> addItems(Long id, Http.Request request) {
        logger.info("addItems(): id = {}", id);

        Form<ShoppingListAddItemsDto> addRemoveItemsForm = formFactory.form(ShoppingListAddItemsDto.class)
                .bindFromRequest(request);

        if (addRemoveItemsForm.hasErrors()) {
            logger.warn("addItems(): form has errors! errors = {}", addRemoveItemsForm.errorsAsJson());
            return completedFuture(badRequest(addRemoveItemsForm.errorsAsJson()));
        }

        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);

        return service.addItems(jwt.getUserId(), id, addRemoveItemsForm.get())
                .thenApply(v -> (Result) noContent())
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> removeItems(Long id, Http.Request request) {
        logger.info("removeItems(): id = {}", id);

        Form<ShoppingListRemoveItemsDto> removeItemsForm = formFactory.form(ShoppingListRemoveItemsDto.class)
                .bindFromRequest(request);

        if (removeItemsForm.hasErrors()) {
            logger.warn("removeItems(): form has errors! errors = {}", removeItemsForm.errorsAsJson());
            return completedFuture(badRequest(removeItemsForm.errorsAsJson()));
        }

        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);

        return service.removeItems(jwt.getUserId(), id, removeItemsForm.get())
                .thenApply(v -> (Result) noContent())
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> completeItem(Long id, Http.Request request) {
        logger.info("completeItem(): id = {}", id);

        Form<ShoppingListCompleteUndoItemDto> completeItemForm = formFactory.form(ShoppingListCompleteUndoItemDto.class)
                .bindFromRequest(request);

        if(completeItemForm.hasErrors()) {
            logger.warn("completeItem(): form has errors! errors = {}", completeItemForm.errorsAsJson());
            return completedFuture(badRequest());
        }

        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);

        return service.completeItem(jwt.getUserId(), id, completeItemForm.get())
                .thenApply(v -> (Result)noContent())
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> undoItem(Long id, Http.Request request) {
        logger.info("undoItem(): id = {}", id);

        Form<ShoppingListCompleteUndoItemDto> undoItemForm = formFactory.form(ShoppingListCompleteUndoItemDto.class)
                .bindFromRequest(request);

        if(undoItemForm.hasErrors()) {
            logger.warn("undoItem(): form has errors! errors = {}", undoItemForm.errorsAsJson());
            return completedFuture(badRequest());
        }

        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);

        return service.undoItem(jwt.getUserId(), id, undoItemForm.get())
                .thenApply(v -> (Result)noContent())
                .exceptionally(mapExceptionWithUnpack);
    }
}
