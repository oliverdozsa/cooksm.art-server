package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.database.rider.core.api.dataset.DataSet;
import controllers.v1.routes;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import play.Logger;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import rules.PlayApplicationWithGuiceDbRiderRule;
import utils.Base62Utils;
import utils.JwtTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static junit.framework.TestCase.*;
import static play.mvc.Http.HttpVerbs.GET;
import static play.mvc.Http.HttpVerbs.PATCH;
import static play.test.Helpers.*;

public class UserSearchesControllerTest {
    @Rule
    public PlayApplicationWithGuiceDbRiderRule application = new PlayApplicationWithGuiceDbRiderRule();

    private static final Logger.ALogger logger = Logger.of(UserSearchesControllerTest.class);

    String jwtToken;

    @Before
    public void setup() {
        jwtToken = JwtTestUtils.createToken(10000L, 1L, application.getApplication().config());
    }

    @Test
    @DataSet(value = "datasets/yml/usersearches-base.yml", disableConstraints = true, cleanBefore = true)
    public void testCreate() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testCreate");
        logger.info("------------------------------------------------------------------------------------------------");

        String jsonStr = "{" +
                "  \"name\": \"someName\"," +
                "  \"query\": {" +
                "    \"searchMode\": \"composed-of-number\"," +
                "    \"goodIngs\": 3," +
                "    \"goodIngsRel\": \"ge\"," +
                "    \"unknownIngs\": \"0\"," +
                "    \"unknownIngsRel\": \"ge\"," +
                "    \"goodAdditionalIngs\": 2," +
                "    \"goodAdditionalIngsRel\": \"ge\"," +
                "    \"inIngs\": [1, 2, 3]," +
                "    \"inIngTags\": [1]," +
                "    \"exIngs\": [4, 7]," +
                "    \"exIngTags\": [2]," +
                "    \"addIngs\": [5]," +
                "    \"addIngTags\": [6]," +
                "    \"sourcePages\": [1, 2]," +
                "    \"useFavoritesOnly\": true" +
                "  }" +
                "}";
        JsonNode jsonNode = Json.parse(jsonStr);

        Http.RequestBuilder httpRequestCreate = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(jsonNode)
                .uri(routes.UserSearchesController.create().url());
        JwtTestUtils.addJwtTokenTo(httpRequestCreate, jwtToken);

        Result result = route(application.getApplication(), httpRequestCreate);

        assertEquals("Result status is not created.", CREATED, result.status());
        Optional<String> locationOpt = result.header(LOCATION);
        assertTrue("Location is not present!", locationOpt.isPresent());
        String location = locationOpt.get();

        Http.RequestBuilder httpRequestGet = new Http.RequestBuilder()
                .method(GET)
                .uri(location);
        JwtTestUtils.addJwtTokenTo(httpRequestGet, jwtToken);

        result = route(application.getApplication(), httpRequestGet);

        assertEquals(OK, result.status());
        String resultJsonStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultJsonStr);
        assertEquals("someName", resultJson.get("name").asText());
        assertNotNull("ID field is missing!", resultJson.get("id"));

        String searchId = resultJson.get("searchId").asText();
        httpRequestGet = new Http.RequestBuilder()
                .method(GET)
                .uri(routes.RecipeSearchesController.single(searchId).url());

        result = route(application.getApplication(), httpRequestGet);
        resultJsonStr = contentAsString(result);
        resultJson = Json.parse(resultJsonStr);
        JsonNode queryJson = resultJson.get("query");
        assertTrue("Use favorites only is not true!", queryJson.get("useFavoritesOnly").asBoolean());
    }

    @Test
    @DataSet(value = {"datasets/yml/usersearches-base.yml", "datasets/yml/usersearches.yml"}, disableConstraints = true, cleanBefore = true)
    public void testGetAll() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetAll");
        logger.info("------------------------------------------------------------------------------------------------");

        Long userId = 2L;
        String jwtUser2 = JwtTestUtils.createToken(10000L, userId, application.getApplication().config());

        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(GET)
                .uri(routes.UserSearchesController.all().url());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwtUser2);

        Result result = route(application.getApplication(), httpRequest);

        logger.warn("result content = {}", contentAsString(result));

        assertEquals(OK, result.status());
        String jsonResultStr = contentAsString(result);
        JsonNode jsonResult = Json.parse(jsonResultStr);

        assertEquals(3, jsonResult.size());
        List<String> queryNames = new ArrayList<>();
        List<String> searchIds = new ArrayList<>();
        jsonResult.forEach(q -> queryNames.add(q.get("name").asText()));
        jsonResult.forEach(q -> searchIds.add(q.get("searchId").asText()));
        assertTrue(queryNames.containsAll(Arrays.asList("user2query1", "user2query2", "user2query3")));
        assertTrue(searchIds.containsAll(Arrays.asList(Base62Utils.encode(239329L), Base62Utils.encode(239330L), Base62Utils.encode(239331L))));
    }

    @Test
    @DataSet(value = {"datasets/yml/usersearches-base.yml", "datasets/yml/usersearches.yml"}, disableConstraints = true, cleanBefore = true)
    public void testPatchFully() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testPatchFully");
        logger.info("------------------------------------------------------------------------------------------------");

        // update existing user1query1 to user1query1renamed
        String jsonStr = "{" +
                "  \"name\": \"user1query1renamed\"," +
                "  \"query\": {" +
                "    \"searchMode\": \"composed-of-ratio\"," +
                "    \"goodIngsRatio\": 0.6," +
                "    \"inIngs\": [1, 2, 3]," +
                "    \"inIngTags\": [1]," +
                "    \"exIngs\": [4, 7]," +
                "    \"exIngTags\": [2]," +
                "    \"sourcePages\": [1]" +
                "  }" +
                "}";
        JsonNode jsonNode = Json.parse(jsonStr);
        Long id = 1L;

        Http.RequestBuilder httpRequestUpdate = new Http.RequestBuilder()
                .method(PATCH)
                .bodyJson(jsonNode)
                .uri(routes.UserSearchesController.patch(id).url());
        JwtTestUtils.addJwtTokenTo(httpRequestUpdate, jwtToken);

        Result response = route(application.getApplication(), httpRequestUpdate);
        assertEquals(NO_CONTENT, response.status());

        Http.RequestBuilder httpRequestGet = new Http.RequestBuilder()
                .method(GET)
                .uri(routes.UserSearchesController.single(id).url());
        JwtTestUtils.addJwtTokenTo(httpRequestGet, jwtToken);

        response = route(application.getApplication(), httpRequestGet);

        assertEquals(OK, response.status());
        String resultJsonStr = contentAsString(response);
        JsonNode resultJson = Json.parse(resultJsonStr);
        assertEquals("user1query1renamed", resultJson.get("name").asText());
        String searchId = resultJson.get("searchId").asText();

        Http.RequestBuilder httpRequestGetRecipeSearch = new Http.RequestBuilder()
                .method(GET)
                .uri(routes.RecipeSearchesController.single(searchId).url());
        JwtTestUtils.addJwtTokenTo(httpRequestGet, jwtToken);

        response = route(application.getApplication(), httpRequestGetRecipeSearch);

        assertEquals(OK, response.status());

        resultJsonStr = contentAsString(response);
        JsonNode responseJson = Json.parse(resultJsonStr);
        JsonNode queryJson = responseJson.get("query");

        assertEquals("composed-of-ratio", queryJson.get("searchMode").asText());
        assertEquals(0.6, queryJson.get("goodIngsRatio").asDouble());
    }

    @Test
    @DataSet(value = "datasets/yml/usersearches-base.yml", disableConstraints = true, cleanBefore = true)
    public void testCreate_InvalidName() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testCreate_InvalidName");
        logger.info("------------------------------------------------------------------------------------------------");

        String jsonStr = "{" +
                "  \"name\": \"s\"," +
                "  \"query\": {" +
                "    \"searchMode\": \"composed-of-number\"," +
                "    \"goodIngs\": 3," +
                "    \"goodIngsRel\": \"ge\"," +
                "    \"unknownIngs\": \"0\"," +
                "    \"unknownIngsRel\": \"ge\"," +
                "    \"goodAdditionalIngs\": 2," +
                "    \"goodAdditionalIngsRel\": \"ge\"," +
                "    \"inIngs\": [1, 2, 3]," +
                "    \"inIngTags\": [1]," +
                "    \"exIngs\": [4, 7]," +
                "    \"exIngTags\": [2]," +
                "    \"addIngs\": [5]," +
                "    \"addIngTags\": [6]," +
                "    \"sourcePages\": [1, 2]" +
                "  }" +
                "}";
        JsonNode jsonNode = Json.parse(jsonStr);

        Http.RequestBuilder httpRequestCreate = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(jsonNode)
                .uri(routes.UserSearchesController.create().url());
        JwtTestUtils.addJwtTokenTo(httpRequestCreate, jwtToken);

        Result result = route(application.getApplication(), httpRequestCreate);
        assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    @DataSet(value = "datasets/yml/usersearches-base.yml", disableConstraints = true, cleanBefore = true)
    public void testCreate_InvalidQuery() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testCreate_InvalidQuery");
        logger.info("------------------------------------------------------------------------------------------------");

        // Missing included ingredients
        String jsonStr = "{" +
                "  \"name\": \"someName\"," +
                "  \"query\": {" +
                "    \"searchMode\": \"composed-of-number\"," +
                "    \"goodIngs\": 3," +
                "    \"goodIngsRel\": \"ge\"," +
                "    \"unknownIngs\": \"0\"," +
                "    \"unknownIngsRel\": \"ge\"," +
                "    \"goodAdditionalIngs\": 2," +
                "    \"goodAdditionalIngsRel\": \"ge\"," +
                "    \"exIngs\": [4, 7]," +
                "    \"exIngTags\": [2]," +
                "    \"addIngs\": [5]," +
                "    \"addIngTags\": [6]," +
                "    \"sourcePages\": [1, 2]" +
                "  }" +
                "}";
        JsonNode jsonNode = Json.parse(jsonStr);

        Http.RequestBuilder httpRequestCreate = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(jsonNode)
                .uri(routes.UserSearchesController.create().url());
        JwtTestUtils.addJwtTokenTo(httpRequestCreate, jwtToken);

        Result result = route(application.getApplication(), httpRequestCreate);
        assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    @DataSet(value = "datasets/yml/usersearches-base.yml", disableConstraints = true, cleanBefore = true)
    public void testCreate_LimitReached() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testCreate_LimitReached");
        logger.info("------------------------------------------------------------------------------------------------");

        String jsonStr = "{" +
                "  \"name\": \"someName\"," +
                "  \"query\": {" +
                "    \"searchMode\": \"composed-of-number\"," +
                "    \"goodIngs\": 3," +
                "    \"goodIngsRel\": \"ge\"," +
                "    \"unknownIngs\": \"0\"," +
                "    \"unknownIngsRel\": \"ge\"," +
                "    \"goodAdditionalIngs\": 2," +
                "    \"goodAdditionalIngsRel\": \"ge\"," +
                "    \"inIngs\": [1, 2, 3]," +
                "    \"inIngTags\": [1]," +
                "    \"exIngs\": [4, 7]," +
                "    \"exIngTags\": [2]," +
                "    \"addIngs\": [5]," +
                "    \"addIngTags\": [6]," +
                "    \"sourcePages\": [1, 2]" +
                "  }" +
                "}";
        JsonNode jsonNode = Json.parse(jsonStr);
        int maxSearches = application.getApplication()
                .config().getInt("receptnekem.usersearches.maxperuser");

        for (int i = 0; i < maxSearches; i++) {
            Http.RequestBuilder httpRequestCreate = new Http.RequestBuilder()
                    .method(POST)
                    .bodyJson(jsonNode)
                    .uri(routes.UserSearchesController.create().url());
            JwtTestUtils.addJwtTokenTo(httpRequestCreate, jwtToken);

            Result result = route(application.getApplication(), httpRequestCreate);
            assertEquals("Result status is not created.", CREATED, result.status());
        }

        Http.RequestBuilder httpRequestCreate = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(jsonNode)
                .uri(routes.UserSearchesController.create().url());
        JwtTestUtils.addJwtTokenTo(httpRequestCreate, jwtToken);

        Result result = route(application.getApplication(), httpRequestCreate);
        assertEquals("Created request over limit!", FORBIDDEN, result.status());
    }

    @Test
    @DataSet(value = {"datasets/yml/usersearches-base.yml", "datasets/yml/usersearches.yml"}, disableConstraints = true, cleanBefore = true)
    public void testPatch_InvalidName() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testPatch_InvalidName");
        logger.info("------------------------------------------------------------------------------------------------");

        String jsonStr = "{" +
                "  \"name\": \"u\"," +
                "  \"query\": {" +
                "    \"searchMode\": \"composed-of-ratio\"," +
                "    \"goodIngsRatio\": 0.6," +
                "    \"inIngs\": [1, 2, 3]," +
                "    \"inIngTags\": [1]," +
                "    \"exIngs\": [4, 7]," +
                "    \"exIngTags\": [2]," +
                "    \"sourcePages\": [1]" +
                "  }" +
                "}";
        JsonNode jsonNode = Json.parse(jsonStr);
        Long id = 1L;

        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(PATCH)
                .bodyJson(jsonNode)
                .uri(routes.UserSearchesController.patch(id).url());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwtToken);

        Result response = route(application.getApplication(), httpRequest);
        assertEquals(BAD_REQUEST, response.status());
    }

    @Test
    @DataSet(value = {"datasets/yml/usersearches-base.yml", "datasets/yml/usersearches.yml"}, disableConstraints = true, cleanBefore = true)
    public void testPatch_InvalidQuery() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testPatch_InvalidQuery");
        logger.info("------------------------------------------------------------------------------------------------");

        // Missing included ingredients
        String jsonStr = "{" +
                "  \"name\": \"user1query1renamed\"," +
                "  \"query\": {" +
                "    \"searchMode\": \"composed-of-ratio\"," +
                "    \"goodIngsRatio\": 0.6," +
                "    \"exIngs\": [4, 7]," +
                "    \"exIngTags\": [2]," +
                "    \"sourcePages\": [1]" +
                "  }" +
                "}";
        JsonNode jsonNode = Json.parse(jsonStr);
        Long id = 1L;

        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(PATCH)
                .bodyJson(jsonNode)
                .uri(routes.UserSearchesController.patch(id).url());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwtToken);

        Result response = route(application.getApplication(), httpRequest);
        assertEquals(BAD_REQUEST, response.status());
    }

    @Test
    @DataSet(value = {"datasets/yml/usersearches-base.yml", "datasets/yml/usersearches.yml"}, disableConstraints = true, cleanBefore = true)
    public void testPatch_OtherUser() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testUpdate_OtherUser");
        logger.info("------------------------------------------------------------------------------------------------");

        // User 2 owns query 3, and user 1 tries to update it
        String jsonStr = "{" +
                "  \"name\": \"user1query1renamed\"," +
                "  \"query\": {" +
                "    \"searchMode\": \"composed-of-ratio\"," +
                "    \"goodIngsRatio\": 0.6," +
                "    \"inIngs\": [1, 2, 3]," +
                "    \"inIngTags\": [1]," +
                "    \"exIngs\": [4, 7]," +
                "    \"exIngTags\": [2]," +
                "    \"sourcePages\": [1]" +
                "  }" +
                "}";
        JsonNode jsonNode = Json.parse(jsonStr);
        Long id = 3L;

        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(PATCH)
                .bodyJson(jsonNode)
                .uri(routes.UserSearchesController.patch(id).url());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwtToken);

        Result response = route(application.getApplication(), httpRequest);
        assertEquals(NOT_FOUND, response.status());
    }

    @Test
    @DataSet(value = {"datasets/yml/usersearches-base.yml", "datasets/yml/usersearches.yml"}, disableConstraints = true, cleanBefore = true)
    public void testDelete() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testDelete");
        logger.info("------------------------------------------------------------------------------------------------");

        Long id = 2L;

        Http.RequestBuilder httpRequestDelete = new Http.RequestBuilder()
                .method(DELETE)
                .uri(routes.UserSearchesController.delete(id).url());
        JwtTestUtils.addJwtTokenTo(httpRequestDelete, jwtToken);

        Result response = route(application.getApplication(), httpRequestDelete);
        assertEquals(NO_CONTENT, response.status());

        Http.RequestBuilder httpRequestGet = new Http.RequestBuilder()
                .method(GET)
                .uri(routes.UserSearchesController.single(id).url());
        JwtTestUtils.addJwtTokenTo(httpRequestGet, jwtToken);
        response = route(application.getApplication(), httpRequestGet);
        assertEquals(NOT_FOUND, response.status());
    }

    @Test
    @DataSet(value = {"datasets/yml/usersearches-base.yml", "datasets/yml/usersearches.yml"}, disableConstraints = true, cleanBefore = true)
    public void testDelete_InvalidId() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testDelete_InvalidId");
        logger.info("------------------------------------------------------------------------------------------------");

        Long id = 42L;

        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(DELETE)
                .uri(routes.UserSearchesController.delete(id).url());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwtToken);

        Result response = route(application.getApplication(), httpRequest);
        assertEquals(NOT_FOUND, response.status());
    }

    @Test
    @DataSet(value = {"datasets/yml/usersearches-base.yml", "datasets/yml/usersearches.yml"}, disableConstraints = true, cleanBefore = true)
    public void testDelete_OtherUser() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testDelete_OtherUser");
        logger.info("------------------------------------------------------------------------------------------------");

        // User 2 owns query 3, and user 1 tries to delete it
        Long id = 3L;

        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(DELETE)
                .uri(routes.UserSearchesController.delete(id).url());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwtToken);

        Result response = route(application.getApplication(), httpRequest);
        assertEquals(NOT_FOUND, response.status());
    }

    @Test
    @DataSet(value = {"datasets/yml/usersearches-base.yml", "datasets/yml/usersearches.yml"}, disableConstraints = true, cleanBefore = true)
    public void testSingle_OtherUser() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testSingle_OtherUser");
        logger.info("------------------------------------------------------------------------------------------------");

        jwtToken = JwtTestUtils.createToken(10000L, 2L, application.getApplication().config());

        Long id = 1L; // User 1 owns user search 1
        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(GET)
                .uri(routes.UserSearchesController.single(id).url());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwtToken);

        Result response = route(application.getApplication(), httpRequest);
        assertEquals(NOT_FOUND, response.status());
    }

    @Test
    @DataSet(value = {"datasets/yml/usersearches-base.yml", "datasets/yml/usersearches.yml"}, disableConstraints = true, cleanBefore = true)
    public void testPatchName() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testPatchName");
        logger.info("------------------------------------------------------------------------------------------------");

        // update existing user1query1 to user1query1renamed
        String jsonStr = "{" +
                "  \"name\": \"user1query1renamed\"" +
                "}";
        JsonNode jsonNode = Json.parse(jsonStr);
        Long id = 1L;

        Http.RequestBuilder httpRequestUpdate = new Http.RequestBuilder()
                .method(PATCH)
                .bodyJson(jsonNode)
                .uri(routes.UserSearchesController.patch(id).url());
        JwtTestUtils.addJwtTokenTo(httpRequestUpdate, jwtToken);

        Result response = route(application.getApplication(), httpRequestUpdate);
        assertEquals(NO_CONTENT, response.status());

        Http.RequestBuilder httpRequestGet = new Http.RequestBuilder()
                .method(GET)
                .uri(routes.UserSearchesController.single(id).url());
        JwtTestUtils.addJwtTokenTo(httpRequestGet, jwtToken);

        response = route(application.getApplication(), httpRequestGet);

        assertEquals(OK, response.status());
        String resultJsonStr = contentAsString(response);
        JsonNode resultJson = Json.parse(resultJsonStr);
        assertEquals("user1query1renamed", resultJson.get("name").asText());
    }

    @Test
    @DataSet(value = {"datasets/yml/usersearches-base.yml", "datasets/yml/usersearches.yml"}, disableConstraints = true, cleanBefore = true)
    public void testPatchQuery() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testPatchQuery");
        logger.info("------------------------------------------------------------------------------------------------");

        String jsonStr = "{" +
                "  \"query\": {" +
                "    \"searchMode\": \"composed-of-ratio\"," +
                "    \"goodIngsRatio\": 0.6," +
                "    \"inIngs\": [1, 2, 3]," +
                "    \"inIngTags\": [1]," +
                "    \"exIngs\": [4, 7]," +
                "    \"exIngTags\": [2]," +
                "    \"sourcePages\": [1]" +
                "  }" +
                "}";
        JsonNode jsonNode = Json.parse(jsonStr);
        Long id = 1L;

        Http.RequestBuilder httpRequestUpdate = new Http.RequestBuilder()
                .method(PATCH)
                .bodyJson(jsonNode)
                .uri(routes.UserSearchesController.patch(id).url());
        JwtTestUtils.addJwtTokenTo(httpRequestUpdate, jwtToken);

        Result response = route(application.getApplication(), httpRequestUpdate);
        assertEquals(NO_CONTENT, response.status());

        Http.RequestBuilder httpRequestGet = new Http.RequestBuilder()
                .method(GET)
                .uri(routes.UserSearchesController.single(id).url());
        JwtTestUtils.addJwtTokenTo(httpRequestGet, jwtToken);

        response = route(application.getApplication(), httpRequestGet);

        assertEquals(OK, response.status());
        String resultJsonStr = contentAsString(response);
        JsonNode resultJson = Json.parse(resultJsonStr);
        assertEquals("user1query1", resultJson.get("name").asText());
        String searchId = resultJson.get("searchId").asText();

        Http.RequestBuilder httpRequestGetRecipeSearch = new Http.RequestBuilder()
                .method(GET)
                .uri(routes.RecipeSearchesController.single(searchId).url());
        JwtTestUtils.addJwtTokenTo(httpRequestGet, jwtToken);

        response = route(application.getApplication(), httpRequestGetRecipeSearch);

        assertEquals(OK, response.status());

        resultJsonStr = contentAsString(response);
        JsonNode responseJson = Json.parse(resultJsonStr);
        JsonNode queryJson = responseJson.get("query");

        assertEquals("composed-of-ratio", queryJson.get("searchMode").asText());
        assertEquals(0.6, queryJson.get("goodIngsRatio").asDouble());
    }
}
