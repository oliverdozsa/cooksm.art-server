package clients;

import com.typesafe.config.Config;
import controllers.v1.routes;
import dto.MenuCreateUpdateDto;
import play.Application;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import utils.JwtTestUtils;

import static play.mvc.Http.HttpVerbs.POST;
import static play.test.Helpers.*;

public class MenuTestClient {
    private final Application application;
    private final Config config;

    public MenuTestClient(Application application) {
        this.application = application;
        this.config = application.config();
    }

    public Result create(MenuCreateUpdateDto menu, Long userId) {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(Json.toJson(menu))
                .uri(routes.MenuController.create().url());

        String jwt = JwtTestUtils.createToken(userId, config);
        JwtTestUtils.addJwtTokenTo(request, jwt);

        return route(application, request);
    }

    public Result delete(Long menuId, Long userId) {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(DELETE)
                .uri(routes.MenuController.delete(menuId).url());

        String jwt = JwtTestUtils.createToken(userId, config);
        JwtTestUtils.addJwtTokenTo(request, jwt);

        return route(application, request);
    }

    public Result update(Long menuId, MenuCreateUpdateDto menu, Long userId) {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(PUT)
                .bodyJson(Json.toJson(menu))
                .uri(routes.MenuController.update(menuId).url());

        String jwt = JwtTestUtils.createToken(userId, config);
        JwtTestUtils.addJwtTokenTo(request, jwt);

        return route(application, request);
    }

    public Result getById(Long menuId, Long userId) {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri(routes.MenuController.getById(menuId, 0L).url());

        String jwt = JwtTestUtils.createToken(userId, config);
        JwtTestUtils.addJwtTokenTo(request, jwt);

        return route(application, request);
    }

    public Result getAllOf(Long userId) {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri(routes.MenuController.getAll().url());

        String jwt = JwtTestUtils.createToken(userId, config);
        JwtTestUtils.addJwtTokenTo(request, jwt);

        return route(application, request);
    }
}
