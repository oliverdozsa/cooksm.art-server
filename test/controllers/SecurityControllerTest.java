package controllers;

import org.junit.Rule;
import play.Logger;
import play.inject.guice.GuiceApplicationBuilder;
import rules.PlayApplicationWithGuiceDbRider;
import security.SocialTokenVerifier;
import utils.MockSocialTokenVerifier;

import static play.inject.Bindings.bind;

public class SecurityControllerTest {
    @Rule
    public PlayApplicationWithGuiceDbRider application = new PlayApplicationWithGuiceDbRider(
            new GuiceApplicationBuilder()
            .overrides(bind(SocialTokenVerifier.class).qualifiedWith("Google").to(MockSocialTokenVerifier.class))
            .overrides(bind(SocialTokenVerifier.class).qualifiedWith("Facebook").to(MockSocialTokenVerifier.class))
    );

    private static final Logger.ALogger logger = Logger.of(IngredientNamesControllerTest.class);
    private static final String RESOURCE_PATH = "/v1/security";

    // TODO

    /*
    private String testDbUrl;
    private Application guiceApp;
    private JwtValidator validator;

    private static boolean recaptchaMockResult = true;
    private static boolean socialTokenMockResult = true;

    public SecurityControllerTests() throws UnsupportedEncodingException {
        guiceApp = new GuiceApplicationBuilder()
                .overrides(bind(RecaptchaTokenVerifier.class).to(MockRecaptchaTokenVerifier.class))
                .overrides(bind(SocialTokenVerifier.class).qualifiedWith("GoogleSocialTokenVerifier").to(MockSocialTokeVerifier.class))
                .build();

        testDbUrl = guiceApp.config().getString("db.default.url");
        validator = new JwtValidatorImpl(guiceApp.config());
    }

    @Before
    public void setUp() {
        try {
            logger.info("running test DB setup on " + testDbUrl + "...");
            TestUtils.runSql(testDbUrl, "test/resources/test_db_create.sql");
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }



    @Test
    public void testLoginThroughGToken_OK_UserCreated() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testLoginThroughGToken_OK");
        logger.info("------------------------------------------------------------------------------------------------");

        SecurityController.UserData userData = new SecurityController.UserData();
        userData.fullName = "Some, One";
        userData.email = "bla@bal.bla";
        userData.socialToken = "SomeRandomToken";
        assertNull(User.findByEmail(userData.email));

        socialTokenMockResult = true;
        Http.RequestBuilder httpRequest = fakeRequest(routes.SecurityController.loginThroughGToken()).bodyJson(Json.toJson(userData));
        Result respResult = route(app, httpRequest);
        assertEquals(OK, respResult.status());

        JsonNode respJson = Json.parse(contentAsString(respResult));
        assertNotNull(respJson.get(AUTH_TOKEN));
        assertNotNull(User.findByEmail(userData.email));
    }

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
