package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.database.rider.core.api.dataset.DataSet;
import controllers.v1.routes;
import data.entities.RecipeSearch;
import io.ebean.Ebean;
import io.seruco.encoding.base62.Base62;
import org.junit.Rule;
import org.junit.Test;
import play.Logger;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import rules.PlayApplicationWithGuiceDbRider;

import java.math.BigInteger;
import java.time.Instant;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static play.mvc.Http.HttpVerbs.GET;
import static play.mvc.Http.HttpVerbs.POST;
import static play.mvc.Http.Status.*;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.route;

public class RecipeSearchesControllerTest {
    @Rule
    public PlayApplicationWithGuiceDbRider application = new PlayApplicationWithGuiceDbRider();

    private static Base62 base62 = Base62.createInstance();

    private static final Logger.ALogger logger = Logger.of(RecipeSearchesControllerTest.class);

    @Test
    @DataSet(value = "datasets/yml/recipesearches.yml", disableConstraints = true, cleanBefore = true)
    public void testGet() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGet");
        logger.info("------------------------------------------------------------------------------------------------");

        // TODO: replace app with an app that replaces real cleaner with a mock.

        long id = 239327L;
        byte[] encodedBytes = base62.encode(BigInteger.valueOf(id).toByteArray());
        String encodedIdString = new String(encodedBytes);

        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(GET)
                .uri(routes.RecipeSearchesController.get(encodedIdString).url());

        Result result = route(application.getApplication(), httpRequest);
        assertEquals("Response status is not OK!", OK, result.status());

        String responseStr = contentAsString(result);
        JsonNode responseJson = Json.parse(responseStr);
        assertEquals("Ids are not matching", encodedIdString, responseJson.get("id").asText());
        JsonNode query = responseJson.get("query");
        assertEquals("Wrong query in result!", "composed-of-number", query.get("searchMode").asText());
    }

    @Test
    @DataSet(value = "datasets/yml/recipesearches.yml", disableConstraints = true, cleanBefore = true)
    public void testCreate() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testCreate");
        logger.info("------------------------------------------------------------------------------------------------");

        JsonNode searchJson = Json.parse("" +
                "{" +
                "\"searchMode\": \"composed-of-number\"," +
                "\"goodIngs\": 3," +
                "\"goodIngsRel\": \"ge\"," +
                "\"inIngs\": [1, 2, 3]" +
                "}"
        );

        Http.RequestBuilder httpCreateRequest = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(searchJson)
                .uri(routes.RecipeSearchesController.create().url());

        Result response = route(application.getApplication(), httpCreateRequest);
        assertEquals("Response status is not OK!", OK, response.status());

        String responseStr = contentAsString(response);
        JsonNode responseJson = Json.parse(responseStr);
        assertNotNull("Content is invalid!", responseJson.get("id"));

        // Check if returned id can be queried.
        String encodedId = responseJson.get("id").asText();
        Http.RequestBuilder httpGetRequest = new Http.RequestBuilder()
                .method(GET)
                .uri(routes.RecipeSearchesController.get(encodedId).url());
        response = route(application.getApplication(), httpGetRequest);
        assertEquals("Response status is not OK!", OK, response.status());

        responseStr = contentAsString(response);
        responseJson = Json.parse(responseStr);
        assertEquals("Ids are not matching", encodedId, responseJson.get("id").asText());
        JsonNode query = responseJson.get("query");
        assertEquals("Wrong query in result!", "composed-of-number", query.get("searchMode").asText());
        assertEquals("Number of ingredients is wrong", 3, query.get("inIngs").size());
        assertEquals("Good ingredients is wrong", 3, query.get("goodIngs").asInt());
    }

    @Test
    @DataSet(value = "datasets/yml/recipesearches.yml", disableConstraints = true, cleanBefore = true)
    public void testExpires() throws InterruptedException {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testExpires");
        logger.info("------------------------------------------------------------------------------------------------");

        RecipeSearch search = new RecipeSearch();
        search.setQuery("someQuery");
        search.setLastAccessed(Instant.now());
        Ebean.save(search);

        // TODO
        // See: https://www.playframework.com/documentation/2.8.x/ScheduledTasks
        Thread.sleep(6000L);
        int count = Ebean.createQuery(RecipeSearch.class).findCount();
        assertEquals("Expired searches are not deleted!", 1, count);

    }

    @Test
    public void testLimitReached() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testLimitReached");
        logger.info("------------------------------------------------------------------------------------------------");

        // Fill DB with max number of searches.
        int maxSearches = application.getApplication().config().getInt("receptnekem.recipesearches.max");
        for (int i = 0; i < maxSearches; i++) {
            RecipeSearch recipeSearch = new RecipeSearch();
            recipeSearch.setQuery("someQuery" + (i + 1));
            recipeSearch.setPermanent(true);
            Ebean.save(recipeSearch);
        }

        JsonNode searchJson = Json.parse("" +
                "{" +
                "\"searchMode\": \"composed-of-number\"," +
                "\"goodIngs\": 3," +
                "\"goodIngsRel\": \"ge\"," +
                "\"inIngs\": [1, 2, 3]" +
                "}"
        );

        Http.RequestBuilder httpCreateRequest = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(searchJson)
                .uri(routes.RecipeSearchesController.create().url());

        Result response = route(application.getApplication(), httpCreateRequest);
        assertEquals("Created request above the maximum threshold!", FORBIDDEN, response.status());
    }
}
