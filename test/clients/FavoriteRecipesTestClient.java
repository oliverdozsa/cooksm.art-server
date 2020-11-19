package clients;

import com.typesafe.config.Config;
import controllers.v1.routes;
import lombokized.dto.FavoriteRecipeCreateDto;
import play.Application;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import utils.JwtTestUtils;

import static play.test.Helpers.*;

public class FavoriteRecipesTestClient {
    private final Application application;
    private final Config config;

    public FavoriteRecipesTestClient(Application application) {
        this.application = application;
        config = application.config();
    }

    public Result allOf(Long userId) {
        Http.RequestBuilder request = new Http.RequestBuilder().uri(routes.FavoriteRecipesController.all().url());
        String jwt = JwtTestUtils.createToken(userId, application.config());
        JwtTestUtils.addJwtTokenTo(request, jwt);

        return route(application, request);
    }

    public Result create(FavoriteRecipeCreateDto dto, Long userId) {
        Http.RequestBuilder request = createPostRequest(userId)
                .bodyJson(Json.toJson(dto));

        return route(application, request);
    }

    public Result create(String text, Long userId) {
        Http.RequestBuilder request = createPostRequest(userId)
                .bodyText(text);

        return route(application, request);
    }

    public Result byLocation(String url, Long userId) {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri(url);
        String jwt = JwtTestUtils.createToken(userId, application.config());
        JwtTestUtils.addJwtTokenTo(request, jwt);

        return route(application, request);
    }

    public Result delete(Long id, Long userId) {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(DELETE)
                .uri(routes.FavoriteRecipesController.delete(id).url());
        String jwt = JwtTestUtils.createToken(userId, config);
        JwtTestUtils.addJwtTokenTo(request, jwt);

        return route(application, request);
    }

    public Result single(Long id, Long userId) {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri(routes.FavoriteRecipesController.single(id).url());
        String jwt = JwtTestUtils.createToken(userId, config);
        JwtTestUtils.addJwtTokenTo(request, jwt);

        return route(application, request);
    }

    private Http.RequestBuilder createPostRequest(Long userId) {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(POST)
                .uri(routes.FavoriteRecipesController.create().url());
        String jwt = JwtTestUtils.createToken(userId, application.config());
        JwtTestUtils.addJwtTokenTo(request, jwt);

        return request;
    }
}
