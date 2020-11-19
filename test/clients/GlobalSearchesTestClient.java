package clients;

import controllers.v1.routes;
import play.Application;
import play.mvc.Http;
import play.mvc.Result;

import static play.test.Helpers.GET;
import static play.test.Helpers.route;

public class GlobalSearchesTestClient {
    private final Application application;

    public GlobalSearchesTestClient(Application application) {
        this.application = application;
    }

    public Result all() {
        Http.RequestBuilder request = new Http.RequestBuilder().method(GET)
                .uri(routes.GlobalSearchesController.all().url());

        return route(application, request);
    }
}
