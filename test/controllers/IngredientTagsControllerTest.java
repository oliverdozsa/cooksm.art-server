package controllers;

import clients.IngredientTagsTestClient;
import com.github.database.rider.core.api.dataset.DataSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.mvc.Result;
import rules.RuleChainForTests;

import static matchers.ResultHasIngredientTagWithIds.hasIngredientTagWithIds;
import static matchers.ResultHasIngredientTagWithIngredientIds.hasIngredientTagWithIngredientIds;
import static matchers.ResultHasSingleIngredientTagWithIngredientNames.hasSingleIngredientTagWithIngredientNames;
import static matchers.ResultHasTotalCount.hasTotalCount;
import static matchers.ResultStatusIs.statusIs;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static play.test.Helpers.BAD_REQUEST;
import static play.test.Helpers.OK;
import static utils.ExtractFromResult.itemsSizeOf;
import static utils.ExtractFromResult.totalCountOf;

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
        assertThat(result, statusIs(OK));
        assertThat(result, hasTotalCount(7));
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
            assertThat(result, statusIs(OK));
            assertThat(result, hasTotalCount(7));
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
        assertThat(result, statusIs(BAD_REQUEST));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/ingredienttags.yml", disableConstraints = true, cleanBefore = true)
    public void testIngredientsOfTags() {
        // When
        Result result = client.page("languageId=1&nameLike=2_tag_2");

        // Then
        assertThat(result, statusIs(OK));
        assertThat(totalCountOf(result), equalTo(1));
        assertThat(result, hasIngredientTagWithIngredientIds(0, 2L, 1L));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testListTags_UserDefined() {
        // When
        Result result = client.authorizedPage(1L, "languageId=1&nameLike=tag_2");

        // Then
        assertThat(result, statusIs(OK));
        assertThat(totalCountOf(result), equalTo(3));
        assertThat(result, hasIngredientTagWithIds(6L, 3L, 11L));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testListTags_UserDefined_Unauth() {
        // When
        Result result = client.page("languageId=1&nameLike=tag_2");

        // Then
        assertThat(result, statusIs(OK));
        assertThat(totalCountOf(result), equalTo(2));
        assertThat(result, hasIngredientTagWithIds(6L, 3L));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUserDefined_SingleWithLanguageId() {
        // When
        Result result = client.single(1L, 10L, 2L);

        // Then
        assertThat(result, statusIs(OK));
        assertThat(result, hasSingleIngredientTagWithIngredientNames("en_1", "en_2"));
    }
}
