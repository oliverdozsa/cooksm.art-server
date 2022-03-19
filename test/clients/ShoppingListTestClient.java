package clients;

import com.typesafe.config.Config;
import controllers.v1.routes;
import dto.ShoppingListAddRemoveItemsDto;
import dto.ShoppingListCompleteUndoItemDto;
import dto.ShoppingListCreateDto;
import dto.ShoppingListRenameDto;
import play.Application;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import security.SecurityUtils;
import utils.JwtTestUtils;

import static play.mvc.Http.HttpVerbs.GET;
import static play.mvc.Http.HttpVerbs.POST;
import static play.test.Helpers.*;

public class ShoppingListTestClient {
    private final Application application;
    private final Config config;

    public ShoppingListTestClient(Application application) {
        this.application = application;
        this.config = application.config();
    }

    public Result create(Long userId, ShoppingListCreateDto createRequest) {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(Json.toJson(createRequest))
                .uri(routes.ShoppingListController.create().url());

        String jwt = JwtTestUtils.createToken(userId, config);
        JwtTestUtils.addJwtTokenTo(request, jwt);

        return route(application, request);
    }

    public Result allOfUser(Long userId) {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri(routes.ShoppingListController.allOfUser().url());

        String jwt = JwtTestUtils.createToken(userId, config);
        JwtTestUtils.addJwtTokenTo(request, jwt);

        return route(application, request);
    }

    public Result getSingle(Long userId, Long shoppingListId) {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri(routes.ShoppingListController.single(shoppingListId).url());

        String jwt = JwtTestUtils.createToken(userId, config);
        JwtTestUtils.addJwtTokenTo(request, jwt);

        return route(application, request);
    }

    public Result delete(Long userId, Long shoppingListId) {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(DELETE)
                .uri(routes.ShoppingListController.delete(shoppingListId).url());

        String jwt = JwtTestUtils.createToken(userId, config);
        JwtTestUtils.addJwtTokenTo(request, jwt);

        return route(application, request);
    }

    public Result addItems(Long userId, Long shoppingListId, ShoppingListAddRemoveItemsDto addItemsRequest) {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(Json.toJson(addItemsRequest))
                .uri(routes.ShoppingListController.addItems(shoppingListId).url());

        String jwt = JwtTestUtils.createToken(userId, config);
        JwtTestUtils.addJwtTokenTo(request, jwt);

        return route(application, request);
    }

    public Result removeItems(Long userId, Long shoppingListId, ShoppingListAddRemoveItemsDto removeItemsRequest) {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(Json.toJson(removeItemsRequest))
                .uri(routes.ShoppingListController.removeItems(shoppingListId).url());

        String jwt = JwtTestUtils.createToken(userId, config);
        JwtTestUtils.addJwtTokenTo(request, jwt);

        return route(application, request);
    }

    public Result rename(Long userId, Long shoppingListId, String newName) {
        ShoppingListRenameDto dto = new ShoppingListRenameDto();
        dto.setNewName(newName);

        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(PUT)
                .bodyJson(Json.toJson(dto))
                .uri(routes.ShoppingListController.rename(shoppingListId).url());

        String jwt = JwtTestUtils.createToken(userId, config);
        JwtTestUtils.addJwtTokenTo(request, jwt);

        return route(application, request);
    }

    public Result completeAnItem(Long userId, Long shoppingListId, String item) {
        ShoppingListCompleteUndoItemDto dto = new ShoppingListCompleteUndoItemDto();
        dto.setName(item);

        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(Json.toJson(dto))
                .uri(routes.ShoppingListController.completeItem(shoppingListId).url());

        String jwt = JwtTestUtils.createToken(userId, config);
        JwtTestUtils.addJwtTokenTo(request, jwt);

        return route(application, request);
    }

    public Result undoAnItem(Long userId, Long shoppingListId, String item) {
        ShoppingListCompleteUndoItemDto dto = new ShoppingListCompleteUndoItemDto();
        dto.setName(item);

        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(Json.toJson(dto))
                .uri(routes.ShoppingListController.undoItem(shoppingListId).url());

        String jwt = JwtTestUtils.createToken(userId, config);
        JwtTestUtils.addJwtTokenTo(request, jwt);

        return route(application, request);
    }

    public Result byLocation(String url, Long userId) {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri(url);

        String jwt = JwtTestUtils.createToken(userId, config);
        JwtTestUtils.addJwtTokenTo(request, jwt);

        return route(application, request);
    }
}
