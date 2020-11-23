package clients;

import com.typesafe.config.Config;
import controllers.v1.routes;
import play.Application;
import play.mvc.Http;
import play.mvc.Result;
import utils.JwtTestUtils;

import static play.test.Helpers.GET;
import static play.test.Helpers.route;

public class RecipesTestClient {
    private final Application application;
    private final Config config;

    public RecipesTestClient(Application application) {
        this.application = application;
        this.config = application.config();
    }

    public Result page(String queryParams) {
        Http.RequestBuilder request = createPageRequest(queryParams);
        return route(application, request);
    }

    public Result page(String queryParams, Long userId) {
        Http.RequestBuilder request = createPageRequest(queryParams);
        String jwt = JwtTestUtils.createToken(userId, config);
        JwtTestUtils.addJwtTokenTo(request, jwt);

        return route(application, request);
    }

    private Http.RequestBuilder createPageRequest(String queryParams) {
        return new Http.RequestBuilder().method(GET)
                .uri(routes.RecipesController.pageRecipes().url() + "?" + queryParams);
    }
}
