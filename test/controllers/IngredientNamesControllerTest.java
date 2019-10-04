package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.database.rider.core.api.dataset.DataSet;
import org.junit.Rule;
import org.junit.Test;
import play.Logger;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import rules.PlayApplicationWithGuiceDbRider;

import static junit.framework.TestCase.assertEquals;
import static play.mvc.Http.HttpVerbs.GET;
import static play.test.Helpers.*;

public class IngredientNamesControllerTest {
    @Rule
    public PlayApplicationWithGuiceDbRider application = new PlayApplicationWithGuiceDbRider();

    private static final Logger.ALogger logger = Logger.of(IngredientNamesControllerTest.class);
    private static final String RESOURCE_PATH = "/v1/ingredientnames";

    @Test
    @DataSet(value = "datasets/yml/ingredientnames.yml", disableConstraints = true, cleanBefore = true)
    public void testListNamesHu() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testListNamesHu");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "languageId=1&nameLike=hu";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(RESOURCE_PATH + "?" + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);

        assertEquals(5, resultJson.get("items").size());
    }

    @Test
    @DataSet(value = "datasets/yml/ingredientnames.yml", disableConstraints = true, cleanBefore = true)
    public void testPaging() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testPaging");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "languageId=1&nameLike=hu&offset=0&limit=2";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(RESOURCE_PATH + "?" + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        JsonNode resultNamesJson = resultJson.get("items");

        assertEquals(2, resultNamesJson.size());
        assertEquals(5, resultJson.get("totalCount").asInt());

        // Next page
        reqParams = "languageId=1&nameLike=hu&offset=2&limit=6";
        httpRequest = new Http.RequestBuilder().method(GET).uri(RESOURCE_PATH + "?" + reqParams);
        result = route(application.getApplication(), httpRequest);

        resultContentStr = contentAsString(result);
        resultJson = Json.parse(resultContentStr);
        resultNamesJson = resultJson.get("items");

        assertEquals(3, resultNamesJson.size());
    }

    @Test
    @DataSet(value = "datasets/yml/ingredientnames.yml", disableConstraints = true, cleanBefore = true)
    public void testIngredientAltNames_hasAltNames() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testIngredientAltNames_hasAltNames");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "languageId=1&nameLike=hu_1&offset=0&limit=2";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(RESOURCE_PATH + "?" + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        JsonNode resultAltNamesJson = resultJson.get("items").get(0).get("altNames");
        assertEquals(3, resultAltNamesJson.size());
    }

    @Test
    @DataSet(value = "datasets/yml/ingredientnames.yml", disableConstraints = true, cleanBefore = true)
    public void testIngredientAltNames_noAltName() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testIngredientAltNames_noAltName");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "languageId=2&nameLike=en_6&offset=0&limit=2";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(RESOURCE_PATH + "?" + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        JsonNode resultAltNamesJson = resultJson.get("items").get(0).get("altNames");
        assertEquals(0, resultAltNamesJson.size());
    }
}
