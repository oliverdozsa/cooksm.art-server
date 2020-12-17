package controllers;

import clients.IngredientTagsTestClient;
import com.github.database.rider.core.api.dataset.DataSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.mvc.Result;
import rules.RuleChainForTests;

import static extractors.DataFromResult.*;
import static extractors.IngredientTagsFromResult.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static play.mvc.Http.Status.NOT_FOUND;
import static play.test.Helpers.BAD_REQUEST;
import static play.test.Helpers.OK;

public class IngredientTagsControllerTest {
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
    @DataSet(value = "datasets/yml/ingredienttags.yml", disableConstraints = true, cleanBefore = true)
    public void testListTags() {
        // When
        Result result = client.page("languageId=1&nameLike=_tag_");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(totalCountOf(result), equalTo(7));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/ingredienttags.yml", disableConstraints = true, cleanBefore = true)
    public void testPaging() {
        int limit = 2;
        int offset = 0;
        boolean lastPageNotReached = true;

        while (lastPageNotReached) {
            // When
            String queryParams = String.format("languageId=1&nameLike=_tag_&limit=%d&offset=%d", limit, offset);
            Result result = client.page(queryParams);

            // Then
            assertThat(statusOf(result), equalTo(OK));
            assertThat(totalCountOf(result), equalTo(7));
            assertThat(itemsSizeOf(result), lessThanOrEqualTo(limit));

            int totalCount = totalCountOf(result);
            lastPageNotReached = offset + limit < totalCount;
            offset += limit;
        }
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/ingredienttags.yml", disableConstraints = true, cleanBefore = true)
    public void testInvalidRequest() {
        // When
        String invalidLenghtNameLikeParams = "languageId=1&nameLike=a";
        Result result = client.page(invalidLenghtNameLikeParams);

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/ingredienttags.yml", disableConstraints = true, cleanBefore = true)
    public void testIngredientsOfTags() {
        // When
        Result result = client.page("languageId=1&nameLike=2_tag_2");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(totalCountOf(result), equalTo(1));
        assertThat(ingredientIdsOfIngredientTagOf(result, 0), containsInAnyOrder(2L, 1L));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testListTags_UserDefined() {
        // When
        Result result = client.authorizedPage(1L, "languageId=1&nameLike=tag_2");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(totalCountOf(result), equalTo(3));
        assertThat(ingredientTagIdsOf(result), hasSize(3));
        assertThat(ingredientTagIdsOf(result), containsInAnyOrder(6L, 3L, 11L));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testListTags_UserDefined_Unauth() {
        // When
        Result result = client.page("languageId=1&nameLike=tag_2");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(totalCountOf(result), equalTo(2));
        assertThat(ingredientTagIdsOf(result), hasSize(2));
        assertThat(ingredientTagIdsOf(result), containsInAnyOrder(6L, 3L));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUserDefined_SingleWithLanguageId() {
        // When
        Result result = client.single(1L, 10L, 2L);

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(ingredientNamesOfSingleIngredientTagOf(result), containsInAnyOrder("en_1", "en_2"));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUserDefined_Single_InvalidId() {
        // When
        Result result = client.single(1L, 42L, 0L);

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testListTags_UserDefinedOny() {
        // When
        Result result = client.userDefinedOnly(1L);

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(sizeAsJsonOf(result), equalTo(3));
        assertThat(idsOfUserDefinedOnlyTagsOf(result), containsInAnyOrder(10L, 11L, 12L));
    }
}
