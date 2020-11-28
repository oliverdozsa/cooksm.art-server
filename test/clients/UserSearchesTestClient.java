package clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import controllers.v1.routes;
import play.Application;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import utils.JwtTestUtils;

import static play.mvc.Http.HttpVerbs.GET;
import static play.mvc.Http.HttpVerbs.PATCH;
import static play.test.Helpers.*;

public class UserSearchesTestClient {
    private final Application application;
    private final Config config;

    public UserSearchesTestClient(Application application) {
        this.application = application;
        this.config = application.config();
    }

    public Result create(String queryStr, Long userId) {
        JsonNode json = Json.parse(queryStr);
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(json)
                .uri(routes.UserSearchesController.create().url());
        String jwt = JwtTestUtils.createToken(userId, config);
        JwtTestUtils.addJwtTokenTo(request, jwt);

        return route(application, request);
    }

    public Result byLocation(String url, Long userId){
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri(url);
        String jwt = JwtTestUtils.createToken(userId, config);
        JwtTestUtils.addJwtTokenTo(request, jwt);

        return route(application, request);
    }

    public Result all(Long userId) {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri(routes.UserSearchesController.all().url());
        String jwt = JwtTestUtils.createToken(userId, config);
        JwtTestUtils.addJwtTokenTo(request, jwt);

        return route(application, request);
    }

    public Result patch(String queryStr, Long userId, Long id){
        JsonNode json = Json.parse(queryStr);
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(PATCH)
                .bodyJson(json)
                .uri(routes.UserSearchesController.patch(id).url());
        String jwt = JwtTestUtils.createToken(userId, config);
        JwtTestUtils.addJwtTokenTo(request, jwt);

        return route(application, request);
    }

    public Result single(Long userId, Long id){
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri(routes.UserSearchesController.single(id).url());
        String jwt = JwtTestUtils.createToken(userId, config);
        JwtTestUtils.addJwtTokenTo(request, jwt);

        return route(application, request);
    }

    public Result delete(Long userId, Long id) {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(DELETE)
                .uri(routes.UserSearchesController.delete(id).url());
        String jwt = JwtTestUtils.createToken(userId, config);
        JwtTestUtils.addJwtTokenTo(request, jwt);

        return route(application, request);
    }
}
