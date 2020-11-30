package controllers;

import clients.RecipesTestClient;
import com.github.database.rider.core.api.dataset.DataSet;
import io.ebean.Ebean;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.mvc.Result;
import rules.RuleChainForTests;

import static extractors.DataFromResult.statusOf;
import static extractors.RecipesFromResult.recipeIdsOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static play.mvc.Http.Status.OK;

public class RecipesController_UserDefinedIngrTagsTest {
    private final RuleChainForTests ruleChainForTests = new RuleChainForTests();

    @Rule
    public RuleChain chain = ruleChainForTests.getRuleChain();

    private RecipesTestClient client;

    @Before
    public void before() {
        client = new RecipesTestClient(ruleChainForTests.getApplication());
        Ebean.createSqlUpdate("update recipe " +
                "set numofings = (select count(*) from recipe_ingredient where recipe.id = recipe_ingredient.recipe_id)")
                .execute();
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/recipes.yml", "datasets/yml/recipes-user-defined-tags.yml"}, disableConstraints = true, cleanBefore = true)
    public void testRecipesWithUserDefinedIngrTags_Included() {
        // When
        Result result = client.page("searchMode=composed-of-number&" +
                "unknownIngs=0&unknownIngsRel=ge&" +
                "goodIngs=2&goodIngsRel=ge&" +
                "limit=50&offset=0&" +
                "orderBy=name&orderBySort=asc&" +
                "minIngs=1&maxIngs=8&" +
                "inIngTags[0]=1&inIngTags[1]=9", 1L);

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOf(result), hasSize(2));
        assertThat(recipeIdsOf(result), containsInAnyOrder(3L, 2L));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/recipes.yml", "datasets/yml/recipes-user-defined-tags.yml"}, disableConstraints = true, cleanBefore = true)
    public void testRecipesWithUserDefinedIngrTags_Included_Unauth() {
        // When
        Result result = client.page("searchMode=composed-of-number&" +
                "unknownIngs=0&unknownIngsRel=ge&" +
                "goodIngs=2&goodIngsRel=ge&" +
                "limit=50&offset=0&" +
                "orderBy=name&orderBySort=asc&" +
                "minIngs=1&maxIngs=8&" +
                "inIngTags[0]=1&inIngTags[1]=9");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOf(result), hasSize(1));
        assertThat(recipeIdsOf(result), containsInAnyOrder(3L));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/recipes.yml", "datasets/yml/recipes-user-defined-tags.yml"}, disableConstraints = true, cleanBefore = true)
    public void testRecipesWithUserDefinedIngrTags_Included_OtherUsersTags() {
        // When
        Result result = client.page("searchMode=composed-of-number&" +
                "unknownIngs=0&unknownIngsRel=ge&" +
                "goodIngs=2&goodIngsRel=ge&" +
                "limit=50&offset=0&" +
                "orderBy=name&orderBySort=asc&" +
                "minIngs=1&maxIngs=8&" +
                "inIngTags[0]=1&inIngTags[1]=9&inIngTags[2]=10", 1L);

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOf(result), hasSize(2));
        assertThat(recipeIdsOf(result), containsInAnyOrder(3L, 2L));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/recipes.yml", "datasets/yml/recipes-user-defined-tags.yml"}, disableConstraints = true, cleanBefore = true)
    public void testRecipesWithUserDefinedIngrTags_Excluded() {
        // When
        Result result = client.page("limit=50&offset=0&" +
                "orderBy=name&orderBySort=asc&" +
                "minIngs=1&maxIngs=8&" +
                "exIngTags[0]=6&exIngTags[1]=8", 1L);

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOf(result), hasSize(1));
        assertThat(recipeIdsOf(result), containsInAnyOrder(5L));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/recipes.yml", "datasets/yml/recipes-user-defined-tags.yml"}, disableConstraints = true, cleanBefore = true)
    public void testRecipesWithUserDefinedIngrTags_Excluded_Unauth() {
        // When
        Result result = client.page("limit=50&offset=0&" +
                "orderBy=name&orderBySort=asc&" +
                "minIngs=1&maxIngs=8&" +
                "exIngTags[0]=6&exIngTags[1]=8");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOf(result), hasSize(3));
        assertThat(recipeIdsOf(result), containsInAnyOrder(5L, 1L, 2L));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/recipes.yml", "datasets/yml/recipes-user-defined-tags.yml"}, disableConstraints = true, cleanBefore = true)
    public void testRecipesWithUserDefinedIngrTags_Excluded_OtherUsersTag() {
        // When
        Result result = client.page("limit=50&offset=0&" +
                "orderBy=name&orderBySort=asc&" +
                "minIngs=1&maxIngs=8&" +
                "exIngTags[0]=7&exIngTags[1]=8&exIngTags[2]=10", 2L);

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOf(result), hasSize(2));
        assertThat(recipeIdsOf(result), containsInAnyOrder(5L, 1L));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/recipes.yml", "datasets/yml/recipes-user-defined-tags.yml"}, disableConstraints = true, cleanBefore = true)
    public void testRecipesWithUserDefinedIngrTags_Additionals() {
        // When
        Result result = client.page("searchMode=composed-of-number&" +
                "limit=50&offset=0&" +
                "unknownIngs=0&unknownIngsRel=ge&" +
                "goodIngs=1&goodIngsRel=eq&" +
                "goodAdditionalIngs=1&goodAdditionalIngsRel=ge&" +
                "orderBy=name&orderBySort=asc&" +
                "minIngs=1&maxIngs=5&" +
                "inIngs[0]=4&addIngTags[0]=6&addIngTags[1]=8", 1L);

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOf(result), hasSize(2));
        assertThat(recipeIdsOf(result), containsInAnyOrder(2L, 3L));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/recipes.yml", "datasets/yml/recipes-user-defined-tags.yml"}, disableConstraints = true, cleanBefore = true)
    public void testRecipesWithUserDefinedIngrTags_Additionals_Unauth() {
        // When
        Result result = client.page("searchMode=composed-of-number&limit=50&unknownIngs=0&unknownIngsRel=ge&goodIngs=1&goodIngsRel=eq&goodAdditionalIngs=1&goodAdditionalIngsRel=ge&offset=0&orderBy=name&orderBySort=asc&minIngs=1&maxIngs=5&inIngs[0]=4&addIngTags[0]=6&addIngTags[1]=8");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOf(result), hasSize(1));
        assertThat(recipeIdsOf(result), containsInAnyOrder(3L));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/recipes.yml", "datasets/yml/recipes-user-defined-tags.yml"}, disableConstraints = true, cleanBefore = true)
    public void testRecipesWithUserDefinedIngrTags_Additionals_OtherUsersTag() {
        // When
        Result result = client.page("searchMode=composed-of-number&" +
                "limit=50&offset=0&" +
                "unknownIngs=0&unknownIngsRel=ge&" +
                "goodIngs=1&goodIngsRel=eq&" +
                "goodAdditionalIngs=1&goodAdditionalIngsRel=ge&" +
                "orderBy=name&orderBySort=asc&" +
                "minIngs=1&maxIngs=5&" +
                "inIngs[0]=4&&addIngTags[0]=8&addIngTags[1]=10", 1L);

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOf(result), hasSize(1));
        assertThat(recipeIdsOf(result), containsInAnyOrder(2L));
    }
}
