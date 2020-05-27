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
                .uri(locationOpt.get());
        JwtTestUtils.addJwtTokenTo(httpRequestGet, jwtToken);

        result = route(application.getApplication(), httpRequestGet);

        assertEquals(OK, result.status());
        String resultJsonStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultJsonStr);
        assertEquals("someName", resultJson.get("name").asText());
        assertNotNull("ID field is missing!", resultJson.get("id"));
    }

    @Test
    @DataSet(value = "datasets/yml/usersearches-base.yml", disableConstraints = true, cleanBefore = true)
    public void testGetAll() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetAll");
        logger.info("------------------------------------------------------------------------------------------------");

        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(GET)
                .uri(routes.UserSearchesController.create().url());

        // TODO
        assertTrue(false);
    }

    @Test
    @DataSet(value = "datasets/yml/usersearches-base.yml", disableConstraints = true, cleanBefore = true)
    public void testUpdate() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testUpdate");
        logger.info("------------------------------------------------------------------------------------------------");

        // TODO
        JsonNode jsonNode = null;
        Long id = null;

        Http.RequestBuilder httpRequestUpdate = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(jsonNode)
                .uri(routes.UserSearchesController.update(id).url());

        // TODO
        assertTrue(false);
    }

    @Test
    @DataSet(value = "datasets/yml/usersearches-base.yml", disableConstraints = true, cleanBefore = true)
    public void testCreate_InvalidName() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testCreate_InvalidName");
        logger.info("------------------------------------------------------------------------------------------------");

        // TODO
        JsonNode jsonNode = null;

        Http.RequestBuilder httpRequestCreate = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(jsonNode)
                .uri(routes.UserSearchesController.create().url());

        // TODO
        assertTrue(false);
    }

    @Test
    @DataSet(value = "datasets/yml/usersearches-base.yml", disableConstraints = true, cleanBefore = true)
    public void testCreate_InvalidQuery() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testCreate_InvalidQuery");
        logger.info("------------------------------------------------------------------------------------------------");

        // TODO
        JsonNode jsonNode = null;

        Http.RequestBuilder httpRequestCreate = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(jsonNode)
                .uri(routes.UserSearchesController.create().url());

        // TODO
        assertTrue(false);
    }

    @Test
    @DataSet(value = "datasets/yml/usersearches-base.yml", disableConstraints = true, cleanBefore = true)
    public void testUpdate_InvalidName() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testUpdate_InvalidName");
        logger.info("------------------------------------------------------------------------------------------------");

        // TODO
        assertTrue(false);
    }

    @Test
    @DataSet(value = "datasets/yml/usersearches-base.yml", disableConstraints = true, cleanBefore = true)
    public void testUpdate_InvalidQuery() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testUpdate_InvalidQuery");
        logger.info("------------------------------------------------------------------------------------------------");

        // TODO
        JsonNode jsonNode = null;
        Long id = null;

        Http.RequestBuilder httpRequestUpdate = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(jsonNode)
                .uri(routes.UserSearchesController.update(id).url());

        // TODO
        assertTrue(false);
    }

    @Test
    @DataSet(value = "datasets/yml/usersearches-base.yml", disableConstraints = true, cleanBefore = true)
    public void testDelete() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testDelete");
        logger.info("------------------------------------------------------------------------------------------------");

        // TODO
        Long id = null;

        Http.RequestBuilder httpRequestUpdate = new Http.RequestBuilder()
                .method(DELETE)
                .uri(routes.UserSearchesController.delete(id).url());

        // TODO
        assertTrue(false);
    }

    @Test
    @DataSet(value = "datasets/yml/usersearches-base.yml", disableConstraints = true, cleanBefore = true)
    public void testDelete_InvalidId() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testDelete_InvalidId");
        logger.info("------------------------------------------------------------------------------------------------");

        // TODO
        Long id = null;

        Http.RequestBuilder httpRequestUpdate = new Http.RequestBuilder()
                .method(DELETE)
                .uri(routes.UserSearchesController.delete(id).url());

        // TODO
        assertTrue(false);
    }
}
