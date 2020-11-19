package clients;

import controllers.v1.routes;
import play.Application;
import play.mvc.Http;
import play.mvc.Result;

import static play.mvc.Http.HttpVerbs.GET;
import static play.test.Helpers.route;

public class IngredientNamesTestClient {
    private final Application application;

    public IngredientNamesTestClient(Application application) {
        this.application = application;
    }

    public Result page(String queryParams) {
        Http.RequestBuilder request = new Http.RequestBuilder().method(GET).uri(
                routes.IngredientNamesController.pageNames().url() + "?" + queryParams);

        return route(application, request);
    }
}
