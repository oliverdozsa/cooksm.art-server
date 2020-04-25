package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.database.rider.core.api.dataset.DataSet;
import controllers.v1.routes;
import io.ebean.Ebean;
import models.entities.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import play.Logger;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import rules.PlayApplicationWithGuiceDbRider;

import java.time.Instant;

import static junit.framework.TestCase.*;
import static play.test.Helpers.*;

public class RecipesControllerTest {
    @Rule
    public PlayApplicationWithGuiceDbRider application = new PlayApplicationWithGuiceDbRider();

    private static final Logger.ALogger logger = Logger.of(IngredientNamesControllerTest.class);

    @Before
    public void before() {
        Ebean.createSqlUpdate("update recipe " +
                "set numofings = (select count(*) from recipe_ingredient where recipe.id = recipe_ingredient.recipe_id)")
                .execute();
    }

    @Test
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetByIngredients_ComposedOf_ExcludedOverlaps() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetByIngredients_ComposedOf_ExcludedOverlaps");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "searchMode=composed-of-number&limit=50&unknownIngs=4&unknownIngsRel=le&goodIngs=2&goodIngsRel=ge&offset=0&orderBy=name&orderBySort=asc&minIngs=1&maxIngs=5&inIngs[0]=1&inIngs[1]=2&exIngs[0]=4&exIngs[1]=5";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                routes.RecipesController.pageRecipes().url() + "?" + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        resultJson = resultJson.get("items");

        assertEquals("Number of items is wrong!", 1, resultJson.size());
        assertEquals("Wrong recipe id!", 1L, resultJson.get(0).get("id").asLong());
    }

    @Test
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetByIngredients_IncludedExcludedNotMutEx() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetByIngredients_IncludedExcludedNotMutEx");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "searchMode=composed-of-number&unknownIngs=4&unknownIngsRel=le&goodIngs=2&goodIngsRel=ge&limit=50&unknownIngs=4&offset=0&orderBy=name&orderBySort=asc&minIngs=1&maxIngs=5&inIngs[0]=1&inIngs[1]=2&exIngs[0]=4&exIngs[1]=2";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                routes.RecipesController.pageRecipes().url() + "?" + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        assertEquals("Unexpected request status!", BAD_REQUEST, result.status());
    }

    @Test
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetByIngredients_ComposedOf_Commons() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetByIngredients_ComposedOf_Commons");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "searchMode=composed-of-number&unknownIngs=4&unknownIngsRel=le&goodIngs=2&goodIngsRel=ge&limit=50&offset=0&orderBy=name&orderBySort=asc&&minIngs=1&maxIngs=5&inIngs[0]=1&inIngs[1]=3&exIngs[0]=5";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                routes.RecipesController.pageRecipes().url() + "?" + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        resultJson = resultJson.get("items");

        assertEquals("Number of items is wrong!", 2, resultJson.size());
        assertEquals("Unexpected recipe!", 1L, resultJson.get(0).get("id").asLong());
        assertEquals("Unexpected recipe!", 2L, resultJson.get(1).get("id").asLong());
    }

    @Test
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetByIngredients_ComposedOf_CommonsWithTags() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetByIngredients_ComposedOf_CommonsWithTags");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "searchMode=composed-of-number&unknownIngs=4&unknownIngsRel=le&goodIngs=2&goodIngsRel=ge&limit=50&offset=0&orderBy=name&orderBySort=asc&minIngs=1&maxIngs=5&inIngs[0]=1&inIngs[1]=3&exIngTags[0]=6";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                routes.RecipesController.pageRecipes().url() + "?" + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        resultJson = resultJson.get("items");

        assertEquals("Number of items is wrong!", 2, resultJson.size());
        assertEquals("Unexpected recipe!", 1L, resultJson.get(0).get("id").asLong());
        assertEquals("Unexpected recipe!", 2L, resultJson.get(1).get("id").asLong());
    }

    @Test
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetByIngredients_ComposedOfStrict() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetByIngredients_ComposedOfStrict");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "searchMode=composed-of-ratio&goodIngsRatio=1.0&limit=50&offset=0&orderBy=name&orderBySort=asc&isAdditiveIngs=true&minIngs=1&maxIngs=4&inIngs[0]=1&inIngs[1]=2&inIngs[2]=3&inIngs[3]=4";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                routes.RecipesController.pageRecipes().url() + "?" + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        resultJson = resultJson.get("items");

        assertEquals("Number of items is wrong!", 2, resultJson.size());
        assertEquals("Unexpected recipe!", 1L, resultJson.get(0).get("id").asLong());
        assertEquals("Unexpected recipe!", 5L, resultJson.get(1).get("id").asLong());
    }

    @Test
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetByIngredients_ComposedOfStrictTagsOnly() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetByIngredients_ComposedOfStrictTagsOnly");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "searchMode=composed-of-ratio&goodIngsRatio=1.0&limit=50&offset=0&orderBy=name&orderBySort=asc&minIngs=1&maxIngs=4&inIngs[0]=10&inIngTags[0]=1&inIngTags[1]=2";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                routes.RecipesController.pageRecipes().url() + "?" + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        resultJson = resultJson.get("items");

        assertEquals("Number of items is wrong!", 2, resultJson.size());
        assertEquals("Unexpected recipe!", 3L, resultJson.get(0).get("id").asLong());
    }

    @Test
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetByIngredients_ComposedOfAnyOf() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetByIngredients_ComposedOfAnyOf");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "searchMode=composed-of-number&unknownIngs=0&unknownIngsRel=ge&goodIngs=0&goodIngsRel=gt&limit=50&offset=0&orderBy=name&orderBySort=asc&minIngs=1&maxIngs=4&inIngs[0]=4";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                routes.RecipesController.pageRecipes().url() + "?" + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        resultJson = resultJson.get("items");

        assertEquals(2, resultJson.size());
        assertEquals("Unexpected recipe!", 3L, resultJson.get(0).get("id").asLong());
        assertEquals("Unexpected recipe!", 5L, resultJson.get(1).get("id").asLong());
    }

    @Test
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetByIngredients_ComposedOfAnyOfTagsOnly() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetByIngredients_ComposedOfAnyOfTagsOnly");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "searchMode=composed-of-number&unknownIngs=0&unknownIngsRel=ge&goodIngs=0&goodIngsRel=gt&limit=50&offset=0&orderBy=name&orderBySort=asc&minIngs=1&maxIngs=8&inIngTags[0]=5";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                routes.RecipesController.pageRecipes().url() + "?" + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        resultJson = resultJson.get("items");

        assertEquals("Result of request is wrong!", OK, result.status());
        assertEquals("Number of items is wrong!", 4, resultJson.size());
    }

    @Test
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetByIngredients_ComposedOfAnyOfTagsAndIngrs() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetByIngredients_ComposedOfAnyOfTagsAndIngrs");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "searchMode=composed-of-number&unknownIngs=0&unknownIngsRel=ge&goodIngs=0&goodIngsRel=gt&limit=50&offset=0&orderBy=name&orderBySort=asc&minIngs=1&maxIngs=8&inIngs[0]=4&inIngTags[0]=1";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                routes.RecipesController.pageRecipes().url() + "?" + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        resultJson = resultJson.get("items");

        assertEquals("Result of request is wrong!", OK, result.status());
        assertEquals("Number of items is wrong!", 4, resultJson.size());
    }

    @Test
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetByIngredients_ComposedOfExact() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetByIngredients_ComposedOfExact");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "searchMode=composed-of-number&unknownIngs=0&unknownIngsRel=le&goodIngs=3&goodIngsRel=eq&limit=50&offset=0&orderBy=name&orderBySort=asc&inIngs[0]=5&inIngs[1]=6&inIngs[2]=7";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                routes.RecipesController.pageRecipes().url() + "?" + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        resultJson = resultJson.get("items");

        assertEquals("Number of items is wrong!", 1, resultJson.size());
        assertEquals("Unexpected recipe!", 4L, resultJson.get(0).get("id").asLong());
    }

    @Test
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetByIngredients_ComposedOfAtLeast() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetByIngredients_ComposedOfAtLeast");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "searchMode=composed-of-number&unknownIngs=0&unknownIngsRel=ge&goodIngs=2&goodIngsRel=ge&limit=50&offset=0&orderBy=name&orderBySort=asc&inIngs[0]=5&inIngs[1]=6&exIngs[0]=4";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                routes.RecipesController.pageRecipes().url() + "?" + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        resultJson = resultJson.get("items");

        assertEquals("Number of items is wrong!", 1, resultJson.size());
        assertEquals("Unexpected recipe!", 4L, resultJson.get(0).get("id").asLong());
    }

    @Test
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetByIngredients_ComposedOfAtLeastWithExTags() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetByIngredients_ComposedOfAtLeastWithExTags");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "searchMode=composed-of-number&unknownIngs=0&unknownIngsRel=ge&goodIngs=2&goodIngsRel=ge&limit=50&offset=0&orderBy=name&orderBySort=asc&inIngs[0]=5&inIngs[1]=6&exIngs[0]=4&exIngTags[0]=3";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                routes.RecipesController.pageRecipes().url() + "?" + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        resultJson = resultJson.get("items");

        assertEquals("Number of items is wrong!", 1, resultJson.size());
    }

    @Test
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetByIngredients_FilterbyNames() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetByIngredients_FilterbyNames");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "searchMode=composed-of-number&unknownIngs=0&unknownIngsRel=ge&goodIngs=2&goodIngsRel=ge&limit=50&offset=0&orderBy=name&orderBySort=asc&inIngs[0]=5&inIngs[1]=6&nameLike=e_3";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                routes.RecipesController.pageRecipes().url() + "?" + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        resultJson = resultJson.get("items");

        assertEquals("Number of items is wrong!", 1, resultJson.size());
        assertEquals("Unexpected recipe!", 3L, resultJson.get(0).get("id").asLong());
    }

    @Test
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetByIngredients_FilterbySourcePages() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetByIngredients_FilterbySourcePages");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "searchMode=composed-of-number&unknownIngs=0&unknownIngsRel=ge&goodIngs=2&goodIngsRel=ge&limit=50&offset=0&orderBy=name&orderBySort=asc&inIngs[0]=5&inIngs[1]=6&sourcePages[0]=4";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                routes.RecipesController.pageRecipes().url() + "?" + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        resultJson = resultJson.get("items");

        assertEquals("Number of items is wrong!", 1, resultJson.size());
        assertEquals("Unexpected recipe!", 4L, resultJson.get(0).get("id").asLong());
    }

    @Test
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testInvalidSearchMode() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testInvalidSearchMode");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "searchMode=invalid-search-mode&unknownIngs=0&unknownIngsRel=ge&goodIngs=2&goodIngsRel=ge&limit=50&offset=0&orderBy=name&orderBySort=asc&inIngs[0]=5&inIngs[1]=6&sourcePages[0]=4";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                routes.RecipesController.pageRecipes().url() + "?" + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetAll_WithExIngTagsOnly() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetAll_WithExIngTagsOnly");
        logger.info("------------------------------------------------------------------------------------------------");

        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                routes.RecipesController.pageRecipes().url() + "?exIngTags[0]=6");
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        resultJson = resultJson.get("items");

        assertEquals("Number of items is wrong!", 3, resultJson.size());
    }

    @Test
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetAll() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetAll");
        logger.info("------------------------------------------------------------------------------------------------");

        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                routes.RecipesController.pageRecipes().url());
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        logger.warn("resultContentStr = {}", resultContentStr);
        JsonNode resultJson = Json.parse(resultContentStr);
        resultJson = resultJson.get("items");


        assertTrue("Number of items is wrong!", resultJson.size() >= 4);
    }

    @Test
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetSingle() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetSingle");
        logger.info("------------------------------------------------------------------------------------------------");

        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(routes.RecipesController.singleRecipe(3, 0).url());
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);

        assertEquals("Unexpected recipe!", 3, resultJson.get("id").asInt());
    }

    @Test
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetSingle_NotFound() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetSingle_NotFound");
        logger.info("------------------------------------------------------------------------------------------------");

        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(routes.RecipesController.singleRecipe(-4, 0).url());
        Result result = route(application.getApplication(), httpRequest);

        assertEquals("Result of request is wrong!", NOT_FOUND, result.status());
    }

    @Test
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetSourcePages() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetSourcePages");
        logger.info("------------------------------------------------------------------------------------------------");

        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(routes.SourcePagesController.sourcePages().url());
        Result result = route(application.getApplication(), httpRequest);

        assertEquals(OK, result.status());

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);

        assertEquals("Number of items is wrong!", 4, resultJson.get("items").size());
        assertEquals("Total count is wrong!", 4, resultJson.get("totalCount").asInt());
    }

    @Test
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testPaging() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testPaging");
        logger.info("------------------------------------------------------------------------------------------------");

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

        String queryParams = "";

        // Forward paging.
        int i;
        for (i = 0; i <= 4; i++) {
            Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                    routes.RecipesController.pageRecipes().url() + queryParams);
            Result result = route(application.getApplication(), httpRequest);

            String resultContentStr = contentAsString(result);
            JsonNode resultJson = Json.parse(resultContentStr);

            assertNotNull("No result items are present!", resultJson.get("items"));
            assertNotNull("No total count is present!", resultJson.get("totalCount"));

            queryParams = "?limit=10&offset=" + (i * 10);
        }

        queryParams = "?offset=100";

        // Backward paging.
        for (i = 4; i >= 0; i--) {
            Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                    routes.RecipesController.pageRecipes().url()  + queryParams);
            Result result = route(application.getApplication(), httpRequest);

            String resultContentStr = contentAsString(result);
            JsonNode resultJson = Json.parse(resultContentStr);

            assertNotNull("No result items are present!", resultJson.get("items"));
            assertNotNull("Total count is not present!", resultJson.get("totalCount"));

            queryParams = "?limit=10&offset=" + (i * 10);
        }
    }

    @Test
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetByRatio() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetByRatio");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "?searchMode=composed-of-ratio&goodIngsRatio=0.6&limit=50&offset=0&orderBy=name&orderBySort=asc&inIngs[0]=2&inIngs[1]=3";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                routes.RecipesController.pageRecipes().url() + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        resultJson = resultJson.get("items");

        assertEquals("Number of items is wrong!", 1, resultJson.size());
        assertEquals("Unexpected recipe!", 1L, resultJson.get(0).get("id").asLong());
    }

    @Test
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testComposedOfNumber_WithAdditionals() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testComposedOfNumber_WithAdditionals");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "?searchMode=composed-of-number&limit=50&unknownIngs=0&unknownIngsRel=ge&goodIngs=1&goodIngsRel=eq&goodAdditionalIngs=1&offset=0&orderBy=name&orderBySort=asc&minIngs=1&maxIngs=5&inIngs[0]=4&addIngs[0]=9&addIngs[1]=5";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                routes.RecipesController.pageRecipes().url() + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        resultJson = resultJson.get("items");

        assertEquals("Number of items is wrong!", 2, resultJson.size());
        assertEquals("Unexpected recipe!", 2L, resultJson.get(0).get("id").asLong());
        assertEquals("Unexpected recipe!", 3L, resultJson.get(1).get("id").asLong());
    }

    @Test
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testComposedOfNumber_WithAdditionals_WithTags() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testComposedOfNumber_WithAdditionals_WithTags");
        logger.info("------------------------------------------------------------------------------------------------");


        String reqParams = "?searchMode=composed-of-number&limit=50&unknownIngs=0&unknownIngsRel=ge&goodIngs=1&goodIngsRel=eq&goodAdditionalIngs=1&offset=0&orderBy=name&orderBySort=asc&minIngs=1&maxIngs=5&inIngs[0]=4&addIngTags[0]=7";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                routes.RecipesController.pageRecipes().url() + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        resultJson = resultJson.get("items");

        assertEquals("Number of items is wrong!", 2, resultJson.size());
        assertEquals("Unexpected recipe!", 2L, resultJson.get(0).get("id").asLong());
        assertEquals("Unexpected recipe!", 3L, resultJson.get(1).get("id").asLong());
    }

    @Test
    @DataSet(value = "datasets/yml/recipes.yml", disableConstraints = true, cleanBefore = true)
    public void testComposedOfNumber_WithAdditionals_MissingParams() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testComposedOfNumber_WithAdditionals_MissingParams");
        logger.info("------------------------------------------------------------------------------------------------");


        String reqParams = "?searchMode=composed-of-number&limit=50&unknownIngs=0&unknownIngsRel=ge&goodIngs=1&goodIngsRel=eq&goodAdditionalIngs=1&offset=0&orderBy=name&orderBySort=asc&minIngs=1&maxIngs=5&inIngs[0]=4";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                routes.RecipesController.pageRecipes().url() + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        assertEquals("Unexpected status!", BAD_REQUEST, result.status());
    }
}
