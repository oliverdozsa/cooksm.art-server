package controllers.recipebooks;

import clients.RecipeSearchesTestClient;
import clients.UserSearchesTestClient;
import com.github.database.rider.core.api.dataset.DataSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.mvc.Result;
import rules.RuleChainForTests;

import static extractors.DataFromResult.statusOf;
import static extractors.RecipeSearchesFromResult.recipeBooksOfSingleRecipeSearchOf;
import static extractors.RecipeSearchesFromResult.useFavoritesOnlyOf;
import static extractors.UserSearchesFromResult.*;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static matchers.ResultHasLocationHeader.hasLocationHeader;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static play.mvc.Http.Status.*;

public class RecipeBooksControllerTest_InShareAndSaveSearch {
    private final RuleChainForTests ruleChainForTests = new RuleChainForTests();

    @Rule
    public RuleChain chain = ruleChainForTests.getRuleChain();

    private RecipeSearchesTestClient recipeSearchesTestClient;
    private UserSearchesTestClient userSearchesTestClient;

    @Before
    public void setup() {
        recipeSearchesTestClient = new RecipeSearchesTestClient(ruleChainForTests.getApplication());
        userSearchesTestClient = new UserSearchesTestClient(ruleChainForTests.getApplication());
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipebooks-query.yml", disableConstraints = true, cleanBefore = true)
    public void testShareSearchWithRecipeBooks() {
        // When
        Result result = recipeSearchesTestClient.create(
                "{" +
                        "  \"searchMode\": \"composed-of-number\"," +
                        "  \"goodIngs\": 3," +
                        "  \"goodIngsRel\": \"ge\"," +
                        "  \"unknownIngs\": \"0\"," +
                        "  \"unknownIngsRel\": \"ge\"," +
                        "  \"goodAdditionalIngs\": 2," +
                        "  \"goodAdditionalIngsRel\": \"ge\"," +
                        "  \"inIngs\": [1, 2, 3]," +
                        "  \"inIngTags\": [1]," +
                        "  \"exIngs\": [4, 7]," +
                        "  \"exIngTags\": [2]," +
                        "  \"addIngs\": [5]," +
                        "  \"addIngTags\": [6]," +
                        "  \"sourcePages\": [1, 2]," +
                        "  \"recipeBooks\": [1, 2]" +
                        "}");

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipebooks-query.yml", disableConstraints = true, cleanBefore = true)
    public void testSaveSearchWithRecipeBooks() {
        // When
        Result result = userSearchesTestClient.create(
                "{" +
                        "  \"name\": \"someName\"," +
                        "  \"query\": {" +
                        "    \"searchMode\": \"composed-of-number\"," +
                        "    \"goodIngs\": 3," +
                        "    \"goodIngsRel\": \"ge\"," +
                        "    \"unknownIngs\": \"0\"," +
                        "    \"unknownIngsRel\": \"ge\"," +
                        "    \"goodAdditionalIngs\": 2," +
                        "    \"goodAdditionalIngsRel\": \"ge\"," +
                        "    \"inIngs\": [1, 2, 3]," +
                        "    \"inIngTags\": [1]," +
                        "    \"exIngs\": [4, 7]," +
                        "    \"exIngTags\": [2]," +
                        "    \"addIngs\": [5]," +
                        "    \"addIngTags\": [6]," +
                        "    \"sourcePages\": [1, 2]," +
                        "    \"useFavoritesOnly\": true," +
                        "    \"recipeBooks\": [1, 2]" +
                        "  }" +
                        "}", 1L);

        // Then
        assertThat(statusOf(result), equalTo(CREATED));
        assertThat(result, hasLocationHeader());

        String url = result.header("Location").get();
        result = userSearchesTestClient.byLocation(url, 1L);

        assertThat(statusOf(result), equalTo(OK));
        assertThat(nameOfSingleUserSearchOf(result), equalTo("someName"));
        assertNotNull(idOfSingleUserSearchOf(result));

        String recipeSearchId = recipeSearchIdOfSingleUserSearchOf(result);
        result = recipeSearchesTestClient.single(recipeSearchId);

        assertThat(statusOf(result), equalTo(OK));
        assertTrue(useFavoritesOnlyOf(result));

        assertThat(recipeBooksOfSingleRecipeSearchOf(result), containsInAnyOrder("recipe-book-1-user-1", "recipe-book-2-user-1"));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipebooks-query.yml", disableConstraints = true, cleanBefore = true)
    public void testSaveSearchWithRecipeBooksContainingOtherUsersBook() {
        // When
        Result result = userSearchesTestClient.create(
                "{" +
                        "  \"name\": \"someName\"," +
                        "  \"query\": {" +
                        "    \"searchMode\": \"composed-of-number\"," +
                        "    \"goodIngs\": 3," +
                        "    \"goodIngsRel\": \"ge\"," +
                        "    \"unknownIngs\": \"0\"," +
                        "    \"unknownIngsRel\": \"ge\"," +
                        "    \"goodAdditionalIngs\": 2," +
                        "    \"goodAdditionalIngsRel\": \"ge\"," +
                        "    \"inIngs\": [1, 2, 3]," +
                        "    \"inIngTags\": [1]," +
                        "    \"exIngs\": [4, 7]," +
                        "    \"exIngTags\": [2]," +
                        "    \"addIngs\": [5]," +
                        "    \"addIngTags\": [6]," +
                        "    \"sourcePages\": [1, 2]," +
                        "    \"useFavoritesOnly\": true," +
                        "    \"recipeBooks\": [1, 3]" +
                        "  }" +
                        "}", 1L);

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipebooks-query.yml", disableConstraints = true, cleanBefore = true)
    public void testSaveSearchWithRecipeBooksContainingNotExisting() {
        // When
        Result result = userSearchesTestClient.create(
                "{" +
                        "  \"name\": \"someName\"," +
                        "  \"query\": {" +
                        "    \"searchMode\": \"composed-of-number\"," +
                        "    \"goodIngs\": 3," +
                        "    \"goodIngsRel\": \"ge\"," +
                        "    \"unknownIngs\": \"0\"," +
                        "    \"unknownIngsRel\": \"ge\"," +
                        "    \"goodAdditionalIngs\": 2," +
                        "    \"goodAdditionalIngsRel\": \"ge\"," +
                        "    \"inIngs\": [1, 2, 3]," +
                        "    \"inIngTags\": [1]," +
                        "    \"exIngs\": [4, 7]," +
                        "    \"exIngTags\": [2]," +
                        "    \"addIngs\": [5]," +
                        "    \"addIngTags\": [6]," +
                        "    \"sourcePages\": [1, 2]," +
                        "    \"useFavoritesOnly\": true," +
                        "    \"recipeBooks\": [1, 42]" +
                        "  }" +
                        "}", 1L);

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipebooks-query.yml", disableConstraints = true, cleanBefore = true)
    public void testSaveSearchWithRecipeBooksOnly() {
        // When
        Result result = userSearchesTestClient.create(
                "{" +
                        "  \"name\": \"someName\"," +
                        "  \"query\": {" +
                        "    \"recipeBooks\": [1, 2]" +
                        "  }" +
                        "}", 1L);

        // Then
        assertThat(statusOf(result), equalTo(CREATED));
        assertThat(result, hasLocationHeader());

        String url = result.header("Location").get();
        result = userSearchesTestClient.byLocation(url, 1L);

        assertThat(statusOf(result), equalTo(OK));
        assertThat(nameOfSingleUserSearchOf(result), equalTo("someName"));
        assertNotNull(idOfSingleUserSearchOf(result));

        String recipeSearchId = recipeSearchIdOfSingleUserSearchOf(result);
        result = recipeSearchesTestClient.single(recipeSearchId);
        assertThat(statusOf(result), equalTo(OK));

        assertThat(recipeBooksOfSingleRecipeSearchOf(result), containsInAnyOrder("recipe-book-1-user-1", "recipe-book-2-user-1"));
    }
}
