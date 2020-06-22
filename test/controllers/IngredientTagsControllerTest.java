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
    @DataSet(value = "datasets/yml/ingredienttags.yml", disableConstraints = true, cleanBefore = true)
    public void testSingle() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testSingle");
        logger.info("------------------------------------------------------------------------------------------------");

        Http.RequestBuilder request = new Http.RequestBuilder().method(GET)
                .uri(routes.IngredientTagsController.single(3L).url());

        Result response = route(application.getApplication(), request);
        assertEquals(OK, response.status());

        String jsonStr = contentAsString(response);
        JsonNode json = Json.parse(jsonStr);
        assertEquals(3L, json.get("id").asLong());
        assertEquals("ingredient_2_tag_2", json.get("name").asText());
        List<Long> ingredientIds = new ArrayList<>();
        json.get("ingredients").forEach(n -> ingredientIds.add(n.asLong()));
        assertTrue(ingredientIds.containsAll(Arrays.asList(1L, 2L)));
    }

    @Test
    @DataSet(value = "datasets/yml/ingredienttags.yml", disableConstraints = true, cleanBefore = true)
    public void testSingle_NotFound() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testSingle_NotFound");
        logger.info("------------------------------------------------------------------------------------------------");

        Http.RequestBuilder request = new Http.RequestBuilder().method(GET)
                .uri(routes.IngredientTagsController.single(42L).url());

        Result response = route(application.getApplication(), request);
        assertEquals(NOT_FOUND, response.status());
    }
}
