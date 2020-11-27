package controllers;

import clients.SecurityTestClient;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.github.database.rider.core.api.dataset.DataSet;
import data.entities.FavoriteRecipe;
import data.entities.RecipeSearch;
import data.entities.UserSearch;
import io.ebean.Ebean;
import lombokized.security.VerifiedGoogleUserInfo;
import lombokized.security.VerifiedUserInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Result;
import rules.RuleChainForTests;
import security.SocialTokenVerifier;
import utils.MockSocialTokenVerifier;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static extractors.DataFromResult.jwtOf;
import static extractors.DataFromResult.statusOf;
import static io.ebean.Expr.eq;
import static junit.framework.TestCase.assertTrue;
import static matchers.ResultHasJwt.hasJwt;
import static matchers.UserExistsInDb.existsInDb;
import static matchers.UserHasName.hasName;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static play.inject.Bindings.bind;
import static play.mvc.Http.Status.*;

public class SecurityControllerTest {
    @Rule
    public RuleChain chain;

    private SecurityTestClient client;
    private final RuleChainForTests ruleChainForTests;

    public SecurityControllerTest() {
        GuiceApplicationBuilder appBuilder = new GuiceApplicationBuilder()
                .overrides(bind(SocialTokenVerifier.class).qualifiedWith("Google").to(MockSocialTokenVerifier.class))
                .overrides(bind(SocialTokenVerifier.class).qualifiedWith("Facebook").to(MockSocialTokenVerifier.class));
        ruleChainForTests = new RuleChainForTests(appBuilder);
        chain = ruleChainForTests.getRuleChain();
    }

    @Before
    public void setUp() {
        client = new SecurityTestClient(ruleChainForTests.getApplication());
    }

    @Test
    @DataSet(value = "datasets/yml/security.yml", disableConstraints = true, cleanBefore = true)
    public void testLoginThroughGoogle_OK_UserCreated() {
        // Given
        assertThat("some@one", not(existsInDb()));

        // When
        mockVerificationWillSucceedFor("Some One", "some@one", "4242");
        Result result = client.loginThroughGoogle("SomeRandomGoogleToken");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat("some@one", existsInDb());
        assertThat(result, hasJwt());
    }

    @Test
    @DataSet(value = "datasets/yml/security.yml", disableConstraints = true, cleanBefore = true)
    public void testLoginThroughFacebook_OK_UserExists_NameChanges() {
        // Given
        assertThat("user1@example.com", existsInDb());
        assertThat("user1@example.com", hasName("John Doe"));

        // When
        mockVerificationWillSucceedFor("John Doe Jack", "user1@example.com", "2424");
        Result result = client.loginThroughFacebook("SomeRandomGoogleToken");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat("user1@example.com", existsInDb());
        assertThat("user1@example.com", hasName("John Doe Jack"));
        assertThat(result, hasJwt());
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/security.yml", disableConstraints = true, cleanBefore = true)
    public void testLoginThroughGoogle_Unauthorized() {
        // When
        mockVerificationWillFailFor("Some One", "some@one.com", "4242");
        Result result = client.loginThroughGoogle("SomeRandomGoogleToken");

        // Then
        assertThat(statusOf(result), equalTo(UNAUTHORIZED));
    }


    @Test
    // Given
    @DataSet(value = "datasets/yml/security.yml", disableConstraints = true, cleanBefore = true)
    public void testLoginThroughGoogle_BadRequest() {
        // When
        Result result = client.loginThroughFacebook(null);

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/security.yml", disableConstraints = true, cleanBefore = true)
    public void testRenewToken_OK() {
        // When
        mockVerificationWillSucceedFor("John Doe Jack", "user1@example.com", "2424");
        Result result = client.loginThroughFacebook("SomeRandomGoogleToken");

        assertThat(statusOf(result), equalTo(OK));

        String jwt = jwtOf(result);
        result = client.renew(jwt);

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(result, hasJwt());
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/security.yml", disableConstraints = true, cleanBefore = true)
    public void testRenewToken_InvalidToken() {
        // When
        Result result = client.renew(createExpiredJwt());

        // Then
        assertThat(statusOf(result), equalTo(FORBIDDEN));
    }

    @Test
    @DataSet(value = "datasets/yml/delete-registration.yml", disableConstraints = true, cleanBefore = true)
    public void testDeleteRegistration() {
        // Given
        assertThat(favoriteRecipesOfUserInDb(1L), hasSize(2));

        List<UserSearch> userSearches = searchesOfUserInDb(1L);
        assertThat(userSearches, hasSize(2));

        List<RecipeSearch> recipeSearches = recipeSearchesOfUserSearches(userSearches);
        assertThat(recipeSearches, hasSize(2));

        assertThat("user1@example.com", existsInDb());

        // When
        Result result = client.deregister(1L);

        // Then
        assertThat(statusOf(result), equalTo(NO_CONTENT));
        assertThat(favoriteRecipesOfUserInDb(1L), hasSize(0));
        assertThat(searchesOfUserInDb(1L), hasSize(0));
        assertTrue(areRecipeSearchesDeleted(recipeSearches));
        assertThat("user1@example.com", not(existsInDb()));
    }

    private void mockVerificationWillSucceedFor(String fullName, String email, String socialId) {
        VerifiedUserInfo mockResult = new VerifiedGoogleUserInfo(fullName, email, socialId);
        MockSocialTokenVerifier.setMockResult(mockResult);
        MockSocialTokenVerifier.shouldThrowException(false);
    }

    private void mockVerificationWillFailFor(String fullName, String email, String socialId) {
        VerifiedUserInfo mockResult = new VerifiedGoogleUserInfo(fullName, email, socialId);
        MockSocialTokenVerifier.setMockResult(mockResult);
        MockSocialTokenVerifier.shouldThrowException(true);
    }

    private String createExpiredJwt() {
        String secret = ruleChainForTests.getApplication().config().getString("play.http.secret.key");
        String issuer = ruleChainForTests.getApplication().config().getString("receptnekem.jwt.issuer");
        Date past = Date.from(Instant.now().minus(70, ChronoUnit.MINUTES));
        Algorithm algorithm = Algorithm.HMAC256(secret);

        return JWT.create()
                .withIssuer(issuer)
                .withClaim("user_id", 3L)
                .withExpiresAt(past)
                .sign(algorithm);
    }

    private List<FavoriteRecipe> favoriteRecipesOfUserInDb(Long userId) {
        return Ebean.createQuery(FavoriteRecipe.class)
                .where(eq("user.id", 1L))
                .findList();
    }

    private List<UserSearch> searchesOfUserInDb(Long userId) {
        return Ebean.createQuery(UserSearch.class)
                .where(eq("user.id", 1L))
                .findList();
    }

    private List<RecipeSearch> recipeSearchesOfUserSearches(List<UserSearch> userSearches) {
        return userSearches.stream()
                .map(UserSearch::getSearch)
                .collect(Collectors.toList());
    }

    private boolean areRecipeSearchesDeleted(List<RecipeSearch> recipeSearches) {
        return recipeSearches.stream()
                .map(r -> Ebean.find(RecipeSearch.class, r.getId()))
                .noneMatch(Objects::nonNull);
    }
}
