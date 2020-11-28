package controllers;

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
import static extractors.RecipeSearchesFromResult.*;
import static extractors.UserSearchesFromResult.*;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static matchers.ResultHasLocationHeader.hasLocationHeader;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static play.test.Helpers.*;

public class UserSearchesControllerTest {
    private final RuleChainForTests ruleChainForTests = new RuleChainForTests();

    @Rule
    public RuleChain chain = ruleChainForTests.getRuleChain();

    private UserSearchesTestClient client;
    private RecipeSearchesTestClient recipeSearchesClient;

    @Before
    public void setup() {
        client = new UserSearchesTestClient(ruleChainForTests.getApplication());
        recipeSearchesClient = new RecipeSearchesTestClient(ruleChainForTests.getApplication());
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/usersearches-base.yml", disableConstraints = true, cleanBefore = true)
    public void testCreate() {
        // When
        Result result = client.create(
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
                        "    \"useFavoritesOnly\": true" +
                        "  }" +
                        "}", 1L);

        // Then
        assertThat(statusOf(result), equalTo(CREATED));
        assertThat(result, hasLocationHeader());

        String url = result.header("Location").get();
        result = client.byLocation(url, 1L);

        assertThat(statusOf(result), equalTo(OK));
        assertThat(nameOfSingleUserSearchOf(result), equalTo("someName"));
        assertNotNull(idOfSingleUserSearchOf(result));

        String recipeSearchId = recipeSearchIdOfSingleUserSearchOf(result);
        result = recipeSearchesClient.single(recipeSearchId);

        assertTrue(useFavoritesOnlyOf(result));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/usersearches-base.yml", "datasets/yml/usersearches.yml"}, disableConstraints = true, cleanBefore = true)
    public void testGetAll() {
        // When
        Result result = client.all(2L);

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(namesOfUserSearchesOf(result), hasSize(3));
        assertThat(namesOfUserSearchesOf(result), containsInAnyOrder("user2query1", "user2query2", "user2query3"));
        assertThat(searchIdsOfUserSearchesOf(result), hasSize(3));
        assertThat(searchIdsOfUserSearchesOf(result), containsInAnyOrder(239329L, 239330L, 239331L));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/usersearches-base.yml", "datasets/yml/usersearches.yml"}, disableConstraints = true, cleanBefore = true)
    public void testPatchFully() {
        // When
        Result result = client.patch("{" +
                "  \"name\": \"user1query1renamed\"," +
                "  \"query\": {" +
                "    \"searchMode\": \"composed-of-ratio\"," +
                "    \"goodIngsRatio\": 0.6," +
                "    \"inIngs\": [1, 2, 3]," +
                "    \"inIngTags\": [1]," +
                "    \"exIngs\": [4, 7]," +
                "    \"exIngTags\": [2]," +
                "    \"sourcePages\": [1]" +
                "  }" +
                "}", 1L, 1L);

        // Then
        assertThat(statusOf(result), equalTo(NO_CONTENT));

        result = client.single(1L, 1L);
        assertThat(nameOfSingleUserSearchOf(result), equalTo("user1query1renamed"));

        String recipeSearchId = recipeSearchIdOfSingleUserSearchOf(result);
        result = recipeSearchesClient.single(recipeSearchId);

        assertThat(statusOf(result), equalTo(OK));
        assertThat(searchModeOfSingleRecipeSearchOf(result), equalTo("composed-of-ratio"));
        assertThat(goodIngredientsRatioOf(result), equalTo(0.6));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/usersearches-base.yml", disableConstraints = true, cleanBefore = true)
    public void testCreate_InvalidName() {
        // When
        Result result = client.create(
                "{" +
                "  \"name\": \"s\"," +
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
                "    \"sourcePages\": [1, 2]" +
                "  }" +
                "}", 1L);

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/usersearches-base.yml", disableConstraints = true, cleanBefore = true)
    public void testCreate_InvalidQuery() {
        // When
        Result result = client.create(
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
                "    \"exIngs\": [4, 7]," +
                "    \"exIngTags\": [2]," +
                "    \"addIngs\": [5]," +
                "    \"addIngTags\": [6]," +
                "    \"sourcePages\": [1, 2]" +
                "  }" +
                "}", 1L);

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    @DataSet(value = "datasets/yml/usersearches-base.yml", disableConstraints = true, cleanBefore = true)
    public void testCreate_LimitReached() {
        // Given
        createMaxUserSearches();

        // When
        Result result = client.create("{" +
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
                "    \"sourcePages\": [1, 2]" +
                "  }" +
                "}", 1L);

        // Then
        assertThat(statusOf(result), equalTo(FORBIDDEN));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/usersearches-base.yml", "datasets/yml/usersearches.yml"}, disableConstraints = true, cleanBefore = true)
    public void testPatch_InvalidName() {
        // When
        Result result = client.patch("{" +
                "  \"name\": \"u\"," +
                "  \"query\": {" +
                "    \"searchMode\": \"composed-of-ratio\"," +
                "    \"goodIngsRatio\": 0.6," +
                "    \"inIngs\": [1, 2, 3]," +
                "    \"inIngTags\": [1]," +
                "    \"exIngs\": [4, 7]," +
                "    \"exIngTags\": [2]," +
                "    \"sourcePages\": [1]" +
                "  }" +
                "}", 1L, 1L);

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/usersearches-base.yml", "datasets/yml/usersearches.yml"}, disableConstraints = true, cleanBefore = true)
    public void testPatch_InvalidQuery() {
        // When
        Result result = client.patch(
                "{" +
                "  \"name\": \"user1query1renamed\"," +
                "  \"query\": {" +
                "    \"searchMode\": \"composed-of-ratio\"," +
                "    \"goodIngsRatio\": 0.6," +
                "    \"exIngs\": [4, 7]," +
                "    \"exIngTags\": [2]," +
                "    \"sourcePages\": [1]" +
                "  }" +
                "}", 1L, 1L);

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/usersearches-base.yml", "datasets/yml/usersearches.yml"}, disableConstraints = true, cleanBefore = true)
    public void testPatch_OtherUser() {
        // When
        Result result = client.patch(
                "{" +
                "  \"name\": \"user1query1renamed\"," +
                "  \"query\": {" +
                "    \"searchMode\": \"composed-of-ratio\"," +
                "    \"goodIngsRatio\": 0.6," +
                "    \"inIngs\": [1, 2, 3]," +
                "    \"inIngTags\": [1]," +
                "    \"exIngs\": [4, 7]," +
                "    \"exIngTags\": [2]," +
                "    \"sourcePages\": [1]" +
                "  }" +
                "}", 1L, 3L);

        //Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/usersearches-base.yml", "datasets/yml/usersearches.yml"}, disableConstraints = true, cleanBefore = true)
    public void testDelete() {
        // When
        Result result = client.delete(1L, 2L);

        // Then
        assertThat(statusOf(result), equalTo(NO_CONTENT));

        result = client.single(1L, 2L);
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/usersearches-base.yml", "datasets/yml/usersearches.yml"}, disableConstraints = true, cleanBefore = true)
    public void testDelete_InvalidId() {
        // When
        Result result = client.delete(1L, 42L);

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/usersearches-base.yml", "datasets/yml/usersearches.yml"}, disableConstraints = true, cleanBefore = true)
    public void testDelete_OtherUser() {
        // When
        Result result = client.delete(1L, 3L);

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/usersearches-base.yml", "datasets/yml/usersearches.yml"}, disableConstraints = true, cleanBefore = true)
    public void testSingle_OtherUser() {
        // When
        Result result = client.single(2L, 1L);

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/usersearches-base.yml", "datasets/yml/usersearches.yml"}, disableConstraints = true, cleanBefore = true)
    public void testPatchName() {
        // When
        Result result = client.patch(
                "{" +
                "  \"name\": \"user1query1renamed\"" +
                "}", 1L, 1L);

        // Then
        assertThat(statusOf(result), equalTo(NO_CONTENT));

        result = client.single(1L, 1L);

        assertThat(statusOf(result), equalTo(OK));
        assertThat(nameOfSingleUserSearchOf(result), equalTo("user1query1renamed"));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/usersearches-base.yml", "datasets/yml/usersearches.yml"}, disableConstraints = true, cleanBefore = true)
    public void testPatchQuery() {
        // When
        Result result = client.patch("{" +
                "  \"query\": {" +
                "    \"searchMode\": \"composed-of-ratio\"," +
                "    \"goodIngsRatio\": 0.6," +
                "    \"inIngs\": [1, 2, 3]," +
                "    \"inIngTags\": [1]," +
                "    \"exIngs\": [4, 7]," +
                "    \"exIngTags\": [2]," +
                "    \"sourcePages\": [1]" +
                "  }" +
                "}", 1L, 1L);

        // Then
        assertThat(statusOf(result), equalTo(NO_CONTENT));

        result = client.single(1L, 1L);

        assertThat(statusOf(result), equalTo(OK));
        assertThat(nameOfSingleUserSearchOf(result), equalTo("user1query1"));

        String searchId = recipeSearchIdOfSingleUserSearchOf(result);

        result = recipeSearchesClient.single(searchId);

        assertThat(statusOf(result), equalTo(OK));
        assertThat(searchModeOfSingleRecipeSearchOf(result), equalTo("composed-of-ratio"));
        assertThat(goodIngredientsRatioOf(result), equalTo(0.6));
    }

    private void createMaxUserSearches(){
        String queryStr = "{" +
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
                "    \"sourcePages\": [1, 2]" +
                "  }" +
                "}";

        int maxSearches = ruleChainForTests.getApplication()
                .config().getInt("receptnekem.usersearches.maxperuser");

        for (int i = 0; i < maxSearches; i++) {
            Result result = client.create(queryStr, 1L);
            assertThat(statusOf(result), equalTo(CREATED));
        }
    }
}
