package controllers;

import clients.IngredientTagsTestClient;
import com.github.database.rider.core.api.dataset.DataSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.mvc.Result;
import rules.RuleChainForTests;

import static extractors.DataFromResult.statusOf;
import static extractors.IngredientTagsFromResult.conflictingUserSearchNamesOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static play.mvc.Http.Status.*;

public class IngredientTagsControllerTest_DeleteTest {
    private final RuleChainForTests ruleChainForTests = new RuleChainForTests();

    @Rule
    public RuleChain chain = ruleChainForTests.getRuleChain();

    private IngredientTagsTestClient client;

    @Before
    public void setup() {
        client = new IngredientTagsTestClient(ruleChainForTests.getApplication());
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUserDefined_Delete() {
        // When
        Result result = client.delete(10L, 1L);

        // Then
        assertThat(statusOf(result), equalTo(NO_CONTENT));

        result = client.single(1L, 10L);
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUserDefined_Delete_InvalidId() {
        // When
        Result result = client.delete(42L, 1L);

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUserDefined_Delete_TagOfOtherUser() {
        // When
        Result result = client.delete(14L, 1L);

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = {
            "datasets/yml/ingredienttags.yml",
            "datasets/yml/ingredienttags-user-defined.yml",
            "datasets/yml/ingredienttags-user-defined-user-search.yml"
    }, disableConstraints = true, cleanBefore = true)
    public void testUserDefined_Delete_UserSearchExistsWithTagToDelete() {
        // When
        Result result = client.delete(10L, 1L);

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
        assertThat(conflictingUserSearchNamesOf(result), containsInAnyOrder("user1query1", "user1query2"));
    }
}
