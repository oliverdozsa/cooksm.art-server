package controllers.recipebooks;

import clients.RecipeBooksTestClient;
import com.github.database.rider.core.api.dataset.DataSet;
import com.typesafe.config.Config;
import data.entities.Recipe;
import data.entities.SourcePage;
import io.ebean.Ebean;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.mvc.Result;
import rules.RuleChainForTests;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static extractors.DataFromResult.statusOf;
import static extractors.RecipeBooksFromResult.lastAccessedDateOfRecipeBookOf;
import static extractors.RecipeBooksFromResult.recipeIdsOfRecipeBookOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static play.mvc.Http.Status.*;

public class RecipeBooksControllerTest_AddRecipesTest {
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
    @DataSet(value = {"datasets/yml/recipes.yml", "datasets/yml/recipebooks.yml"}, disableConstraints = true, cleanBefore = true)
    public void testAddRecipesTo() {
        // When
        Result result = client.single(5L, 2L);
        Instant lastAccessedBeforeUpdate = lastAccessedDateOfRecipeBookOf(result);

        result = client.addRecipes(5L, 2L, new Long[]{1L, 2L, 3L});

        // Then
        assertThat(statusOf(result), equalTo(NO_CONTENT));

        result = client.recipesOf(5L, 2L);
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOfRecipeBookOf(result), hasSize(3));
        assertThat(recipeIdsOfRecipeBookOf(result), containsInAnyOrder(1L, 2L, 3L));
        assertThat(lastAccessedDateOfRecipeBookOf(result), greaterThan(lastAccessedBeforeUpdate));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/recipes.yml", "datasets/yml/recipebooks.yml"}, disableConstraints = true, cleanBefore = true)
    public void testAddRecipesAlreadyAdded() {
        // When
        Result result = client.single(1L, 1L);
        Instant lastAccessedBeforeUpdate = lastAccessedDateOfRecipeBookOf(result);

        result = client.addRecipes(1L, 1L, new Long[]{1L, 2L, 3L, 4L});

        // Then
        assertThat(statusOf(result), equalTo(NO_CONTENT));

        result = client.recipesOf(1L, 1L);
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOfRecipeBookOf(result), hasSize(4));
        assertThat(recipeIdsOfRecipeBookOf(result), containsInAnyOrder(1L, 2L, 3L, 4L));
        assertThat(lastAccessedDateOfRecipeBookOf(result), greaterThan(lastAccessedBeforeUpdate));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/recipes.yml", "datasets/yml/recipebooks.yml"}, disableConstraints = true, cleanBefore = true)
    public void testAddRecipesNotExistingBook() {
        // When
        Result result = client.addRecipes(42L, 1L, new Long[]{1L});

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/recipes.yml", "datasets/yml/recipebooks.yml"}, disableConstraints = true, cleanBefore = true)
    public void testAddRecipesToOtherUsersBook() {
        // When
        Result result = client.addRecipes(2L, 1L, new Long[]{42L});

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/recipes.yml", "datasets/yml/recipebooks.yml"}, disableConstraints = true, cleanBefore = true)
    public void testAddRecipesToNotExistingRecipe() {
        // When
        Result result = client.addRecipes(1L, 1L, new Long[]{42L});

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test

    @DataSet(value = {"datasets/yml/recipes.yml", "datasets/yml/recipebooks.yml"}, disableConstraints = true, cleanBefore = true)
    public void testAddRecipesToLimitReached() {
        // Given
        addMaxRecipesTo(5L, 2L);

        // When
        Result result = client.addRecipes(5L, 2L, new Long[]{1L});

        // Then
        assertThat(statusOf(result), equalTo(FORBIDDEN));
    }

    private void addMaxRecipesTo(Long id, Long userId) {
        Config config = ruleChainForTests.getApplication().config();
        int maxPerBook = config.getInt("receptnekem.recipebooks.maxrecipesperbook");

        List<Long> recipeIds = createNumberOfRecipesInDb(maxPerBook);
        addRecipesInSplits(recipeIds, id, userId);
    }

    private List<Long> createNumberOfRecipesInDb(int n) {
        SourcePage sourcePage = Ebean.find(SourcePage.class, 1L);
        List<Long> ids = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            Recipe recipe = new Recipe();
            recipe.setSourcePage(sourcePage);
            Ebean.save(recipe);
            ids.add(recipe.getId());
        }

        return ids;
    }

    private void addRecipesInSplits(List<Long> recipeIdsList, Long id, Long userId) {
        Long[] recipeIds = recipeIdsList.toArray(new Long[]{});
        int splitSize = 50;

        for (int i = 0; i < recipeIds.length; i += splitSize) {
            int from = i;
            int to = Math.min(from + splitSize, recipeIds.length);
            Long[] split = Arrays.copyOfRange(recipeIds, from, to);
            client.addRecipes(id, userId, split);
        }
    }
}
