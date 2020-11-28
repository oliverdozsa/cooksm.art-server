package clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import controllers.v1.routes;
import play.Application;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import utils.Base62Utils;
import utils.JwtTestUtils;

import static play.mvc.Http.HttpVerbs.GET;
import static play.mvc.Http.HttpVerbs.POST;
import static play.test.Helpers.route;

public class RecipeSearchesTestClient {
    private final Application application;
    private final Config config;

    public RecipeSearchesTestClient(Application application) {
        this.application = application;
        this.config = application.config();
    }

    public Result single(Long id) {
        String encodedId = Base62Utils.encode(id);
        return single(encodedId);
    }

    public Result single(String id) {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri(routes.RecipeSearchesController.single(id).url());

        return route(application, request);
    }

    public Result create(String searchJsonStr) {
        Http.RequestBuilder request = createPostRequest(searchJsonStr);
        return route(application, request);
    }

    public Result create(String searchJsonStr, Long userId) {
        Http.RequestBuilder request = createPostRequest(searchJsonStr);
        String jwt = JwtTestUtils.createToken(userId, config);
        JwtTestUtils.addJwtTokenTo(request, jwt);

        return route(application, request);
    }

    public Result byLocation(String location) {
        Http.RequestBuilder httpGetRequest = new Http.RequestBuilder()
                .method(GET)
                .uri(location);

        return route(application, httpGetRequest);
    }

    private Http.RequestBuilder createPostRequest(String searchJsonStr) {
        JsonNode json = Json.parse(searchJsonStr);
        return new Http.RequestBuilder()
                .method(POST)
                .bodyJson(json)
                .uri(routes.RecipeSearchesController.create().url());
    }
}
