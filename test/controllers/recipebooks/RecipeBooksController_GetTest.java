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
import static extractors.RecipeBooksFromResult.recipeBookNameOf;
import static extractors.RecipeBooksFromResult.recipeBookNamesOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static play.mvc.Http.Status.NOT_FOUND;
import static play.mvc.Http.Status.OK;

public class RecipeBooksController_GetTest {
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
    @DataSet(value = "datasets/yml/recipebooks.yml", disableConstraints = true, cleanBefore = true)
    public void testGetSingle() {
        // When
        Result result = client.single(1L, 1L);

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeBookNameOf(result), equalTo("recipe-book-1-user-1"));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipebooks.yml", disableConstraints = true, cleanBefore = true)
    public void testGetSingle_OtherUser() {
        // When
        Result result = client.single(1L, 2L);

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipebooks.yml", disableConstraints = true, cleanBefore = true)
    public void testGetAllOfUser() {
        // When
        Result result = client.allOf(2L);

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeBookNamesOf(result), hasSize(3));
        assertThat(recipeBookNamesOf(result), containsInAnyOrder(
                "recipe-book-2-user-2",
                "recipe-book-3-user-2",
                "recipe-book-4-user-2"));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipebooks.yml", disableConstraints = true, cleanBefore = true)
    public void testGetAllOfUser_UserHasNoBooks() {
        // When
        Result result = client.single(1L, 3L);

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipebooks.yml", disableConstraints = true, cleanBefore = true)
    public void testRecipeBookDoesNotExist() {
        // When
        Result result = client.single(42L, 1L);

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipebooks.yml", disableConstraints = true, cleanBefore = true)
    public void testUserDoesNotExist() {
        // When
        Result result = client.single(1L, 42L);

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }
}
