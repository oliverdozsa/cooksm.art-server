package clients;

import controllers.v1.routes;
import dto.IngredientTagCreateUpdateDto;
import play.Application;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import utils.JwtTestUtils;

import static play.test.Helpers.*;

public class IngredientTagsTestClient {
    private final Application application;

    public IngredientTagsTestClient(Application application) {
        this.application = application;
    }

    public Result page(String queryParams) {
        Http.RequestBuilder request = createPageRequest(queryParams);
        return route(application, request);
    }

    public Result authorizedPage(Long userId, String queryParams) {
        Http.RequestBuilder request = createPageRequest(queryParams);
        String jwt = JwtTestUtils.createToken(userId, application.config());
        JwtTestUtils.addJwtTokenTo(request, jwt);

        return route(application, request);
    }

    public Result single(Long userId, Long id, Long languageId) {
        Http.RequestBuilder request = new Http.RequestBuilder().method(GET)
                .uri(routes.IngredientTagsController.single(id, languageId).url());
        String jwt = JwtTestUtils.createToken(userId, application.config());
        JwtTestUtils.addJwtTokenTo(request, jwt);

        return route(application, request);
    }

    public Result create(IngredientTagCreateUpdateDto dto, Long userId) {
        Http.RequestBuilder request = new Http.RequestBuilder().method(POST)
                .bodyJson(Json.toJson(dto))
                .uri(routes.IngredientTagsController.create().url());
        String jwt = JwtTestUtils.createToken(userId, application.config());
        JwtTestUtils.addJwtTokenTo(request, jwt);

        return route(application, request);
    }

    public Result byLocation(String url, Long userId) {
        Http.RequestBuilder request = new Http.RequestBuilder().method(GET)
                .uri(url);
        String jwt = JwtTestUtils.createToken(userId, application.config());
        JwtTestUtils.addJwtTokenTo(request, jwt);

        return route(application, request);
    }

    private Http.RequestBuilder createPageRequest(String queryParams) {
        return new Http.RequestBuilder().method(GET)
                .uri(routes.IngredientTagsController.page().url() + "?" + queryParams);
    }
}
