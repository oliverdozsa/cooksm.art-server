package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.database.rider.core.api.dataset.DataSet;
import controllers.v1.routes;
import dto.UserSocialLoginDto;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import play.Logger;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import rules.PlayApplicationWithGuiceDbRider;
import security.JwtValidator;
import security.SocialTokenVerifier;
import security.imp.JwtValidatorImp;
import utils.MockSocialTokenVerifier;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static play.inject.Bindings.bind;
import static play.mvc.Http.HttpVerbs.POST;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.route;

public class SecurityControllerTest {
    @Rule
    public PlayApplicationWithGuiceDbRider application;
    private JwtValidator validator;

    private static final Logger.ALogger logger = Logger.of(IngredientNamesControllerTest.class);

    public SecurityControllerTest() {
        application = new PlayApplicationWithGuiceDbRider(
                new GuiceApplicationBuilder()
                        .overrides(bind(SocialTokenVerifier.class).qualifiedWith("Google").to(MockSocialTokenVerifier.class))
                        .overrides(bind(SocialTokenVerifier.class).qualifiedWith("Facebook").to(MockSocialTokenVerifier.class))
        );
    }

    @Before
    public void setup() {
        validator = new JwtValidatorImp(application.getApplication().config());
    }

    @Test
    @DataSet(value = "datasets/yml/security.yml", disableConstraints = true, cleanBefore = true)
    public void testLoginThroughGToken_OK_UserCreated() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testLoginThroughGToken_OK");
        logger.info("------------------------------------------------------------------------------------------------");

        UserSocialLoginDto dto = new UserSocialLoginDto(
                "John Doe",
                "mail@example.com",
                "SomeRandomGoogleToken"
        );

        // TODO: Check db for user not existing

        MockSocialTokenVerifier.setMockResult(true);
        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(POST)
                .uri(routes.SecurityController.loginThroughGoogle().url())
                .bodyJson(Json.toJson(dto));
        Result result = route(application.getApplication(), httpRequest);
        assertEquals(OK, result.status());

        JsonNode resultJson = Json.parse(contentAsString(result));
        assertNotNull(resultJson.get("token"));
        // TODO: Check db for user
    }

    /*
    // TODO: test for login through facebook
    // TODO: test for login with updated data


    @Test
    public void testLoginThroughGToken_OK_UserExists() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testLoginThroughGToken_OK_UserExists");
        logger.info("------------------------------------------------------------------------------------------------");

        SecurityController.UserData userData = new SecurityController.UserData();
        userData.fullName = "Some, One";
        userData.email = "bla@bal.bla";
        userData.socialToken = "SomeRandomToken";

        new User(userData.email, "some", userData.fullName).save();
        assertNotNull(User.findByEmail(userData.email));

        socialTokenMockResult = true;
        Http.RequestBuilder httpRequest = fakeRequest(routes.SecurityController.loginThroughGToken()).bodyJson(Json.toJson(userData));
        Result respResult = route(app, httpRequest);
        assertEquals(OK, respResult.status());

        JsonNode respJson = Json.parse(contentAsString(respResult));
        assertNotNull(respJson.get(AUTH_TOKEN));
    }

    @Test
    public void testLoginThroughGToken_Unauthorized() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testLoginThroughGToken_Unauthorized");
        logger.info("------------------------------------------------------------------------------------------------");

        SecurityController.UserData userData = new SecurityController.UserData();
        userData.fullName = "Some, One";
        userData.email = "bla@bal.bla";
        userData.socialToken = "SomeRandomToken";
        assertNull(User.findByEmail(userData.email));

        socialTokenMockResult = false;
        Http.RequestBuilder httpRequest = fakeRequest(routes.SecurityController.loginThroughGToken()).bodyJson(Json.toJson(userData));
        Result respResult = route(app, httpRequest);
        assertEquals(UNAUTHORIZED, respResult.status());
    }

    @Test
    public void testLoginThroughGToken_BadRequest() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testLoginThroughGToken_BadRequest");
        logger.info("------------------------------------------------------------------------------------------------");

        SecurityController.UserData userData = new SecurityController.UserData();
        userData.fullName = "Some, One";
        userData.email = "bla@bal.bla";
        userData.socialToken = null;
        assertNull(User.findByEmail(userData.email));

        socialTokenMockResult = true;
        Http.RequestBuilder httpRequest = fakeRequest(routes.SecurityController.loginThroughGToken()).bodyJson(Json.toJson(userData));
        Result respResult = route(app, httpRequest);
        assertEquals(BAD_REQUEST, respResult.status());
    }

    @Test
    public void testRenewToken_OK() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testRenewToken_OK");
        logger.info("------------------------------------------------------------------------------------------------");

        // Get token
        SecurityController.UserData userData = new SecurityController.UserData();
        userData.fullName = "Some, One";
        userData.email = "bla@bal.bla";
        userData.socialToken = "SomeRandomToken";

        socialTokenMockResult = true;
        Http.RequestBuilder httpRequest = fakeRequest(routes.SecurityController.loginThroughGToken()).bodyJson(Json.toJson(userData));
        Result respResult = route(app, httpRequest);
        assertEquals(OK, respResult.status());

        JsonNode respJson = Json.parse(contentAsString(respResult));
        JsonNode tokenJson = respJson.get(AUTH_TOKEN);

        httpRequest = fakeRequest(routes.SecurityController.renewToken());
        TestUtils.addJwtTokenToRequest(httpRequest, tokenJson.asText());
        respResult = route(app, httpRequest);
        respJson = Json.parse(contentAsString(respResult));
        assertEquals(OK, respResult.status());
        assertNotNull(respJson.get(AUTH_TOKEN));
    }

    @Test
    public void testRenewToken_InvalidToken() throws UnsupportedEncodingException {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testRenewToken_InvalidToken");
        logger.info("------------------------------------------------------------------------------------------------");

        String secret = guiceApp.config().getString("play.http.secret.key");
        Date past = Date.from(ZonedDateTime.now(ZoneId.systemDefault()).minusMinutes(70).toInstant());
        Algorithm algorithm = Algorithm.HMAC256(secret);
        String tokenPast = JWT.create()
                .withIssuer(ISSUER)
                .withClaim("user_id", 3L)
                .withExpiresAt(past)
                .sign(algorithm);

        Http.RequestBuilder httpRequest = fakeRequest(routes.SecurityController.renewToken());
        TestUtils.addJwtTokenToRequest(httpRequest, tokenPast);
        Result respResult = route(app, httpRequest);
        assertEquals(FORBIDDEN, respResult.status());
    }

    // TODO: test token expires (request any op with filter, with expired token)

    private static class MockRecaptchaTokenVerifier implements RecaptchaTokenVerifier {
        @Override
        public boolean verifyToken(String token) {
            return recaptchaMockResult;
        }
    }

    private static class MockSocialTokeVerifier implements SocialTokenVerifier {

        @Override
        public boolean verifyToken(String token) {
            return socialTokenMockResult;
        }
    }
    */
}
