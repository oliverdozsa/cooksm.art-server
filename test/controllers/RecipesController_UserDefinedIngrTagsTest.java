package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.database.rider.core.api.dataset.DataSet;
import controllers.v1.routes;
import io.ebean.Ebean;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import play.Logger;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import rules.PlayApplicationWithGuiceDbRider;
import utils.JwtTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

public class RecipesController_UserDefinedIngrTagsTest {
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
    @DataSet(value = {"datasets/yml/recipes.yml", "datasets/yml/user-defined-tags.yml"}, disableConstraints = true, cleanBefore = true)
    public void testRecipesWithUserDefinedIngrTags_Included() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testRecipesWithUserDefinedIngrTags_Included");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "searchMode=composed-of-number&unknownIngs=0&unknownIngsRel=ge&goodIngs=2&goodIngsRel=ge&limit=50&offset=0&orderBy=name&orderBySort=asc&minIngs=1&maxIngs=8&inIngTags[0]=1&inIngTags[1]=9";
        String jwt = JwtTestUtils.createToken(1000L, 1L, application.getApplication().config());
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                routes.RecipesController.pageRecipes().url() + "?" + reqParams);
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        resultJson = resultJson.get("items");

        assertEquals("Result of request is wrong!", OK, result.status());
        assertEquals("Number of items is wrong!", 2, resultJson.size());

        List<Long> recipeIds = extractRecipeIds(resultJson);
        logger.warn("recipeIds = {}", recipeIds);
        assertTrue(recipeIds.containsAll(Arrays.asList(3L, 2L)));
    }

    @Test
    @DataSet(value = {"datasets/yml/recipes.yml", "datasets/yml/user-defined-tags.yml"}, disableConstraints = true, cleanBefore = true)
    public void testRecipesWithUserDefinedIngrTags_Included_Unauth() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testRecipesWithUserDefinedIngrTags_Included_Unauth");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "searchMode=composed-of-number&unknownIngs=0&unknownIngsRel=ge&goodIngs=2&goodIngsRel=ge&limit=50&offset=0&orderBy=name&orderBySort=asc&minIngs=1&maxIngs=8&inIngTags[0]=1&inIngTags[1]=9";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                routes.RecipesController.pageRecipes().url() + "?" + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        resultJson = resultJson.get("items");

        assertEquals("Result of request is wrong!", OK, result.status());
        assertEquals("Number of items is wrong!", 1, resultJson.size());

        List<Long> recipeIds = extractRecipeIds(resultJson);
        logger.warn("recipeIds = {}", recipeIds);
        assertTrue(recipeIds.containsAll(Arrays.asList(3L)));
    }

    @Test
    @DataSet(value = {"datasets/yml/recipes.yml", "datasets/yml/user-defined-tags.yml"}, disableConstraints = true, cleanBefore = true)
    public void testRecipesWithUserDefinedIngrTags_Included_OtherUsersTags() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testRecipesWithUserDefinedIngrTags_Included_OtherUsersTags");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "searchMode=composed-of-number&unknownIngs=0&unknownIngsRel=ge&goodIngs=2&goodIngsRel=ge&limit=50&offset=0&orderBy=name&orderBySort=asc&minIngs=1&maxIngs=8&inIngTags[0]=1&inIngTags[1]=9&inIngTags[2]=10";
        String jwt = JwtTestUtils.createToken(1000L, 1L, application.getApplication().config());
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                routes.RecipesController.pageRecipes().url() + "?" + reqParams);
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        resultJson = resultJson.get("items");

        assertEquals("Result of request is wrong!", OK, result.status());
        assertEquals("Number of items is wrong!", 2, resultJson.size());

        List<Long> recipeIds = extractRecipeIds(resultJson);
        logger.warn("recipeIds = {}", recipeIds);
        assertTrue(recipeIds.containsAll(Arrays.asList(3L, 2L)));
    }

    @Test
    @DataSet(value = {"datasets/yml/recipes.yml", "datasets/yml/user-defined-tags.yml"}, disableConstraints = true, cleanBefore = true)
    public void testRecipesWithUserDefinedIngrTags_Excluded() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testRecipesWithUserDefinedIngrTags_Excluded");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "limit=50&offset=0&orderBy=name&orderBySort=asc&minIngs=1&maxIngs=8&exIngTags[0]=6&exIngTags[1]=8";
        String jwt = JwtTestUtils.createToken(1000L, 1L, application.getApplication().config());
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                routes.RecipesController.pageRecipes().url() + "?" + reqParams);
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        resultJson = resultJson.get("items");

        assertEquals("Result of request is wrong!", OK, result.status());
        assertEquals("Number of items is wrong!", 1, resultJson.size());

        List<Long> recipeIds = extractRecipeIds(resultJson);
        logger.warn("recipeIds = {}", recipeIds);
        assertTrue(recipeIds.containsAll(Arrays.asList(5L)));
    }

    @Test
    @DataSet(value = {"datasets/yml/recipes.yml", "datasets/yml/user-defined-tags.yml"}, disableConstraints = true, cleanBefore = true)
    public void testRecipesWithUserDefinedIngrTags_Excluded_Unauth() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testRecipesWithUserDefinedIngrTags_Excluded_Unauth");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "limit=50&offset=0&orderBy=name&orderBySort=asc&minIngs=1&maxIngs=8&exIngTags[0]=6&exIngTags[1]=8";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                routes.RecipesController.pageRecipes().url() + "?" + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        resultJson = resultJson.get("items");

        assertEquals("Result of request is wrong!", OK, result.status());
        assertEquals("Number of items is wrong!", 3, resultJson.size());

        List<Long> recipeIds = extractRecipeIds(resultJson);
        logger.warn("recipeIds = {}", recipeIds);
        assertTrue(recipeIds.containsAll(Arrays.asList(5L, 1L, 2L)));
    }

    @Test
    @DataSet(value = {"datasets/yml/recipes.yml", "datasets/yml/user-defined-tags.yml"}, disableConstraints = true, cleanBefore = true)
    public void testRecipesWithUserDefinedIngrTags_Excluded_OtherUsersTag() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testRecipesWithUserDefinedIngrTags_Excluded_OtherUsersTag");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "limit=50&offset=0&orderBy=name&orderBySort=asc&minIngs=1&maxIngs=8&exIngTags[0]=7&exIngTags[1]=8&exIngTags[2]=10";
        String jwt = JwtTestUtils.createToken(1000L, 2L, application.getApplication().config());
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                routes.RecipesController.pageRecipes().url() + "?" + reqParams);
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        resultJson = resultJson.get("items");

        assertEquals("Result of request is wrong!", OK, result.status());
        assertEquals("Number of items is wrong!", 2, resultJson.size());

        List<Long> recipeIds = extractRecipeIds(resultJson);
        logger.warn("recipeIds = {}", recipeIds);
        assertTrue(recipeIds.containsAll(Arrays.asList(5L, 1L)));
    }

    @Test
    @DataSet(value = {"datasets/yml/recipes.yml", "datasets/yml/user-defined-tags.yml"}, disableConstraints = true, cleanBefore = true)
    public void testRecipesWithUserDefinedIngrTags_Additionals() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testRecipesWithUserDefinedIngrTags_Additionals");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "searchMode=composed-of-number&limit=50&unknownIngs=0&unknownIngsRel=ge&goodIngs=1&goodIngsRel=eq&goodAdditionalIngs=1&goodAdditionalIngsRel=ge&offset=0&orderBy=name&orderBySort=asc&minIngs=1&maxIngs=5&inIngs[0]=4&addIngTags[0]=6&addIngTags[1]=8";
        String jwt = JwtTestUtils.createToken(1000L, 1L, application.getApplication().config());
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                routes.RecipesController.pageRecipes().url() + "?" + reqParams);
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        resultJson = resultJson.get("items");

        assertEquals("Result of request is wrong!", OK, result.status());
        assertEquals("Number of items is wrong!", 2, resultJson.size());

        List<Long> recipeIds = extractRecipeIds(resultJson);
        logger.warn("recipeIds = {}", recipeIds);
        assertTrue(recipeIds.containsAll(Arrays.asList(2L, 3L)));
    }

    @Test
    @DataSet(value = {"datasets/yml/recipes.yml", "datasets/yml/user-defined-tags.yml"}, disableConstraints = true, cleanBefore = true)
    public void testRecipesWithUserDefinedIngrTags_Additionals_Unauth() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testRecipesWithUserDefinedIngrTags_Additionals_Unauth");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "searchMode=composed-of-number&limit=50&unknownIngs=0&unknownIngsRel=ge&goodIngs=1&goodIngsRel=eq&goodAdditionalIngs=1&goodAdditionalIngsRel=ge&offset=0&orderBy=name&orderBySort=asc&minIngs=1&maxIngs=5&inIngs[0]=4&addIngTags[0]=6&addIngTags[1]=8";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                routes.RecipesController.pageRecipes().url() + "?" + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        resultJson = resultJson.get("items");

        assertEquals("Result of request is wrong!", OK, result.status());
        assertEquals("Number of items is wrong!", 1, resultJson.size());

        List<Long> recipeIds = extractRecipeIds(resultJson);
        logger.warn("recipeIds = {}", recipeIds);
        assertTrue(recipeIds.containsAll(Arrays.asList(3L)));
    }

    @Test
    @DataSet(value = {"datasets/yml/recipes.yml", "datasets/yml/user-defined-tags.yml"}, disableConstraints = true, cleanBefore = true)
    public void testRecipesWithUserDefinedIngrTags_Additionals_OtherUsersTag() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testRecipesWithUserDefinedIngrTags_Additionals_OtherUsersTag");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "searchMode=composed-of-number&limit=50&unknownIngs=0&unknownIngsRel=ge&goodIngs=1&goodIngsRel=eq&goodAdditionalIngs=1&goodAdditionalIngsRel=ge&offset=0&orderBy=name&orderBySort=asc&minIngs=1&maxIngs=5&inIngs[0]=4&&addIngTags[0]=9&addIngTags[1]=10";
        String jwt = JwtTestUtils.createToken(1000L, 1L, application.getApplication().config());
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                routes.RecipesController.pageRecipes().url() + "?" + reqParams);
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        resultJson = resultJson.get("items");

        assertEquals("Result of request is wrong!", OK, result.status());
        assertEquals("Number of items is wrong!", 1, resultJson.size());

        List<Long> recipeIds = extractRecipeIds(resultJson);
        logger.warn("recipeIds = {}", recipeIds);
        assertTrue(recipeIds.containsAll(Arrays.asList(2L)));
    }

    private static List<Long> extractRecipeIds(JsonNode result) {
        List<Long> ids = new ArrayList<>();

        ArrayNode arrayNode = (ArrayNode) result;
        arrayNode.forEach(n -> ids.add(n.get("id").asLong()));

        return ids;
    }
}
