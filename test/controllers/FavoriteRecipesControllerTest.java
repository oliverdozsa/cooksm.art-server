package controllers;

import clients.FavoriteRecipesTestClient;
import com.github.database.rider.core.api.dataset.DataSet;
import data.entities.FavoriteRecipe;
import data.entities.Recipe;
import data.entities.SourcePage;
import data.entities.User;
import io.ebean.Ebean;
import lombokized.dto.FavoriteRecipeCreateDto;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.mvc.Result;
import rules.RuleChainForTests;

import static extractors.FavoriteRecipesFromResult.recipeIdOfSingleFavoriteRecipeOf;
import static extractors.FavoriteRecipesFromResult.recipeIdsOfFavoriteRecipesOf;
import static extractors.DataFromResult.sizeAsJsonOf;
import static extractors.DataFromResult.statusOf;
import static matchers.ResultHasLocationHeader.hasLocationHeader;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

public class FavoriteRecipesControllerTest {
    private final RuleChainForTests ruleChainForTests = new RuleChainForTests();

    @Rule
    public RuleChain chain = ruleChainForTests.getRuleChain();

    private FavoriteRecipesTestClient client;

    @Before
    public void setup() {
        client = new FavoriteRecipesTestClient(ruleChainForTests.getApplication());
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/favoriterecipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetEmpty() {
        // When
        Result result = client.allOf(1L);

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(sizeAsJsonOf(result), equalTo(0));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/favoriterecipes.yml", disableConstraints = true, cleanBefore = true)
    public void testCreate() {
        // When
        FavoriteRecipeCreateDto favoriteRecipe = new FavoriteRecipeCreateDto();
        favoriteRecipe.setRecipeId(1L);

        Result result = client.create(favoriteRecipe, 1L);

        // Then
        assertThat(statusOf(result), equalTo(CREATED));
        assertThat(result, hasLocationHeader());

        String locationUrl = result.headers().get("Location");
        result = client.byLocation(locationUrl, 1L);
        assertThat(recipeIdOfSingleFavoriteRecipeOf(result), equalTo(1L));

        result = client.allOf(1L);
        assertThat(sizeAsJsonOf(result), equalTo(1));
        assertThat(recipeIdsOfFavoriteRecipesOf(result), containsInAnyOrder(1L));
    }

    @Test
    @DataSet(value = "datasets/yml/favoriterecipes.yml", disableConstraints = true, cleanBefore = true)
    public void testDelete() {
        // Given
        FavoriteRecipe favoriteRecipe = createFavoriteRecipeInDb(1L, 1L);

        // When
        Result result = client.delete(favoriteRecipe.getId(), 1L);

        // Then
        assertThat(statusOf(result), equalTo(NO_CONTENT));
    }

    @Test
    public void testCreate_Invalid_NoJson() {
        // Given
        User user = createUserInDb("some@one");

        // When
        Result result = client.create("{asd", user.getId());

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    public void testCreate_Invalid_RecipeNotExisting() {
        // Given
        FavoriteRecipeCreateDto favoriteRecipe = new FavoriteRecipeCreateDto();
        favoriteRecipe.setRecipeId(4284L);

        // When
        Result result = client.create(favoriteRecipe, 1L);

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    @DataSet(value = "datasets/yml/favoriterecipes-max-reached.yml", disableConstraints = true, cleanBefore = true)
    public void testCreate_MaxReached() {
        // Given
        int max = ruleChainForTests.getApplication().config().getInt("receptnekem.favoriterecipes.maxperuser");
        for (int i = 0; i < max; i++) {
            Recipe recipe = createRecipeInDb("test-recipe-" + i, 1L);
            createFavoriteRecipeInDb(recipe.getId(), 1L);
        }

        // When
        FavoriteRecipeCreateDto favoriteRecipe = new FavoriteRecipeCreateDto();
        favoriteRecipe.setRecipeId(1L);

        Result result = client.create(favoriteRecipe, 1L);

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    @DataSet(value = "datasets/yml/favoriterecipes.yml", disableConstraints = true, cleanBefore = true)
    public void testCreate_Already_Existing() {
        // Given
        createFavoriteRecipeInDb(1L, 1L);

        // When
        FavoriteRecipeCreateDto favoriteRecipe = new FavoriteRecipeCreateDto();
        favoriteRecipe.setRecipeId(1L);

        Result result = client.create(favoriteRecipe, 1L);

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/favoriterecipes.yml", disableConstraints = true, cleanBefore = true)
    public void testSingle_UserDoesntOwn() {
        // When
        Result result = client.single(2L, 1L);

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    private User createUserInDb(String email) {
        User user = new User();
        user.setEmail(email);
        Ebean.save(user);

        return user;
    }

    private FavoriteRecipe createFavoriteRecipeInDb(Long recipeId, Long userId) {
        FavoriteRecipe favoriteRecipe = new FavoriteRecipe();
        favoriteRecipe.setRecipe(Ebean.find(Recipe.class, recipeId));
        favoriteRecipe.setUser(Ebean.find(User.class, userId));
        Ebean.save(favoriteRecipe);

        return favoriteRecipe;
    }

    private Recipe createRecipeInDb(String name, Long sourcePageId) {
        Recipe recipe = new Recipe();
        recipe.setName(name);
        recipe.setSourcePage(Ebean.find(SourcePage.class, sourcePageId));
        Ebean.save(recipe);

        return recipe;
    }
}
