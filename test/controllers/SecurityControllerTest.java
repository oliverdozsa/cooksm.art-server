package controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.database.rider.core.api.dataset.DataSet;
import controllers.v1.routes;
import data.entities.FavoriteRecipe;
import data.entities.RecipeSearch;
import data.entities.UserSearch;
import lombokized.dto.UserSocialLoginDto;
import io.ebean.Ebean;
import lombokized.security.VerifiedFacebookUserInfo;
import lombokized.security.VerifiedGoogleUserInfo;
import lombokized.security.VerifiedUserInfo;
import data.entities.User;
import org.junit.Rule;
import org.junit.Test;
import play.Logger;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import rules.PlayApplicationWithGuiceDbRiderRule;
import security.SocialTokenVerifier;
import utils.JwtTestUtils;
import utils.MockSocialTokenVerifier;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.ebean.Expr.eq;
import static junit.framework.TestCase.*;
import static play.inject.Bindings.bind;
import static play.mvc.Http.HttpVerbs.POST;
import static play.mvc.Http.Status.*;
import static play.test.Helpers.*;

public class SecurityControllerTest {
    @Rule
    public PlayApplicationWithGuiceDbRiderRule application;

    private static final Logger.ALogger logger = Logger.of(SecurityControllerTest.class);

    public SecurityControllerTest() {
        application = new PlayApplicationWithGuiceDbRiderRule(
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

        UserSocialLoginDto dto = new UserSocialLoginDto("SomeRandomGoogleToken");
        String email = "some@one.com";

        Optional<User> entityOpt = Ebean.createQuery(User.class)
                .where()
                .eq("email", email)
                .findOneOrEmpty();
        assertFalse("Entity shouldn't be present!", entityOpt.isPresent());

        VerifiedUserInfo mockResult = new VerifiedGoogleUserInfo("Some One", email, "4242");
        MockSocialTokenVerifier.setMockResult(mockResult);
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
                .eq("email", email)
                .findOneOrEmpty();
        assertTrue("Entity should be present!", entityOpt.isPresent());
    }

    @Test
    @DataSet(value = "datasets/yml/security.yml", disableConstraints = true, cleanBefore = true)
    public void testLoginThroughFacebook_OK_UserExists() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testLoginThroughFacebook_OK_UserExists");
        logger.info("------------------------------------------------------------------------------------------------");

        UserSocialLoginDto dto = new UserSocialLoginDto("SomeRandomGoogleToken");
        String email = "user1@example.com";

        Optional<User> entityOpt = Ebean.createQuery(User.class)
                .where()
                .eq("email", email)
                .findOneOrEmpty();
        assertTrue("Entity should be present!", entityOpt.isPresent());
        assertEquals("User name is wrong!", "John Doe", entityOpt.get().getFullName());

        VerifiedUserInfo userInfo = new VerifiedFacebookUserInfo("John Doe Jack", email, "2424");
        MockSocialTokenVerifier.setMockResult(userInfo);
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
                .eq("email", email)
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

        UserSocialLoginDto dto = new UserSocialLoginDto("SomeRandomGoogleToken");

        VerifiedUserInfo mockResult = new VerifiedGoogleUserInfo("Some One", "some@one.com", "4242");
        MockSocialTokenVerifier.setMockResult(mockResult);
        MockSocialTokenVerifier.shouldThrowException(true);
        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(POST)
                .uri(routes.SecurityController.loginThroughGoogle().url())
                .bodyJson(Json.toJson(dto));
        Result result = route(application.getApplication(), httpRequest);
        assertEquals("Result of request is wrong!", UNAUTHORIZED, result.status());
        MockSocialTokenVerifier.shouldThrowException(false);
    }


    @Test
    @DataSet(value = "datasets/yml/security.yml", disableConstraints = true, cleanBefore = true)
    public void testLoginThroughGoogle_BadRequest() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testLoginThroughGoogle_BadRequest");
        logger.info("------------------------------------------------------------------------------------------------");

        UserSocialLoginDto dto = new UserSocialLoginDto(null);

        MockSocialTokenVerifier.setMockResult(null);
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
        UserSocialLoginDto dto = new UserSocialLoginDto("someRandomGoogleToken");

        VerifiedUserInfo mockResult = new VerifiedGoogleUserInfo("Some One", "some@one.com", "4242");
        MockSocialTokenVerifier.setMockResult(mockResult);
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

    @Test
    @DataSet(value = "datasets/yml/delete-registration.yml", disableConstraints = true, cleanBefore = true)
    public void testDeleteRegistration() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testDeleteRegistration");
        logger.info("------------------------------------------------------------------------------------------------");

        List<FavoriteRecipe> favoriteRecipesBeforeDelete = Ebean.createQuery(FavoriteRecipe.class)
                .where(eq("user.id", 1L))
                .findList();
        assertEquals("Favorite recipes before delete is wrong!", 2, favoriteRecipesBeforeDelete.size());

        List<UserSearch> userSearchesBeforeDelete = Ebean.createQuery(UserSearch.class)
                .where(eq("user.id", 1L))
                .findList();
        List<RecipeSearch> recipesSearchesOfUserSearch = userSearchesBeforeDelete.stream()
                .map(UserSearch::getSearch)
                .collect(Collectors.toList());
        assertEquals("User searches before delete is wrong!",2, userSearchesBeforeDelete.size());
        assertEquals("Recipe searches of user before delete is wrong!",2, recipesSearchesOfUserSearch.size());

        User user = Ebean.find(User.class, 1L);
        assertNotNull("User to delete not found!", user);

        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(DELETE)
                .uri(routes.SecurityController.deregister().url());
        String jwt = JwtTestUtils.createToken(4000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);

        Result result = route(application.getApplication(), httpRequest);
        assertEquals("Failed to delete user!", NO_CONTENT, result.status());

        List<FavoriteRecipe> favoriteRecipesAfterDelete = Ebean.createQuery(FavoriteRecipe.class)
                .where(eq("user.id", 1L))
                .findList();
        assertEquals("Favorite recipes still exist after deletion!", 0, favoriteRecipesAfterDelete.size());

        List<UserSearch> userSearchesAfterDelete = Ebean.createQuery(UserSearch.class)
                .where(eq("user.id", 1L))
                .findList();
        assertEquals("User searches still exist after deletion!",0, userSearchesAfterDelete.size());

        List<Long> recipeSearchIdsInDbAfterDelete = recipesSearchesOfUserSearch.stream()
                .map(r -> Ebean.find(RecipeSearch.class, r.getId()))
                .filter(Objects::nonNull)
                .map(RecipeSearch::getId)
                .collect(Collectors.toList());
        assertEquals("Recipe searches left in DB after delete profile!",0, recipeSearchIdsInDbAfterDelete.size());

        user = Ebean.find(User.class, 1L);
        assertNull("Deleted user still exists!", user);
    }
}
