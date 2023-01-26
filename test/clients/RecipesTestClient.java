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

    public Result page() {
        Http.RequestBuilder request = createPageRequest(null);
        return route(application, request);
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

    public Result single(Long id, Long languageId) {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri(routes.RecipesController.singleRecipe(id, languageId).url());

        return route(application, request);
    }

    public Result recipeBooksOf(Long id, Long userId) {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri(routes.RecipesController.recipeBooksOf(id).url());

        String jwt = JwtTestUtils.createToken(userId, application.config());
        JwtTestUtils.addJwtTokenTo(request, jwt);

        return route(application, request);
    }

    private Http.RequestBuilder createPageRequest(String queryParams) {
        String queryParamsToAppend = queryParams == null ? "" : queryParams;
        if(!queryParamsToAppend.equals("")){
            queryParamsToAppend = "?" + queryParamsToAppend;
        }

        return new Http.RequestBuilder().method(GET)
                .uri(routes.RecipesController.pageRecipes().url() + queryParamsToAppend);
    }
}
