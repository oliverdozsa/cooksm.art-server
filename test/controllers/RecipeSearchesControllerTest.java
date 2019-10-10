package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.database.rider.core.api.dataset.DataSet;
import controllers.v1.RecipesControllerQuery;
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
import static play.test.Helpers.*;

public class RecipeSearchesControllerTest {
    @Rule
    public PlayApplicationWithGuiceDbRider application = new PlayApplicationWithGuiceDbRider();

    private static final Logger.ALogger logger = Logger.of(IngredientNamesControllerTest.class);
    private static final String RESOURCE_PATH_GLOBAL = "/v1/searches";
    private static final String RESOURCE_PATH_USER = "/v1/usersearches";

    @Test
    @DataSet(value = "datasets/yml/recipesearches.yml")
    public void testGetGlobalSearches() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetGlobalSearches");
        logger.info("------------------------------------------------------------------------------------------------");

        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(RESOURCE_PATH_GLOBAL);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        assertEquals(2, resultJson.get("items").size());
    }

    @Test
    public void testGetUserSearches() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetUserSearches");
        logger.info("------------------------------------------------------------------------------------------------");

        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(RESOURCE_PATH_USER);
        String jwt = JwtTestUtils.createToken(1000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        assertEquals(2, resultJson.get("items").size());
    }

    @Test
    public void testGetUserSearches_UserNotFound() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetUserSearches_UserNotFound");
        logger.info("------------------------------------------------------------------------------------------------");

        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(RESOURCE_PATH_USER);
        String jwt = JwtTestUtils.createToken(1000L, 42L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);
        Result result = route(application.getApplication(), httpRequest);
        assertEquals(NOT_FOUND, result.status());
    }

    @Test
    public void testGetUserSearch() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetUserSearch");
        logger.info("------------------------------------------------------------------------------------------------");

        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(RESOURCE_PATH_USER + "/3");
        String jwt = JwtTestUtils.createToken(1000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        assertEquals("user 1 search 1", resultJson.get("name").asText());
    }

    @Test
    public void testGetUserSearch_UserNotFound() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetUserSearch");
        logger.info("------------------------------------------------------------------------------------------------");

        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(RESOURCE_PATH_USER + "/3");
        String jwt = JwtTestUtils.createToken(1000L, 42L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);
        Result result = route(application.getApplication(), httpRequest);

        assertEquals(NOT_FOUND, result.status());
    }

    /*
    @Test
    public void testGetUserSearchNotFound() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetUserSearchNotFound");
        logger.info("------------------------------------------------------------------------------------------------");

        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri("/api/usersearches/5");
        String jwt = JwtTestUtils.createToken(1000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);
        Result result = route(application.getApplication(), httpRequest);
        assertEquals(NOT_FOUND, result.status());
    }

    @Test
    public void testUserSearchCreate_NONE() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testUserSearchCreate_NONE");
        logger.info("------------------------------------------------------------------------------------------------");

        RecipesControllerQuery.Params params = new RecipesControllerQuery.Params();
        JsonNode paramsJson = Json.toJson(params);
        RecipeSearchCreateUpdateDto newSearch = new RecipeSearchCreateUpdateDto("newSearchUser1", paramsJson.toString());

        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(Json.toJson(newSearch))
                .uri(RESOURCE_PATH_USER);

        String jwt = JwtTestUtils.createToken(1000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);

        Result result = route(application.getApplication(), httpRequest);
        assertEquals(CREATED, result.status());
    }

    @Test
    public void testUserSearchCreate_COMPOSED_OF() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testUserSearchCreate_COMPOSED_OF");
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
                .uri(RESOURCE_PATH_USER);

        String jwt = JwtTestUtils.createToken(1000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);

        Result result = route(application.getApplication(), httpRequest);
        logger.warn("result body = " + contentAsString(result));
        assertEquals(CREATED, result.status());
    }

    @Test
    public void testUserSearchCreate_COMPOSED_OF_Invalid() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testUserSearchCreate_COMPOSED_OF_Invalid");
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
                .uri(RESOURCE_PATH_USER);
        String jwt = JwtTestUtils.createToken(1000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);

        Result result = route(application.getApplication(), httpRequest);
        assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    public void testUserSearchCreate_InvalidParams() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testUserSearchCreate_InvalidParams");
        logger.info("------------------------------------------------------------------------------------------------");

        RecipesControllerQuery.Params params = new RecipesControllerQuery.Params();
        params.searchMode = 4284;

        JsonNode paramsJson = Json.toJson(params);

        RecipeSearchCreateUpdateDto newSearch = new RecipeSearchCreateUpdateDto("newSearchUser1", paramsJson.toString());

        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(Json.toJson(newSearch))
                .uri(RESOURCE_PATH_USER);
        String jwt = JwtTestUtils.createToken(1000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);

        Result result = route(application.getApplication(), httpRequest);
        assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    public void testUserSearchUpdate() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testUserSearchUpdate");
        logger.info("------------------------------------------------------------------------------------------------");

        RecipesControllerQuery.Params params = new RecipesControllerQuery.Params();

        JsonNode paramsJson = Json.toJson(params);

        RecipeSearchCreateUpdateDto newSearch =
                new RecipeSearchCreateUpdateDto("newSearchUser1", paramsJson.toString());

        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(PUT)
                .bodyJson(Json.toJson(newSearch))
                .uri(RESOURCE_PATH_USER + "/3");
        String jwt = JwtTestUtils.createToken(1000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);

        Result result = route(application.getApplication(), httpRequest);
        assertEquals(OK, result.status());

        httpRequest = new Http.RequestBuilder()
                .method(GET)
                .bodyJson(Json.toJson(newSearch))
                .uri("/api/usersearches/3");
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);

        result = route(application.getApplication(), httpRequest);
        JsonNode resultJson = Json.parse(contentAsString(result));
        assertEquals("newSearchUser1", resultJson.get("name").asText());
    }

    @Test
    public void testUserSearchUpdate_InvalidParams() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testUserSearchUpdate_InvalidParams");
        logger.info("------------------------------------------------------------------------------------------------");

        RecipesControllerQuery.Params params = new RecipesControllerQuery.Params();
        params.searchMode = 4842;

        JsonNode paramsJson = Json.toJson(params);

        RecipeSearchCreateUpdateDto newSearch =
                new RecipeSearchCreateUpdateDto("newSearchUser1", paramsJson.toString());

        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(PUT)
                .bodyJson(Json.toJson(newSearch))
                .uri(RESOURCE_PATH_USER + "/3");
        String jwt = JwtTestUtils.createToken(1000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);

        Result result = route(application.getApplication(), httpRequest);
        assertEquals(OK, result.status());
        assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    public void testUserSearchUpdate_OtherUser() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testUserSearchUpdate_OtherUser");
        logger.info("------------------------------------------------------------------------------------------------");

        RecipesControllerQuery.Params params = new RecipesControllerQuery.Params();

        JsonNode paramsJson = Json.toJson(params);

        RecipeSearchCreateUpdateDto newSearch =
                new RecipeSearchCreateUpdateDto("newSearchUser1", paramsJson.toString());

        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(PUT)
                .bodyJson(Json.toJson(newSearch))
                .uri(RESOURCE_PATH_USER + "/5");
        String jwt = JwtTestUtils.createToken(1000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);

        Result result = route(application.getApplication(), httpRequest);
        assertEquals(NOT_FOUND, result.status());
    }

    @Test
    public void testUserSearchCreate_LimitReached() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testUserSearchCreate_LimitReached");
        logger.info("------------------------------------------------------------------------------------------------");

        RecipesControllerQuery.Params params = new RecipesControllerQuery.Params();

        JsonNode paramsJson = Json.toJson(params);

        Result result = null;

        int maxNumOfSearches = application.getApplication().config().getInt("receptnekem.usersearches.maxperuser");
        for (int i = 0; i < maxNumOfSearches; i++) {
            RecipeSearchCreateUpdateDto newSearch =
                    new RecipeSearchCreateUpdateDto("newSearchUser1" + i, paramsJson.toString());
            Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                    .method(POST)
                    .bodyJson(Json.toJson(newSearch))
                    .uri(RESOURCE_PATH_USER);
            String jwt = JwtTestUtils.createToken(1000L, 1L, application.getApplication().config());
            JwtTestUtils.addJwtTokenTo(httpRequest, jwt);
            result = route(application.getApplication(), httpRequest);
        }

        assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    public void testUserSearchDelete() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testUserSearchDelete");
        logger.info("------------------------------------------------------------------------------------------------");

        // TODO: create first?
        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(DELETE)
                .uri(RESOURCE_PATH_USER + "/3");
        String jwt = JwtTestUtils.createToken(1000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);

        Result result = route(application.getApplication(), httpRequest);
        assertEquals(OK, result.status());

        httpRequest = new Http.RequestBuilder()
                .method(GET)
                .uri(RESOURCE_PATH_USER + "/3");
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);
        result = route(application.getApplication(), httpRequest);

        assertEquals(NOT_FOUND, result.status());
    }

    @Test
    public void testUserSearchDelete_Other() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testUserSearchDelete_Other");
        logger.info("------------------------------------------------------------------------------------------------");


        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(DELETE)
                .uri(RESOURCE_PATH_USER + "/5");
        String jwt = JwtTestUtils.createToken(1000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);

        Result result = route(application.getApplication(), httpRequest);
        assertEquals(NOT_FOUND, result.status());
    }

    @Test
    public void testUserSearchDelete_NotExisting() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testUserSearchDelete_NotExisting");
        logger.info("------------------------------------------------------------------------------------------------");


        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(DELETE)
                .uri(RESOURCE_PATH_USER + "/42");
        String jwt = JwtTestUtils.createToken(1000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwt);

        Result result = route(application.getApplication(), httpRequest);
        assertEquals(NOT_FOUND, result.status());
    }*/
}
