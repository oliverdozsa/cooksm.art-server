package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.database.rider.core.api.dataset.DataSet;
import controllers.v1.RecipesControllerQuery;
import controllers.v1.routes;
import dto.RecipeSearchCreateUpdateDto;
import org.junit.Rule;
import org.junit.Test;
import play.Logger;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import rules.PlayApplicationWithGuiceDbRider;
import utils.JwtTestUtils;

import java.util.ArrayList;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static play.test.Helpers.*;

public class RecipeSearchesControllerTest {
    @Rule
    public PlayApplicationWithGuiceDbRider application = new PlayApplicationWithGuiceDbRider();

    private static final Logger.ALogger logger = Logger.of(IngredientNamesControllerTest.class);

    @Test
    @DataSet(value = "datasets/yml/recipesearches.yml", disableConstraints = true, cleanBefore = true)
    public void testGetGlobalSearches() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetGlobalSearches");
        logger.info("------------------------------------------------------------------------------------------------");

        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                routes.RecipeSearchesController.globals().url());
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        assertEquals("Number of items is wrong!", 2, resultJson.get("items").size());
    }

    @Test
    @DataSet(value = "datasets/yml/recipesearches.yml", disableConstraints = true, cleanBefore = true)
    public void testGetUser_1() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetUser_1");
        logger.info("------------------------------------------------------------------------------------------------");

        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                routes.RecipeSearchesController.userSearches().url());
        String jwt = JwtTestUtils.createToken(10000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        assertEquals("Number of items is wrong!", 2, resultJson.get("items").size());
    }

    @Test
    @DataSet(value = "datasets/yml/recipesearches.yml", disableConstraints = true, cleanBefore = true)
    public void testGetSingle() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetSingle");
        logger.info("------------------------------------------------------------------------------------------------");

        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                routes.RecipeSearchesController.userSearch(3).url());
        String jwt = JwtTestUtils.createToken(10000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        assertEquals("Name of user search is wrong!", "user 1 search 1", resultJson.get("name").asText());
    }

    @Test
    @DataSet(value = "datasets/yml/recipesearches.yml", disableConstraints = true, cleanBefore = true)
    public void testGet_UserNotFound() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGet_UserNotFound");
        logger.info("------------------------------------------------------------------------------------------------");

        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                routes.RecipeSearchesController.userSearch(3).url());
        String jwt = JwtTestUtils.createToken(10000L, 42L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);
        Result result = route(application.getApplication(), httpRequest);

        assertEquals("Result of request is wrong!", NOT_FOUND, result.status());
    }


    @Test
    @DataSet(value = "datasets/yml/recipesearches.yml", disableConstraints = true, cleanBefore = true)
    public void testGet_NotFound() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGet_NotFound");
        logger.info("------------------------------------------------------------------------------------------------");

        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri("/api/usersearches/5");
        String jwt = JwtTestUtils.createToken(10000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);
        Result result = route(application.getApplication(), httpRequest);
        assertEquals("Result of request is wrong!", NOT_FOUND, result.status());
    }


    @Test
    @DataSet(value = "datasets/yml/recipesearches.yml", disableConstraints = true, cleanBefore = true)
    public void testCreate_NONE() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testCreate_NONE");
        logger.info("------------------------------------------------------------------------------------------------");

        RecipesControllerQuery.Params params = new RecipesControllerQuery.Params();
        JsonNode paramsJson = Json.toJson(params);
        RecipeSearchCreateUpdateDto newSearch = new RecipeSearchCreateUpdateDto("newSearchUser1", paramsJson.toString());

        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(Json.toJson(newSearch))
                .uri(routes.RecipeSearchesController.create().url());

        String jwt = JwtTestUtils.createToken(10000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);

        Result result = route(application.getApplication(), httpRequest);
        assertEquals("Result of request is wrong!", CREATED, result.status());
        assertTrue("Header \"Location\" is not present!", result.header(LOCATION).isPresent());

        // Check newly created recipe search
        httpRequest = new Http.RequestBuilder()
                .method(GET)
                .bodyJson(Json.toJson(newSearch))
                .uri(result.header(LOCATION).get());

        jwt = JwtTestUtils.createToken(10000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);

        result = route(application.getApplication(), httpRequest);
        assertEquals("Result of request is wrong!", OK, result.status());

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        assertEquals("Recipe search name is wrong!", "newSearchUser1", resultJson.get("name").asText());
    }


    @Test
    @DataSet(value = "datasets/yml/recipesearches.yml", disableConstraints = true, cleanBefore = true)
    public void testCreate_COMPOSED_OF() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testCreate_COMPOSED_OF");
        logger.info("------------------------------------------------------------------------------------------------");

        RecipesControllerQuery.Params params = new RecipesControllerQuery.Params();
        params.searchMode = RecipesControllerQuery.SearchMode.COMPOSED_OF.id;
        params.inIngs = new ArrayList<>();
        params.inIngs.add(1L);
        params.unknownIngs = 0;
        params.unknownIngsRel = "gt";
        params.goodIngs = 0;
        params.goodIngsRel = "gt";

        JsonNode paramsJson = Json.toJson(params);
        RecipeSearchCreateUpdateDto newSearch = new RecipeSearchCreateUpdateDto("newSearchUser1", paramsJson.toString());

        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(Json.toJson(newSearch))
                .uri(routes.RecipeSearchesController.create().url());

        String jwt = JwtTestUtils.createToken(10000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);

        Result result = route(application.getApplication(), httpRequest);
        logger.warn("result body = " + contentAsString(result));
        assertEquals("Result of request is wrong!", CREATED, result.status());
    }

    @Test
    @DataSet(value = "datasets/yml/recipesearches.yml", disableConstraints = true, cleanBefore = true)
    public void testCreate_COMPOSED_OF_Invalid() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testCreate_COMPOSED_OF_Invalid");
        logger.info("------------------------------------------------------------------------------------------------");

        RecipesControllerQuery.Params params = new RecipesControllerQuery.Params();
        params.searchMode = RecipesControllerQuery.SearchMode.COMPOSED_OF.id;
        params.inIngs = new ArrayList<>();
        params.inIngs.add(1L);

        JsonNode paramsJson = Json.toJson(params);

        RecipeSearchCreateUpdateDto newSearch = new RecipeSearchCreateUpdateDto("newSearchUser1", paramsJson.toString());

        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(Json.toJson(newSearch))
                .uri(routes.RecipeSearchesController.create().url());
        String jwt = JwtTestUtils.createToken(10000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);

        Result result = route(application.getApplication(), httpRequest);
        assertEquals("Result of request is wrong!", BAD_REQUEST, result.status());
    }

    @Test
    @DataSet(value = "datasets/yml/recipesearches.yml", disableConstraints = true, cleanBefore = true)
    public void testCreate_InvalidParams() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testCreate_InvalidParams");
        logger.info("------------------------------------------------------------------------------------------------");

        RecipesControllerQuery.Params params = new RecipesControllerQuery.Params();
        params.searchMode = 4284;

        JsonNode paramsJson = Json.toJson(params);

        RecipeSearchCreateUpdateDto newSearch = new RecipeSearchCreateUpdateDto("newSearchUser1", paramsJson.toString());

        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(Json.toJson(newSearch))
                .uri(routes.RecipeSearchesController.create().url());
        String jwt = JwtTestUtils.createToken(10000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);

        Result result = route(application.getApplication(), httpRequest);
        assertEquals("Result of request is wrong!", BAD_REQUEST, result.status());
    }

    @Test
    @DataSet(value = "datasets/yml/recipesearches.yml", disableConstraints = true, cleanBefore = true)
    public void testUpdate() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testUpdate");
        logger.info("------------------------------------------------------------------------------------------------");

        RecipesControllerQuery.Params params = new RecipesControllerQuery.Params();

        JsonNode paramsJson = Json.toJson(params);

        RecipeSearchCreateUpdateDto newSearch =
                new RecipeSearchCreateUpdateDto("newSearchUser1", paramsJson.toString());

        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(PUT)
                .bodyJson(Json.toJson(newSearch))
                .uri(routes.RecipeSearchesController.update(3).url());
        String jwt = JwtTestUtils.createToken(10000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);

        Result result = route(application.getApplication(), httpRequest);
        assertEquals("Result of request is wrong!", NO_CONTENT, result.status());

        httpRequest = new Http.RequestBuilder()
                .method(GET)
                .bodyJson(Json.toJson(newSearch))
                .uri(routes.RecipeSearchesController.userSearch(3).url());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);

        result = route(application.getApplication(), httpRequest);
        JsonNode resultJson = Json.parse(contentAsString(result));
        assertEquals("Recipe search name is wrong!", "newSearchUser1", resultJson.get("name").asText());
    }


    @Test
    @DataSet(value = "datasets/yml/recipesearches.yml", disableConstraints = true, cleanBefore = true)
    public void testUpdate_InvalidParams() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testUpdate_InvalidParams");
        logger.info("------------------------------------------------------------------------------------------------");

        RecipesControllerQuery.Params params = new RecipesControllerQuery.Params();
        params.searchMode = 4842;

        JsonNode paramsJson = Json.toJson(params);

        RecipeSearchCreateUpdateDto newSearch =
                new RecipeSearchCreateUpdateDto("newSearchUser1", paramsJson.toString());

        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(PUT)
                .bodyJson(Json.toJson(newSearch))
                .uri(routes.RecipeSearchesController.update(3).url());
        String jwt = JwtTestUtils.createToken(10000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);

        Result result = route(application.getApplication(), httpRequest);
        assertEquals("Result of request is wrong!", BAD_REQUEST, result.status());
    }


    @Test
    @DataSet(value = "datasets/yml/recipesearches.yml", disableConstraints = true, cleanBefore = true)
    public void testUpdate_OtherUser() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testUpdate_OtherUser");
        logger.info("------------------------------------------------------------------------------------------------");

        RecipesControllerQuery.Params params = new RecipesControllerQuery.Params();

        JsonNode paramsJson = Json.toJson(params);

        RecipeSearchCreateUpdateDto newSearch =
                new RecipeSearchCreateUpdateDto("newSearchUser1", paramsJson.toString());

        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(PUT)
                .bodyJson(Json.toJson(newSearch))
                .uri(routes.RecipeSearchesController.update(5).url());
        String jwt = JwtTestUtils.createToken(10000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);

        Result result = route(application.getApplication(), httpRequest);
        assertEquals("Result of request is wrong!", BAD_REQUEST, result.status());
    }

    @Test
    @DataSet(value = "datasets/yml/recipesearches.yml", disableConstraints = true, cleanBefore = true)
    public void testCreate_LimitReached() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testCreate_LimitReached");
        logger.info("------------------------------------------------------------------------------------------------");

        RecipesControllerQuery.Params params = new RecipesControllerQuery.Params();

        JsonNode paramsJson = Json.toJson(params);

        Result result = null;

        int maxNumOfSearches = application.getApplication().config().getInt("receptnekem.usersearches.maxperuser");
        for (int i = 0; i < maxNumOfSearches + 1; i++) {
            RecipeSearchCreateUpdateDto newSearch =
                    new RecipeSearchCreateUpdateDto("newSearchUser3_" + i, paramsJson.toString());
            Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                    .method(POST)
                    .bodyJson(Json.toJson(newSearch))
                    .uri(routes.RecipeSearchesController.create().url());
            String jwt = JwtTestUtils.createToken(10000L, 3L, application.getApplication().config());
            JwtTestUtils.addJwtTokenTo(httpRequest, jwt);
            result = route(application.getApplication(), httpRequest);
            if (i < maxNumOfSearches) {
                assertEquals("Result of request is wrong!", CREATED, result.status());
            } else {
                assertEquals("Result of request is wrong!", BAD_REQUEST, result.status());
            }
        }
    }

    @Test
    @DataSet(value = "datasets/yml/recipesearches.yml", disableConstraints = true, cleanBefore = true)
    public void testDelete() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testDelete");
        logger.info("------------------------------------------------------------------------------------------------");

        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(DELETE)
                .uri(routes.RecipeSearchesController.delete(3).url());
        String jwt = JwtTestUtils.createToken(10000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);

        Result result = route(application.getApplication(), httpRequest);
        assertEquals("Result of request is wrong!", NO_CONTENT, result.status());

        httpRequest = new Http.RequestBuilder()
                .method(GET)
                .uri(routes.RecipeSearchesController.userSearch(3).url());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);
        result = route(application.getApplication(), httpRequest);

        assertEquals("Result of request is wrong!", NOT_FOUND, result.status());
    }

    @Test
    @DataSet(value = "datasets/yml/recipesearches.yml", disableConstraints = true, cleanBefore = true)
    public void testDelete_Other() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testDelete_Other");
        logger.info("------------------------------------------------------------------------------------------------");


        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(DELETE)
                .uri(routes.RecipeSearchesController.delete(5).url());
        String jwt = JwtTestUtils.createToken(10000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);

        Result result = route(application.getApplication(), httpRequest);
        assertEquals("Result of request is wrong!", BAD_REQUEST, result.status());
    }

    @Test
    @DataSet(value = "datasets/yml/recipesearches.yml", disableConstraints = true, cleanBefore = true)
    public void testDelete_NotExisting() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testDelete_NotExisting");
        logger.info("------------------------------------------------------------------------------------------------");

        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(DELETE)
                .uri(routes.RecipeSearchesController.delete(42).url());
        String jwt = JwtTestUtils.createToken(10000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);

        Result result = route(application.getApplication(), httpRequest);
        assertEquals("Result of request is wrong!", NOT_FOUND, result.status());
    }

    @Test
    @DataSet(value = "datasets/yml/recipesearches.yml", disableConstraints = true, cleanBefore = true)
    public void testGetSingle_DoesntOwn(){
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetSingle_DoesntOwn");
        logger.info("------------------------------------------------------------------------------------------------");

        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(
                routes.RecipeSearchesController.userSearch(2).url());
        String jwt = JwtTestUtils.createToken(10000L, 3L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);
        Result result = route(application.getApplication(), httpRequest);

        assertEquals("Result of request is wrong!", NOT_FOUND, result.status());
    }
}
