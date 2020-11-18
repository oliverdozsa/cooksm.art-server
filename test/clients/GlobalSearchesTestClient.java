package clients;

import com.typesafe.config.Config;
import controllers.v1.routes;
import play.Application;
import play.mvc.Http;
import play.mvc.Result;

import static play.test.Helpers.GET;
import static play.test.Helpers.route;

public class GlobalSearchesTestClient {
    private Application application;
    private Config config;

    public GlobalSearchesTestClient(Application application) {
        this.application = application;
        this.config = application.config();
    }

    public Result all() {
        Http.RequestBuilder request = new Http.RequestBuilder().method(GET)
                .uri(routes.GlobalSearchesController.all().url());

        return route(application, request);
    }
}
