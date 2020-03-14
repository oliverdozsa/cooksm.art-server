package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.database.rider.core.api.dataset.DataSet;
import controllers.v1.routes;
import org.junit.Rule;
import org.junit.Test;
import play.Logger;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import rules.PlayApplicationWithGuiceDbRider;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static play.test.Helpers.*;

public class IngredientTagsControllerTest {
    @Rule
    public PlayApplicationWithGuiceDbRider application = new PlayApplicationWithGuiceDbRider();

    private static final Logger.ALogger logger = Logger.of(IngredientTagsControllerTest.class);

    @Test
    @DataSet(value = "datasets/yml/ingredienttags.yml", disableConstraints = true, cleanBefore = true)
    public void testListTags() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testListTags");
        logger.info("------------------------------------------------------------------------------------------------");

        String queryParams = "languageId=1&nameLike=_tag_";
        Http.RequestBuilder request = new Http.RequestBuilder().method(GET)
                .uri(routes.IngredientTagsController.pageTags().url() + "?" + queryParams);

        Result result = route(application.getApplication(), request);

        assertEquals("Status is not success", Http.Status.OK, result.status());

        String jsonStr = contentAsString(result);
        JsonNode json = Json.parse(jsonStr);

        assertEquals("Total count is incorrect!",7, json.get("totalCount").asInt());
    }

    @Test
    @DataSet(value = "datasets/yml/ingredienttags.yml", disableConstraints = true, cleanBefore = true)
    public void testPaging() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testPaging");
        logger.info("------------------------------------------------------------------------------------------------");

        int limit = 2;
        int offset = 0;
        boolean lastPageNotReached = true;

        while (lastPageNotReached) {
            String queryParams = String.format("languageId=1&nameLike=_tag_&limit=%d&offset=%d", limit, offset);
            Http.RequestBuilder request = new Http.RequestBuilder().method(GET)
                    .uri(routes.IngredientTagsController.pageTags().url() + "?" + queryParams);

            Result result = route(application.getApplication(), request);

            assertEquals("Status is not success", Http.Status.OK, result.status());

            String jsonStr = contentAsString(result);
            JsonNode json = Json.parse(jsonStr);

            assertEquals("Total count is incorrect!",7, json.get("totalCount").asInt());
            assertTrue("Items' size is not <= limit!", json.get("items").size() <= limit);

            int totalCount = json.get("totalCount").asInt();
            lastPageNotReached = offset + limit < totalCount;
            offset += limit;
        }
    }

    @Test
    @DataSet(value = "datasets/yml/ingredienttags.yml", disableConstraints = true, cleanBefore = true)
    public void testInvalidRequest() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testInvalidRequest");
        logger.info("------------------------------------------------------------------------------------------------");

        String invalidLenghtNameLikeParams = "languageId=1&nameLike=a";
        Http.RequestBuilder request = new Http.RequestBuilder().method(GET)
                .uri(routes.IngredientTagsController.pageTags().url() + "?" + invalidLenghtNameLikeParams);

        Result result = route(application.getApplication(), request);
        assertEquals("Invalid request's response status is not 400!", BAD_REQUEST, result.status());
    }
}
