package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
import rules.PlayApplicationWithGuiceDbRider;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static play.mvc.Http.HeaderNames.LOCATION;
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
    public void testCreate() throws IOException {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testCreate");
        logger.info("------------------------------------------------------------------------------------------------");

        JsonNode searchJson = Json.parse("" +
                "{" +
                "\"searchMode\": \"composed-of-number\"," +
                "\"goodIngs\": 3," +
                "\"goodIngsRel\": \"ge\"," +
                "\"unknownIngs\": \"0\"," +
                "\"unknownIngsRel\": \"ge\"," +
                "\"goodAdditionalIngs\": 2," +
                "\"inIngs\": [1, 2, 3]," +
                "\"inIngTags\": [1]," +
                "\"exIngs\": [4, 7]," +
                "\"exIngTags\": [2]," +
                "\"addIngs\": [5]," +
                "\"addIngTags\": [6]," +
                "\"sourcePages\": [1, 2]" +
                "}"
        );

        Http.RequestBuilder httpCreateRequest = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(searchJson)
                .uri(routes.RecipeSearchesController.create().url());

        Result response = route(application.getApplication(), httpCreateRequest);
        assertEquals("Response status is not CREATED!", CREATED, response.status());
        assertTrue("Missing Location header!", response.header(LOCATION).isPresent());

        String location = response.header(LOCATION).get();

        // Check if returned id can be queried.
        Http.RequestBuilder httpGetRequest = new Http.RequestBuilder()
                .method(GET)
                .uri(location);
        response = route(application.getApplication(), httpGetRequest);
        assertEquals("Response status is not OK!", OK, response.status());

        String responseStr = contentAsString(response);
        JsonNode responseJson = Json.parse(responseStr);
        JsonNode query = responseJson.get("query");
        assertEquals("Wrong query in result!", "composed-of-number", query.get("searchMode").asText());
        assertEquals("Good ingredients is wrong", 3, query.get("goodIngs").asInt());
        assertEquals("Number of included ingredients is wrong", 3, query.get("inIngs").size());
        assertEquals("Number of included ingredient tags is wrong", 1, query.get("inIngTags").size());
        assertEquals("Number of excluded ingredients is wrong", 2, query.get("exIngs").size());
        assertEquals("Number of excluded ingredient tags is wrong", 1, query.get("exIngTags").size());
        assertEquals("Number of additional ingredients is wrong", 1, query.get("addIngs").size());
        assertEquals("Number of additional ingredient tags is wrong", 1, query.get("addIngTags").size());
        assertEquals("Number of source pages is wrong", 2, query.get("sourcePages").size());

        List<String> includedIngredientsNames = extractNames(query.get("inIngs"));
        List<String> excludedIngredientsNames = extractNames(query.get("exIngs"));
        List<String> additionalIngredientsNames = extractNames(query.get("addIngs"));
        List<String> includedIngredientTagNames = extractNames(query.get("inIngTags"));
        List<String> excludedIngredientTagNames = extractNames(query.get("exIngTags"));
        List<String> additionalIngredientTagNames = extractNames(query.get("addIngTags"));
        List<String> sourcePageNames = extractNames(query.get("sourcePages"));

        assertTrue(includedIngredientsNames.containsAll(Arrays.asList("ingredient_1", "ingredient_2", "ingredient_3")));
        assertTrue(excludedIngredientsNames.containsAll(Arrays.asList("ingredient_4", "ingredient_7")));
        assertTrue(additionalIngredientsNames.containsAll(Arrays.asList("ingredient_5")));
        assertTrue(includedIngredientTagNames.containsAll(Arrays.asList("ingredient_tag_1")));
        assertTrue(excludedIngredientTagNames.containsAll(Arrays.asList("ingredient_tag_2")));
        assertTrue(additionalIngredientTagNames.containsAll(Arrays.asList("ingredient_tag_6")));
        assertTrue(sourcePageNames.containsAll(Arrays.asList("src_pg_1", "src_pg_2")));
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

    @Test
    public void testCreate_InvalidSearchMode() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testCreate_InvalidSearchMode");
        logger.info("------------------------------------------------------------------------------------------------");

        JsonNode searchJson = Json.parse("" +
                "{" +
                "\"searchMode\": \"some-random-search-mode\"," +
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
        assertEquals(BAD_REQUEST, response.status());
    }

    @Test
    public void testCreate_InvalidNotMutuallyExclusive() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testCreate_InvalidNotMutuallyExclusive");
        logger.info("------------------------------------------------------------------------------------------------");

        JsonNode searchJson = Json.parse("" +
                "{" +
                "\"searchMode\": \"composed-of-number\"," +
                "\"goodIngs\": 3," +
                "\"goodIngsRel\": \"ge\"," +
                "\"inIngs\": [1, 2, 3]," +
                "\"exIngs\": [1, 5, 6]" +
                "}"
        );

        Http.RequestBuilder httpCreateRequest = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(searchJson)
                .uri(routes.RecipeSearchesController.create().url());

        Result response = route(application.getApplication(), httpCreateRequest);
        assertEquals(BAD_REQUEST, response.status());
    }

    @Test
    public void testSizeLimit() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testSizeLimit");
        logger.info("------------------------------------------------------------------------------------------------");

        JsonNode searchJson = Json.parse("" +
                "{" +
                "\"searchMode\": \"composed-of-number\"," +
                "\"goodIngs\": 3," +
                "\"goodIngsRel\": \"ge\"," +
                "\"unknownIngs\": \"0\"," +
                "\"unknownIngsRel\": \"ge\"," +
                "\"goodAdditionalIngs\": 2," +
                "\"inIngs\": [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20]," +
                "\"inIngTags\": [1, 2, 3, 4, 5]," +
                "\"exIngs\": [21, 22, 23, 24, 25]," +
                "\"exIngTags\": [6, 7, 8]," +
                "\"addIngs\": [26, 27, 28, 29]," +
                "\"addIngTags\": [9, 10, 11]," +
                "\"sourcePages\": [1, 2]" +
                "}"
        );

        Http.RequestBuilder httpCreateRequest = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(searchJson)
                .uri(routes.RecipeSearchesController.create().url());

        Result response = route(application.getApplication(), httpCreateRequest);
        assertEquals(BAD_REQUEST, response.status());
    }

    private List<String> extractNames(JsonNode node) {
        List<String> names = new ArrayList<>();
        ArrayNode arrayNode = (ArrayNode) node;
        arrayNode.forEach(n -> names.add(n.get("name").asText()));
        return names;
    }
}
