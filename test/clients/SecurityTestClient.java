package clients;

import com.typesafe.config.Config;
import controllers.v1.routes;
import lombokized.dto.UserSocialLoginDto;
import play.Application;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import utils.JwtTestUtils;

import static play.mvc.Http.HttpVerbs.POST;
import static play.test.Helpers.DELETE;
import static play.test.Helpers.route;

public class SecurityTestClient {
    private final Application application;
    private Config config;

    public SecurityTestClient(Application application) {
        this.application = application;
        this.config = application.config();
    }

    public Result loginThroughGoogle(String socialToken) {
        return loginThroughSocialUrl(socialToken, routes.SecurityController.loginThroughGoogle().url());
    }

    public Result loginThroughFacebook(String socialToken) {
        return loginThroughSocialUrl(socialToken, routes.SecurityController.loginThroughFacebook().url());
    }

    public Result renew(String jwt) {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(POST)
                .uri(routes.SecurityController.renew().url());
        JwtTestUtils.addJwtTokenTo(request, jwt);

        return route(application, request);
    }

    public Result deregister(Long userId) {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(DELETE)
                .uri(routes.SecurityController.deregister().url());
        String jwt = JwtTestUtils.createToken(userId, config);
        JwtTestUtils.addJwtTokenTo(request, jwt);

        return route(application, request);
    }

    private Result loginThroughSocialUrl(String socialToken, String url) {
        UserSocialLoginDto dto = new UserSocialLoginDto(socialToken);
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(POST)
                .uri(url)
                .bodyJson(Json.toJson(dto));

        return route(application, request);
    }
}
