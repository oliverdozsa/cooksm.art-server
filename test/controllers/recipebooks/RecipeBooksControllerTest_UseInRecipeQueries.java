package controllers.recipebooks;

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
import static play.mvc.Http.Status.NOT_FOUND;
import static play.mvc.Http.Status.OK;

public class RecipeBooksControllerTest_UseInRecipeQueries {
    private final RuleChainForTests ruleChainForTests = new RuleChainForTests();

    @Rule
    public RuleChain chain = ruleChainForTests.getRuleChain();

    private RecipesTestClient client;

    @Before
    public void setup() {
        client = new RecipesTestClient(ruleChainForTests.getApplication());
        Ebean.createSqlUpdate("update recipe " +
                "set numofings = (select count(*) from recipe_ingredient where recipe.id = recipe_ingredient.recipe_id)")
                .execute();
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipebooks-query.yml", disableConstraints = true, cleanBefore = true)
    public void testUseInRecipeQueryUnauth() {
        // When
        Result result = client.page("searchMode=composed-of-number&" +
                "unknownIngs=4&unknownIngsRel=le&" +
                "goodIngs=2&goodIngsRel=ge&" +
                "limit=50&offset=0&" +
                "orderBy=name&orderBySort=asc&" +
                "minIngs=1&maxIngs=5&" +
                "inIngs[0]=1&inIngs[1]=3&exIngs[0]=5&" +
                "recipeBooks[0]=1&recipeBooks[1]=2");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOf(result), hasSize(3));
        assertThat(recipeIdsOf(result), containsInAnyOrder(1L, 2L, 6L));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipebooks-query.yml", disableConstraints = true, cleanBefore = true)
    public void testUseInRecipeQueryAuth() {
        // When
        Result result = client.page("searchMode=composed-of-number&" +
                "unknownIngs=4&unknownIngsRel=le&" +
                "goodIngs=2&goodIngsRel=ge&" +
                "limit=50&offset=0&" +
                "orderBy=name&orderBySort=asc&" +
                "minIngs=1&maxIngs=5&" +
                "inIngs[0]=1&inIngs[1]=3&exIngs[0]=5&" +
                "recipeBooks[0]=1&recipeBooks[1]=2", 1L);

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOf(result), hasSize(2));
        assertThat(recipeIdsOf(result), containsInAnyOrder(1L, 2L));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipebooks-query.yml", disableConstraints = true, cleanBefore = true)
    public void testUseInRecipeQuery_NotExistingBook() {
        // When
        Result result = client.page("searchMode=composed-of-number&" +
                "unknownIngs=4&unknownIngsRel=le&" +
                "goodIngs=2&goodIngsRel=ge&" +
                "limit=50&offset=0&" +
                "orderBy=name&orderBySort=asc&" +
                "minIngs=1&maxIngs=5&" +
                "inIngs[0]=1&inIngs[1]=3&exIngs[0]=5&" +
                "recipeBooks[0]=1&recipeBooks[1]=42", 1L);

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipebooks-query.yml", disableConstraints = true, cleanBefore = true)
    public void testUseInRecipeQuery_OtherUsersBook() {
        // When
        Result result = client.page("searchMode=composed-of-number&" +
                "unknownIngs=4&unknownIngsRel=le&" +
                "goodIngs=2&goodIngsRel=ge&" +
                "limit=50&offset=0&" +
                "orderBy=name&orderBySort=asc&" +
                "minIngs=1&maxIngs=5&" +
                "inIngs[0]=1&inIngs[1]=3&exIngs[0]=5&" +
                "recipeBooks[0]=1&recipeBooks[1]=3", 1L);

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipebooks-query.yml", disableConstraints = true, cleanBefore = true)
    public void testGetAllOfUser_OneRecipeInMultipleBooks() {
        // When
        Result result = client.page("recipeBooks[0]=6&recipeBooks[1]=7", 3L);

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOf(result), hasSize(4));
        assertThat(recipeIdsOf(result), containsInAnyOrder(1L, 2L, 3L, 4L));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipebooks-query.yml", disableConstraints = true, cleanBefore = true)
    public void testUseFavoritesAndRecipeBooksWithNoIngredientsCondition() {
        Result result = client.page("useFavoritesOnly=true&" +
                "recipeBooks[0]=1&" +
                "limit=50&offset=0&" +
                "orderBy=name&orderBySort=asc", 1L);

        // Then
        assertThat(statusOf(result), equalTo(OK));
    }
}
