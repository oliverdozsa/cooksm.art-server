package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.database.rider.core.api.dataset.DataSet;
import controllers.v1.routes;
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

import static junit.framework.TestCase.*;
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

        assertEquals("Total count is incorrect!", 7, json.get("totalCount").asInt());
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

            assertEquals("Total count is incorrect!", 7, json.get("totalCount").asInt());
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

    @Test
    @DataSet(value = "datasets/yml/ingredienttags.yml", disableConstraints = true, cleanBefore = true)
    public void testIngredientsOfTags() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testIngredientsOfTags");
        logger.info("------------------------------------------------------------------------------------------------");

        String queryParams = "languageId=1&nameLike=2_tag_2";
        Http.RequestBuilder request = new Http.RequestBuilder().method(GET)
                .uri(routes.IngredientTagsController.pageTags().url() + "?" + queryParams);

        Result result = route(application.getApplication(), request);

        assertEquals("Status is not success", Http.Status.OK, result.status());

        String jsonStr = contentAsString(result);
        JsonNode jsonPage = Json.parse(jsonStr);

        assertEquals(1, jsonPage.get("totalCount").asInt());

        JsonNode jsonTag = jsonPage.get("items").get(0);

        assertNotNull("Ingredients field is not present!", jsonTag.get("ingredients"));
        assertEquals("Number of ingredients of tags is wrong!", 2, jsonTag.get("ingredients").size());
    }

    @Test
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testListTags_UserDefined() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testListTags_UserDefined");
        logger.info("------------------------------------------------------------------------------------------------");

        String queryParams = "languageId=1&nameLike=tag_2";
        String jwt = JwtTestUtils.createToken(1000L, 1L, application.getApplication().config());
        Http.RequestBuilder request = new Http.RequestBuilder().method(GET)
                .uri(routes.IngredientTagsController.pageTags().url() + "?" + queryParams);
        JwtTestUtils.addJwtTokenTo(request, jwt);

        Result result = route(application.getApplication(), request);

        assertEquals("Status is not success", Http.Status.OK, result.status());

        String jsonStr = contentAsString(result);
        JsonNode json = Json.parse(jsonStr);

        logger.warn("result = " + jsonStr);
        assertEquals("Total count is incorrect!", 3, json.get("totalCount").asInt());

        List<Long> tagIds = extractTagIds(json.get("items"));
        assertTrue(tagIds.containsAll(Arrays.asList(6L, 3L, 11L)));
    }

    @Test
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testListTags_UserDefined_Unauth() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testListTags_UserDefined_Unauth");
        logger.info("------------------------------------------------------------------------------------------------");

        String queryParams = "languageId=1&nameLike=tag_2";
        Http.RequestBuilder request = new Http.RequestBuilder().method(GET)
                .uri(routes.IngredientTagsController.pageTags().url() + "?" + queryParams);

        Result result = route(application.getApplication(), request);

        assertEquals("Status is not success", Http.Status.OK, result.status());

        String jsonStr = contentAsString(result);
        JsonNode json = Json.parse(jsonStr);

        assertEquals("Total count is incorrect!", 2, json.get("totalCount").asInt());

        List<Long> tagIds = extractTagIds(json.get("items"));
        assertTrue(tagIds.containsAll(Arrays.asList(6L, 3L)));
    }

    private List<Long> extractTagIds(JsonNode result) {
        List<Long> ids = new ArrayList<>();

        ArrayNode arrayNode = (ArrayNode) result;
        arrayNode.forEach(n -> ids.add(n.get("id").asLong()));

        return ids;
    }
}
