package clients;

import com.typesafe.config.Config;
import controllers.v1.routes;
import dto.RecipeBookCreateUpdateDto;
import play.Application;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import utils.JwtTestUtils;

import static play.mvc.Http.HttpVerbs.GET;
import static play.mvc.Http.HttpVerbs.POST;
import static play.test.Helpers.route;

public class RecipeBooksTestClient {
    private final Application application;
    private final Config config;

    public RecipeBooksTestClient(Application application) {
        this.application = application;
        config = application.config();
    }

    public Result create(RecipeBookCreateUpdateDto dto, Long userId) {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(Json.toJson(dto))
                .uri(routes.RecipeBooksController.create().url());

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
