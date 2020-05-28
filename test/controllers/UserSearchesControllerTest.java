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
import rules.PlayApplicationWithGuiceDbRider;
import utils.JwtTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static junit.framework.TestCase.*;
import static play.mvc.Http.HttpVerbs.GET;
import static play.test.Helpers.*;

public class UserSearchesControllerTest {
    @Rule
    public PlayApplicationWithGuiceDbRider application = new PlayApplicationWithGuiceDbRider();

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
                "  name: \"someName\"," +
                "  query: {" +
                "    \"searchMode\": \"composed-of-number\"," +
                "    \"goodIngs\": 3," +
                "    \"goodIngsRel\": \"ge\"," +
                "    \"unknownIngs\": \"0\"," +
                "    \"unknownIngsRel\": \"ge\"," +
                "    \"goodAdditionalIngs\": 2," +
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
    }

    @Test
    @DataSet(value = {"datasets/yml/usersearches-base.yml", "datasets/yml/usersearches.yml"}, disableConstraints = true, cleanBefore = true)
    public void testGetAll() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetAll");
        logger.info("------------------------------------------------------------------------------------------------");

        // TODO: At least 3 users in DB, user 2 has 3 searches, and query those.
        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(GET)
                .uri(routes.UserSearchesController.create().url());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwtToken);

        Result result = route(application.getApplication(), httpRequest);

        assertEquals(OK, result.status());
        String jsonResultStr = contentAsString(result);
        JsonNode jsonResult = Json.parse(jsonResultStr);

        assertEquals(3, jsonResult.size());
        List<String> queryNames = new ArrayList<>();
        jsonResult.forEach(q -> queryNames.add(q.get("name").asText()));
        assertTrue(queryNames.containsAll(Arrays.asList("user2query1", "user2query2", "user2query3")));
    }

    @Test
    @DataSet(value = {"datasets/yml/usersearches-base.yml", "datasets/yml/usersearches.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUpdate() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testUpdate");
        logger.info("------------------------------------------------------------------------------------------------");

        // TODO: update existing user1query1 to user1query1renamed
        String jsonStr = "{" +
                "  name: \"user1query1renamed\"," +
                "  query: {" +
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
                .method(PUT)
                .bodyJson(jsonNode)
                .uri(routes.UserSearchesController.update(id).url());
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
        JsonNode queryJson = resultJson.get("query");
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
                "  name: \"s\"," +
                "  query: {" +
                "    \"searchMode\": \"composed-of-number\"," +
                "    \"goodIngs\": 3," +
                "    \"goodIngsRel\": \"ge\"," +
                "    \"unknownIngs\": \"0\"," +
                "    \"unknownIngsRel\": \"ge\"," +
                "    \"goodAdditionalIngs\": 2," +
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
                "  name: \"someName\"," +
                "  query: {" +
                "    \"searchMode\": \"composed-of-number\"," +
                "    \"goodIngs\": 3," +
                "    \"goodIngsRel\": \"ge\"," +
                "    \"unknownIngs\": \"0\"," +
                "    \"unknownIngsRel\": \"ge\"," +
                "    \"goodAdditionalIngs\": 2," +
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
    @DataSet(value = {"datasets/yml/usersearches-base.yml", "datasets/yml/usersearches.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUpdate_InvalidName() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testUpdate_InvalidName");
        logger.info("------------------------------------------------------------------------------------------------");

        String jsonStr = "{" +
                "  name: \"u\"," +
                "  query: {" +
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
                .method(PUT)
                .bodyJson(jsonNode)
                .uri(routes.UserSearchesController.update(id).url());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwtToken);

        Result response = route(application.getApplication(), httpRequest);
        assertEquals(BAD_REQUEST, response.status());
    }

    @Test
    @DataSet(value = {"datasets/yml/usersearches-base.yml", "datasets/yml/usersearches.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUpdate_InvalidQuery() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testUpdate_InvalidQuery");
        logger.info("------------------------------------------------------------------------------------------------");

        // Missing included ingredients
        String jsonStr = "{" +
                "  name: \"user1query1renamed\"," +
                "  query: {" +
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
                .method(PUT)
                .bodyJson(jsonNode)
                .uri(routes.UserSearchesController.update(id).url());
        JwtTestUtils.addJwtTokenTo(httpRequest, jwtToken);

        Result response = route(application.getApplication(), httpRequest);
        assertEquals(BAD_REQUEST, response.status());
    }

    @Test
    @DataSet(value = {"datasets/yml/usersearches-base.yml", "datasets/yml/usersearches.yml"}, disableConstraints = true, cleanBefore = true)
    public void testDelete() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testDelete");
        logger.info("------------------------------------------------------------------------------------------------");

        // TODO
        Long id = 2L;

        Http.RequestBuilder httpRequestDelete = new Http.RequestBuilder()
                .method(DELETE)
                .uri(routes.UserSearchesController.delete(id).url());

        Result response = route(application.getApplication(), httpRequestDelete);
        assertEquals(NO_CONTENT, response.status());

        Http.RequestBuilder httpRequestGet = new Http.RequestBuilder()
                .method(GET)
                .uri(routes.UserSearchesController.single(id).url());
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

        Result response = route(application.getApplication(), httpRequest);
        assertEquals(NOT_FOUND, response.status());
    }
}
