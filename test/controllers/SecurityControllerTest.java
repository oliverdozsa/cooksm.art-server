package controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.database.rider.core.api.dataset.DataSet;
import controllers.v1.routes;
import lombokized.dto.UserSocialLoginDto;
import io.ebean.Ebean;
import models.entities.User;
import org.junit.Rule;
import org.junit.Test;
import play.Logger;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import rules.PlayApplicationWithGuiceDbRider;
import security.SocialTokenVerifier;
import utils.JwtTestUtils;
import utils.MockSocialTokenVerifier;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

import static junit.framework.TestCase.*;
import static play.inject.Bindings.bind;
import static play.mvc.Http.HttpVerbs.POST;
import static play.mvc.Http.Status.*;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.route;

public class SecurityControllerTest {
    @Rule
    public PlayApplicationWithGuiceDbRider application;

    private static final Logger.ALogger logger = Logger.of(IngredientNamesControllerTest.class);

    public SecurityControllerTest() {
        application = new PlayApplicationWithGuiceDbRider(
                new GuiceApplicationBuilder()
                        .overrides(bind(SocialTokenVerifier.class).qualifiedWith("Google").to(MockSocialTokenVerifier.class))
                        .overrides(bind(SocialTokenVerifier.class).qualifiedWith("Facebook").to(MockSocialTokenVerifier.class))
        );
    }

    @Test
    @DataSet(value = "datasets/yml/security.yml", disableConstraints = true, cleanBefore = true)
    public void testLoginThroughGoogle_OK_UserCreated() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testLoginThroughGoogle_OK_UserCreated");
        logger.info("------------------------------------------------------------------------------------------------");

        UserSocialLoginDto dto = new UserSocialLoginDto(
                "John Doe",
                "mail@example.com",
                "SomeRandomGoogleToken"
        );

        Optional<User> entityOpt = Ebean.createQuery(User.class)
                .where()
                .eq("email", dto.getEmail())
                .findOneOrEmpty();
        assertFalse("Entity shouldn't be present!", entityOpt.isPresent());

        MockSocialTokenVerifier.setMockResult(true);
        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(POST)
                .uri(routes.SecurityController.loginThroughGoogle().url())
                .bodyJson(Json.toJson(dto));
        Result result = route(application.getApplication(), httpRequest);
        assertEquals("Result of request is wrong!", OK, result.status());

        JsonNode resultJson = Json.parse(contentAsString(result));
        assertNotNull("JWT is missing!", resultJson.get("jwtAuthToken"));

        entityOpt = Ebean.createQuery(User.class)
                .where()
                .eq("email", dto.getEmail())
                .findOneOrEmpty();
        assertTrue("Entity should be present!", entityOpt.isPresent());
    }

    @Test
    @DataSet(value = "datasets/yml/security.yml", disableConstraints = true, cleanBefore = true)
    public void testLoginThroughFacebook_OK_UserExists() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testLoginThroughFacebook_OK_UserExists");
        logger.info("------------------------------------------------------------------------------------------------");

        UserSocialLoginDto dto = new UserSocialLoginDto(
                "John Doe Jack",
                "user1@example.com",
                "SomeRandomGoogleToken"
        );

        Optional<User> entityOpt = Ebean.createQuery(User.class)
                .where()
                .eq("email", dto.getEmail())
                .findOneOrEmpty();
        assertTrue("Entity should be present!", entityOpt.isPresent());
        assertEquals("User name is wrong!", "John Doe", entityOpt.get().getFullName());

        MockSocialTokenVerifier.setMockResult(true);
        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(POST)
                .uri(routes.SecurityController.loginThroughFacebook().url())
                .bodyJson(Json.toJson(dto));
        Result result = route(application.getApplication(), httpRequest);
        assertEquals("Result of request is wrong!", OK, result.status());

        JsonNode resultJson = Json.parse(contentAsString(result));
        assertNotNull("JWT is missing!", resultJson.get("jwtAuthToken"));

        entityOpt = Ebean.createQuery(User.class)
                .where()
                .eq("email", dto.getEmail())
                .findOneOrEmpty();
        assertTrue("Entity should be present!", entityOpt.isPresent());
        assertEquals("User name is wrong!", "John Doe Jack", entityOpt.get().getFullName());
    }

    @Test
    @DataSet(value = "datasets/yml/security.yml", disableConstraints = true, cleanBefore = true)
    public void testLoginThroughGoogle_Unauthorized() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testLoginThroughGoogle_Unauthorized");
        logger.info("------------------------------------------------------------------------------------------------");

        UserSocialLoginDto dto = new UserSocialLoginDto(
                "John Doe",
                "mail@example.com",
                "SomeRandomGoogleToken"
        );

        MockSocialTokenVerifier.setMockResult(false);
        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(POST)
                .uri(routes.SecurityController.loginThroughGoogle().url())
                .bodyJson(Json.toJson(dto));
        Result result = route(application.getApplication(), httpRequest);
        assertEquals("Result of request is wrong!", UNAUTHORIZED, result.status());
    }


    @Test
    @DataSet(value = "datasets/yml/security.yml", disableConstraints = true, cleanBefore = true)
    public void testLoginThroughGoogle_BadRequest() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testLoginThroughGoogle_BadRequest");
        logger.info("------------------------------------------------------------------------------------------------");

        UserSocialLoginDto dto = new UserSocialLoginDto(
                "John Doe",
                "mail@example.com",
                null
        );

        MockSocialTokenVerifier.setMockResult(false);
        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(POST)
                .uri(routes.SecurityController.loginThroughGoogle().url())
                .bodyJson(Json.toJson(dto));
        Result result = route(application.getApplication(), httpRequest);
        assertEquals("Result of request is wrong!", BAD_REQUEST, result.status());
    }

    @Test
    @DataSet(value = "datasets/yml/security.yml", disableConstraints = true, cleanBefore = true)
    public void testRenewToken_OK() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testRenewToken_OK");
        logger.info("------------------------------------------------------------------------------------------------");

        // Get token
        UserSocialLoginDto dto = new UserSocialLoginDto(
                "John Doe",
                "mail@example.com",
                "someRandomGoogleToken"
        );

        MockSocialTokenVerifier.setMockResult(true);
        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(POST)
                .uri(routes.SecurityController.loginThroughGoogle().url())
                .bodyJson(Json.toJson(dto));
        Result result = route(application.getApplication(), httpRequest);
        assertEquals("Result of request is wrong!", OK, result.status());

        JsonNode resultJson = Json.parse(contentAsString(result));
        String jwt = resultJson.get("jwtAuthToken").asText();

        // Renew it
        httpRequest = new Http.RequestBuilder()
                .method(POST)
                .uri(routes.SecurityController.renew().url());

        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);

        result = route(application.getApplication(), httpRequest);
        assertEquals("Result of request is wrong!", OK, result.status());
        resultJson = Json.parse(contentAsString(result));
        assertNotNull("JWT is missing!", resultJson.get("jwtAuthToken"));
    }

    @Test
    @DataSet(value = "datasets/yml/security.yml", disableConstraints = true, cleanBefore = true)
    public void testRenewToken_InvalidToken() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testRenewToken_InvalidToken");
        logger.info("------------------------------------------------------------------------------------------------");

        String secret = application.getApplication().config().getString("play.http.secret.key");
        String issuer = application.getApplication().config().getString("receptnekem.jwt.issuer");
        Date past = Date.from(Instant.now().minus(70, ChronoUnit.MINUTES));
        Algorithm algorithm = Algorithm.HMAC256(secret);
        String tokenPast = JWT.create()
                .withIssuer(issuer)
                .withClaim("user_id", 3L)
                .withExpiresAt(past)
                .sign(algorithm);

        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(POST)
                .uri(routes.SecurityController.renew().url());

        JwtTestUtils.addJwtTokenTo(httpRequest, tokenPast);

        Result result = route(application.getApplication(), httpRequest);
        assertEquals("Result of request is wrong!", FORBIDDEN, result.status());
    }
}
