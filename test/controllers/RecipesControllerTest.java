package controllers;

import clients.RecipesTestClient;
import clients.SourcePagesTestClient;
import com.github.database.rider.core.api.dataset.DataSet;
import data.entities.*;
import io.ebean.Ebean;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.mvc.Result;
import rules.RuleChainForTests;

import java.time.Instant;

import static extractors.DataFromResult.*;
import static extractors.RecipesFromResult.recipeIdsOf;
import static extractors.RecipesFromResult.singleRecipeIdOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;
import static play.test.Helpers.*;

public class RecipesControllerTest {
    private final RuleChainForTests ruleChainForTests = new RuleChainForTests();

    @Rule
    public RuleChain chain = ruleChainForTests.getRuleChain();

    private RecipesTestClient client;
    private SourcePagesTestClient sourcePagesClient;

    @Before
    public void before() {
        client = new RecipesTestClient(ruleChainForTests.getApplication());
        sourcePagesClient = new SourcePagesTestClient(ruleChainForTests.getApplication());
        Ebean.createSqlUpdate("update recipe " +
                "set numofings = (select count(*) from recipe_ingredient where recipe.id = recipe_ingredient.recipe_id)")
                .execute();
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetByIngredients_ComposedOf_ExcludedOverlaps() {
        // When
        Result result = client.page("searchMode=composed-of-number&" +
                "limit=50&offset=0&" +
                "unknownIngs=4&unknownIngsRel=le&" +
                "goodIngs=2&goodIngsRel=ge&" +
                "orderBy=name&orderBySort=asc&" +
                "minIngs=1&maxIngs=5&" +
                "inIngs[0]=1&inIngs[1]=2&exIngs[0]=4&exIngs[1]=5");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOf(result), hasSize(1));
        assertThat(recipeIdsOf(result), containsInAnyOrder(1L));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetByIngredients_IncludedExcludedNotMutEx() {
        boolean isMutualExclusionCheckDisabled = ruleChainForTests.getApplication().config()
                .getBoolean("receptnekem.disable.mutual.exclusion.check");
        assumeFalse("Mutual exclusion check is disabled.", isMutualExclusionCheckDisabled);

        // When
        Result result = client.page("searchMode=composed-of-number&" +
                "unknownIngs=4&unknownIngsRel=le&" +
                "goodIngs=2&goodIngsRel=ge&" +
                "limit=50&offset=0&" +
                "orderBy=name&orderBySort=asc&" +
                "minIngs=1&maxIngs=5&" +
                "inIngs[0]=1&inIngs[1]=2&exIngs[0]=4&exIngs[1]=2");

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetByIngredients_IncludedExcludedNotMutEx_MutexDisabled() {
        boolean isMutualExclusionCheckDisabled = ruleChainForTests.getApplication().config()
                .getBoolean("receptnekem.disable.mutual.exclusion.check");
        assumeTrue("Mutual exclusion check is enabled.", isMutualExclusionCheckDisabled);

        // When
        Result result = client.page("searchMode=composed-of-number&" +
                "unknownIngs=4&unknownIngsRel=le&" +
                "goodIngs=2&goodIngsRel=ge&" +
                "limit=50&offset=0&" +
                "orderBy=name&orderBySort=asc&" +
                "minIngs=1&maxIngs=5&" +
                "inIngs[0]=1&inIngs[1]=2&exIngs[0]=4&exIngs[1]=2");

        // Then
        assertThat(statusOf(result), equalTo(OK));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetByIngredients_ComposedOf_Commons() {
        // When
        Result result = client.page("searchMode=composed-of-number&" +
                "unknownIngs=4&unknownIngsRel=le&" +
                "goodIngs=2&goodIngsRel=ge&" +
                "limit=50&offset=0&" +
                "orderBy=name&orderBySort=asc&" +
                "minIngs=1&maxIngs=5&" +
                "inIngs[0]=1&inIngs[1]=3&exIngs[0]=5");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOf(result), hasSize(2));
        assertThat(recipeIdsOf(result), containsInAnyOrder(1L, 2L));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetByIngredients_ComposedOf_CommonsWithTags() {
        // When
        Result result = client.page("searchMode=composed-of-number&" +
                "unknownIngs=4&unknownIngsRel=le&" +
                "goodIngs=2&goodIngsRel=ge&" +
                "limit=50&offset=0&" +
                "orderBy=name&orderBySort=asc&" +
                "minIngs=1&maxIngs=5&" +
                "inIngs[0]=1&inIngs[1]=3&exIngTags[0]=6");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOf(result), hasSize(2));
        assertThat(recipeIdsOf(result), containsInAnyOrder(1L, 2L));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetByIngredients_ComposedOfStrict() {
        // When
        Result result = client.page("searchMode=composed-of-ratio&" +
                "goodIngsRatio=1.0&" +
                "limit=50&offset=0&" +
                "orderBy=name&orderBySort=asc&" +
                "minIngs=1&maxIngs=4&" +
                "inIngs[0]=1&inIngs[1]=2&inIngs[2]=3&inIngs[3]=4");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOf(result), hasSize(2));
        assertThat(recipeIdsOf(result), containsInAnyOrder(1L, 5L));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetByIngredients_ComposedOfStrictTagsOnly() {
        // When
        Result result = client.page("searchMode=composed-of-ratio&" +
                "goodIngsRatio=1.0&" +
                "limit=50&offset=0&" +
                "orderBy=name&orderBySort=asc&" +
                "minIngs=1&maxIngs=4&" +
                "inIngs[0]=10&inIngTags[0]=1&inIngTags[1]=2");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOf(result), hasSize(2));
        assertThat(recipeIdsOf(result), containsInAnyOrder(3L, 5L));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetByIngredients_ComposedOfAnyOf() {
        // When
        Result result = client.page("searchMode=composed-of-number&" +
                "unknownIngs=0&unknownIngsRel=ge&" +
                "goodIngs=0&goodIngsRel=gt&" +
                "limit=50&offset=0&" +
                "orderBy=name&orderBySort=asc&" +
                "minIngs=1&maxIngs=4&inIngs[0]=4");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOf(result), hasSize(2));
        assertThat(recipeIdsOf(result), containsInAnyOrder(3L, 5L));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetByIngredients_ComposedOfAnyOfTagsOnly() {
        // When
        Result result = client.page("searchMode=composed-of-number&" +
                "unknownIngs=0&unknownIngsRel=ge&" +
                "goodIngs=0&goodIngsRel=gt&" +
                "limit=50&offset=0&" +
                "orderBy=name&orderBySort=asc&" +
                "minIngs=1&maxIngs=8&" +
                "inIngTags[0]=5");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOf(result), hasSize(4));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetByIngredients_ComposedOfAnyOfTagsAndIngrs() {
        // When
        Result result = client.page("searchMode=composed-of-number&" +
                "unknownIngs=0&unknownIngsRel=ge&" +
                "goodIngs=0&goodIngsRel=gt&" +
                "limit=50&offset=0&" +
                "orderBy=name&orderBySort=asc&" +
                "minIngs=1&maxIngs=8&" +
                "inIngs[0]=4&inIngTags[0]=1");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOf(result), hasSize(4));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetByIngredients_ComposedOfExact() {
        // When
        Result result = client.page("searchMode=composed-of-number&" +
                "unknownIngs=0&unknownIngsRel=le&" +
                "goodIngs=3&goodIngsRel=eq&" +
                "limit=50&offset=0&" +
                "orderBy=name&orderBySort=asc&" +
                "inIngs[0]=5&inIngs[1]=6&inIngs[2]=7");


        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOf(result), hasSize(1));
        assertThat(recipeIdsOf(result), containsInAnyOrder(4L));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetByIngredients_ComposedOfAtLeast() {
        // When
        Result result = client.page("searchMode=composed-of-number&" +
                "unknownIngs=0&unknownIngsRel=ge&" +
                "goodIngs=2&goodIngsRel=ge&" +
                "limit=50&offset=0&" +
                "orderBy=name&orderBySort=asc&" +
                "inIngs[0]=5&inIngs[1]=6&exIngs[0]=4");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOf(result), hasSize(1));
        assertThat(recipeIdsOf(result), containsInAnyOrder(4L));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetByIngredients_ComposedOfAtLeastWithExTags() {
        // When
        Result result = client.page("searchMode=composed-of-number&" +
                "unknownIngs=0&unknownIngsRel=ge&" +
                "goodIngs=2&goodIngsRel=ge&" +
                "limit=50&offset=0&" +
                "orderBy=name&orderBySort=asc&" +
                "inIngs[0]=5&inIngs[1]=6&exIngs[0]=4&exIngTags[0]=3");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOf(result), hasSize(1));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetByIngredients_FilterbyNames() {
        // When
        Result result = client.page("searchMode=composed-of-number&" +
                "unknownIngs=0&unknownIngsRel=ge&" +
                "goodIngs=2&goodIngsRel=ge&" +
                "limit=50&offset=0&" +
                "orderBy=name&orderBySort=asc&" +
                "inIngs[0]=5&inIngs[1]=6&nameLike=e_3");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOf(result), hasSize(1));
        assertThat(recipeIdsOf(result), containsInAnyOrder(3L));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetByIngredients_FilterbySourcePages() {
        // When
        Result result = client.page("searchMode=composed-of-number&" +
                "unknownIngs=0&unknownIngsRel=ge&" +
                "goodIngs=2&goodIngsRel=ge&" +
                "limit=50&offset=0&" +
                "orderBy=name&orderBySort=asc&" +
                "inIngs[0]=5&inIngs[1]=6&sourcePages[0]=4");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOf(result), hasSize(1));
        assertThat(recipeIdsOf(result), containsInAnyOrder(4L));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testInvalidSearchMode() {
        // When
        Result result = client.page("searchMode=invalid-search-mode&" +
                "unknownIngs=0&unknownIngsRel=ge&" +
                "goodIngs=2&goodIngsRel=ge&" +
                "limit=50&offset=0&" +
                "orderBy=name&orderBySort=asc&" +
                "inIngs[0]=5&inIngs[1]=6&" +
                "sourcePages[0]=4");

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetAll_WithExIngTagsOnly() {
        // When
        Result result = client.page("exIngTags[0]=6");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOf(result), hasSize(3));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetAll() {
        // When
        Result result = client.page();

        // Then
        assertThat(recipeIdsOf(result), hasSize(greaterThan(4)));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetSingle() {
        // When
        Result result = client.single(3L, 0L);

        // Then
        assertThat(singleRecipeIdOf(result), equalTo(3L));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetSingle_NotFound() {
        // When
        Result result = client.single(-4L, 0L);

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetSourcePages() {
        // When
        Result result = sourcePagesClient.all();

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(itemsSizeOf(result), equalTo(4));
        assertThat(totalCountOf(result), equalTo(4));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testPaging_Backward() {
        // Given
        createRecipesInDbForPaging();

        String queryParams = "offset=100";
        for (int i = 4; i >= 0; i--) {
            Result result = client.page(queryParams);

            // Then
            assertThat(statusOf(result), equalTo(OK));
            assertThat(itemsSizeOf(result), greaterThan(0));

            queryParams = "limit=10&offset=" + (i * 10);
        }
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testPaging_Forward() {
        // Given
        createRecipesInDbForPaging();

        String queryParams = "";
        for (int i = 0; i <= 4; i++) {
            // When
            Result result = client.page(queryParams);

            // Then
            assertThat(statusOf(result), equalTo(OK));
            assertThat(itemsSizeOf(result), greaterThan(0));

            queryParams = "limit=10&offset=" + (i * 10);
        }
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetByRatio() {
        // When
        Result result = client.page("searchMode=composed-of-ratio&" +
                "goodIngsRatio=0.6&" +
                "limit=50&offset=0&" +
                "orderBy=name&orderBySort=asc&" +
                "inIngs[0]=2&inIngs[1]=3");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOf(result), hasSize(1));
        assertThat(recipeIdsOf(result), containsInAnyOrder(1L));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testComposedOfNumber_WithAdditionals() {
        // When
        Result result = client.page("searchMode=composed-of-number&" +
                "limit=50&offset=0&" +
                "unknownIngs=0&unknownIngsRel=ge&" +
                "goodIngs=1&goodIngsRel=eq&" +
                "goodAdditionalIngs=1&goodAdditionalIngsRel=ge&" +
                "orderBy=name&orderBySort=asc&" +
                "minIngs=1&maxIngs=5&" +
                "inIngs[0]=4&addIngs[0]=9&addIngs[1]=5&addIngs[2]=4");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOf(result), hasSize(3));
        assertThat(recipeIdsOf(result), containsInAnyOrder(2L, 3L, 5L));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipes-additionals.yml", disableConstraints = true, cleanBefore = true)
    public void testComposedOfNumber_WithAdditionals_Equal() {
        // When
        Result result = client.page("searchMode=composed-of-number&" +
                "limit=50&offset=0&" +
                "unknownIngs=0&unknownIngsRel=ge&" +
                "goodIngs=1&goodIngsRel=eq&" +
                "goodAdditionalIngs=2&goodAdditionalIngsRel=eq&" +
                "orderBy=name&orderBySort=asc&" +
                "minIngs=1&maxIngs=5&" +
                "inIngs[0]=1&addIngs[0]=2&addIngs[1]=3");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOf(result), hasSize(2));
        assertThat(recipeIdsOf(result), containsInAnyOrder(1L, 2L));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testComposedOfNumber_WithAdditionals_WithTags() {
        // When
        Result result = client.page("searchMode=composed-of-number&" +
                "limit=50&offset=0&" +
                "unknownIngs=0&unknownIngsRel=ge&" +
                "goodIngs=1&goodIngsRel=eq&" +
                "goodAdditionalIngs=1&goodAdditionalIngsRel=ge&" +
                "orderBy=name&orderBySort=asc&" +
                "minIngs=1&maxIngs=5&" +
                "inIngs[0]=4&addIngTags[0]=7");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOf(result), hasSize(2));
        assertThat(recipeIdsOf(result), containsInAnyOrder(2L, 3L));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testComposedOfNumber_WithAdditionals_MissingParams() {
        // When
        Result result = client.page("searchMode=composed-of-number&" +
                "limit=50&offset=0&" +
                "unknownIngs=0&unknownIngsRel=ge&" +
                "goodIngs=1&goodIngsRel=eq&" +
                "goodAdditionalIngs=1&goodAdditionalIngsRel=ge&" +
                "orderBy=name&orderBySort=asc&" +
                "minIngs=1&maxIngs=5&" +
                "inIngs[0]=4");

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testComposedOfNumber_WithAdditionals_MissingRelationParam() {
        // When
        Result result = client.page("searchMode=composed-of-number&" +
                "limit=50&offset=0&" +
                "unknownIngs=0&unknownIngsRel=ge&" +
                "goodIngs=1&goodIngsRel=eq&" +
                "goodAdditionalIngs=1&" +
                "orderBy=name&orderBySort=asc&" +
                "minIngs=1&maxIngs=5&" +
                "inIngs[0]=4&addIngTags[0]=7");

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testByRatio_WithAdditionals() {
        // When
        Result result = client.page("searchMode=composed-of-ratio&" +
                "goodIngsRatio=0.9&" +
                "limit=50&offset=0&" +
                "orderBy=name&orderBySort=asc&" +
                "goodAdditionalIngs=1&goodAdditionalIngsRel=ge&" +
                "minIngs=1&maxIngs=5&" +
                "inIngs[0]=1&inIngs[1]=2&inIngs[2]=3&inIngs[3]=4&addIngs[0]=1");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOf(result), hasSize(1));
        assertThat(recipeIdsOf(result), containsInAnyOrder(1L));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/recipes.yml", "datasets/yml/recipes-favorites.yml"}, disableConstraints = true, cleanBefore = true)
    public void testFavoriteRecipes_ComposedOf() {
        // When
        Result result = client.page("useFavoritesOnly=true&" +
                "searchMode=composed-of-number&" +
                "unknownIngs=4&unknownIngsRel=le&" +
                "goodIngs=2&goodIngsRel=ge&" +
                "limit=50&offset=0&" +
                "orderBy=name&orderBySort=asc&" +
                "minIngs=1&maxIngs=5&" +
                "inIngs[0]=1&inIngs[1]=3&exIngs[0]=5", 1L);

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOf(result), hasSize(1));
        assertThat(recipeIdsOf(result), containsInAnyOrder(2L));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/recipes.yml", "datasets/yml/recipes-favorites.yml"}, disableConstraints = true, cleanBefore = true)
    public void testFavoriteRecipes_All() {
        Result result = client.page("useFavoritesOnly=true&" +
                "limit=50&offset=0&" +
                "orderBy=name&orderBySort=asc", 1L);

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOf(result), hasSize(2));
        assertThat(recipeIdsOf(result), containsInAnyOrder(2L, 4L));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/recipes.yml", "datasets/yml/recipes-favorites.yml"}, disableConstraints = true, cleanBefore = true)
    public void testFavoriteRecipes_UseFavoriteTrueJwtNotPresent() {
        // When
        Result result = client.page("useFavoritesOnly=true&" +
                "searchMode=composed-of-number&" +
                "unknownIngs=4&unknownIngsRel=le&" +
                "goodIngs=2&goodIngsRel=ge&" +
                "limit=50&offset=0&" +
                "orderBy=name&orderBySort=asc&" +
                "minIngs=1&maxIngs=5&" +
                "inIngs[0]=1&inIngs[1]=3&exIngs[0]=5");

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/recipes.yml"}, disableConstraints = true, cleanBefore = true)
    public void testFilterByTime() {
        // When
        Result result = client.page("limit=50&offset=0&" +
                "orderBy=name&orderBySort=asc&" +
                "times[0]=0&times[1]=2&" +
                "sourcePages[0]=1&sourcePages[1]=2&sourcePages[2]=3&sourcePages[3]=4");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeIdsOf(result), hasSize(3));
        assertThat(recipeIdsOf(result), containsInAnyOrder(1L, 3L, 4L));
    }

    private void createRecipesInDbForPaging() {
        Measure m = new Measure();
        m.setName("kg");
        Ebean.save(m);

        for (int i = 0; i < 120; i++) {
            Recipe recipe = new Recipe();
            recipe.setName("testRecipe_" + i);
            recipe.setDateAdded(Instant.now());
            recipe.setNumofings(1);
            recipe.setSourcePage(Ebean.createQuery(SourcePage.class).where().eq("id", 1).findOne());
            Ebean.save(recipe);

            RecipeIngredient recipeIngredient = new RecipeIngredient();
            recipeIngredient.setRecipe(recipe);
            recipeIngredient.setMeasure(Ebean.createQuery(Measure.class).findList().get(0));
            recipeIngredient.setAmount(1);
            recipeIngredient.setIngredient(Ebean.createQuery(Ingredient.class).findList().get(0));
            Ebean.save(recipeIngredient);
        }
    }
}
