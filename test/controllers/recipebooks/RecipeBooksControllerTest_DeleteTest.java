package controllers.recipebooks;

import clients.RecipeBooksTestClient;
import com.github.database.rider.core.api.dataset.DataSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.mvc.Result;
import rules.RuleChainForTests;

import static extractors.DataFromResult.statusOf;
import static extractors.RecipeBooksFromDb.countOfRecipesInRecipeBook;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static play.mvc.Http.Status.*;

public class RecipeBooksControllerTest_DeleteTest {
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
    public void testDelete() {
        // Given
        Result result = client.single(1L, 1L);
        assertThat(statusOf(result), equalTo(OK));
        assertThat(countOfRecipesInRecipeBook(1L), equalTo(3));

        // When
        result = client.delete(1L, 1L);

        // Then
        assertThat(statusOf(result), equalTo(NO_CONTENT));
        assertThat(countOfRecipesInRecipeBook(1L), equalTo(0));

        result = client.single(1L, 1L);
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipebooks.yml", disableConstraints = true, cleanBefore = true)
    public void testDeleteNotExistingRecipeBook() {
        // When
        Result result = client.delete(42L, 1L);

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipebooks.yml", disableConstraints = true, cleanBefore = true)
    public void testUserNotExisting() {
        // When
        Result result = client.delete(1L, 2L);

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipebooks.yml", disableConstraints = true, cleanBefore = true)
    public void testDeleteOtherUser() {
        // When
        Result result = client.delete(1L, 2L);

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }
}
