package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.database.rider.core.api.dataset.DataSet;
import controllers.v1.routes;
import org.junit.Rule;
import org.junit.Test;
import play.Logger;
import play.mvc.Http;
import rules.PlayApplicationWithGuiceDbRider;

import static junit.framework.TestCase.assertTrue;
import static play.mvc.Http.HttpVerbs.GET;
import static play.test.Helpers.DELETE;
import static play.test.Helpers.POST;

public class UserSearchesControllerTest {
    @Rule
    public PlayApplicationWithGuiceDbRider application = new PlayApplicationWithGuiceDbRider();

    private static final Logger.ALogger logger = Logger.of(UserSearchesControllerTest.class);

    @Test
    @DataSet(value = "datasets/yml/usersearches-base.yml", disableConstraints = true, cleanBefore = true)
    public void testCreate() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testCreate");
        logger.info("------------------------------------------------------------------------------------------------");

        // TODO
        JsonNode jsonNode = null;

        Http.RequestBuilder httpRequestCreate = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(jsonNode)
                .uri(routes.UserSearchesController.create().url());

        // TODO: create, the query the created search based on LOCATION header
        assertTrue(false);
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
