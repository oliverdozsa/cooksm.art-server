package controllers.recipebooks;

import clients.RecipeBooksTestClient;
import com.github.database.rider.core.api.dataset.DataSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.mvc.Result;
import rules.RuleChainForTests;

import java.time.Instant;

import static extractors.DataFromResult.statusOf;
import static extractors.RecipeBooksFromResult.lastAccessedDateOfRecipeBookOf;
import static extractors.RecipeBooksFromResult.recipeIdsOfRecipeBookOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static play.mvc.Http.Status.*;

public class RecipeBooksControllerTest_UpdateRecipesTest {
    private final RuleChainForTests ruleChainForTests = new RuleChainForTests();

    @Rule
    public RuleChain chain = ruleChainForTests.getRuleChain();

    private RecipeBooksTestClient client;

    @Before
    public void setup() {
        client = new RecipeBooksTestClient(ruleChainForTests.getApplication());
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/recipebooks.yml", "datasets/yml/recipes.yml"}, disableConstraints = true, cleanBefore = true)
    public void testDeleteRecipesFromBook() {
        // When
        Result result = client.recipesOf(1L, 1L);
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOfRecipeBookOf(result), hasSize(3));
        Instant lastAccessedBeforeUpdate = lastAccessedDateOfRecipeBookOf(result);
        result = client.updateRecipes(1L, 1L, new Long[]{});

        // Then
        assertThat(statusOf(result), equalTo(NO_CONTENT));
        result = client.recipesOf(1L, 1L);
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOfRecipeBookOf(result), hasSize(0));
        assertThat(lastAccessedDateOfRecipeBookOf(result), greaterThan(lastAccessedBeforeUpdate));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/recipebooks.yml", "datasets/yml/recipes.yml"}, disableConstraints = true, cleanBefore = true)
    public void testDeleteRecipesFromBookRecipeIdsNotSet() {
        // When
        Result result = client.recipesOf(1L, 1L);
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOfRecipeBookOf(result), hasSize(3));
        Instant lastAccessedBeforeUpdate = lastAccessedDateOfRecipeBookOf(result);
        result = client.updateRecipes(1L, 1L, null);

        // Then
        assertThat(statusOf(result), equalTo(NO_CONTENT));
        result = client.recipesOf(1L, 1L);
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOfRecipeBookOf(result), hasSize(0));
        assertThat(lastAccessedDateOfRecipeBookOf(result), greaterThan(lastAccessedBeforeUpdate));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/recipebooks.yml", "datasets/yml/recipes.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUpdateRecipesOfBook() {
        // When
        Result result = client.recipesOf(1L, 1L);
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOfRecipeBookOf(result), hasSize(3));
        Instant lastAccessedBeforeUpdate = lastAccessedDateOfRecipeBookOf(result);
        result = client.updateRecipes(1L, 1L, new Long[]{4L, 1L});

        // Then
        assertThat(statusOf(result), equalTo(NO_CONTENT));
        result = client.recipesOf(1L, 1L);
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOfRecipeBookOf(result), hasSize(2));
        assertThat(recipeIdsOfRecipeBookOf(result), containsInAnyOrder(4L, 1L));
        assertThat(lastAccessedDateOfRecipeBookOf(result), greaterThan(lastAccessedBeforeUpdate));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/recipebooks.yml", "datasets/yml/recipes.yml"}, disableConstraints = true, cleanBefore = true)
    public void testNotExistingRecipe() {
        // When
        Result result = client.updateRecipes(1L, 1L, new Long[]{42L});

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/recipebooks.yml", "datasets/yml/recipes.yml"}, disableConstraints = true, cleanBefore = true)
    public void testNotExistingBook() {
        // When
        Result result = client.updateRecipes(42L, 1L, new Long[]{});

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/recipebooks.yml", "datasets/yml/recipes.yml"}, disableConstraints = true, cleanBefore = true)
    public void testOtherUsersBook() {
        // When
        Result result = client.updateRecipes(1L, 2L, new Long[]{});

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }
}
