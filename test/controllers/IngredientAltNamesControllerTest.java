package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Rule;
import org.junit.Test;
import play.Logger;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import rules.PlayApplicationWithGuiceDbRider;

import static junit.framework.TestCase.assertEquals;
import static play.mvc.Http.HttpVerbs.GET;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.route;

public class IngredientAltNamesControllerTest {
    @Rule
    public PlayApplicationWithGuiceDbRider application = new PlayApplicationWithGuiceDbRider();

    private static final Logger.ALogger logger = Logger.of(IngredientNamesControllerTest.class);
    private static final String RESOURCE_PATH = "/v1/ingredientaltnames";

    @Test
    public void testIngredientAltNames_all() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testIngredientAltNames_all");
        logger.info("------------------------------------------------------------------------------------------------");

        String queryParams = "languageId=1";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(RESOURCE_PATH + "?" + queryParams);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr).get("items");

        assertEquals(5, resultJson.size());
    }

    @Test
    public void testIngredientAltNames_paging() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testIngredientAltNames_paging");
        logger.info("------------------------------------------------------------------------------------------------");

        String queryParams = "languageId=1&limit=3&offset=0";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(RESOURCE_PATH + "?" + queryParams);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        JsonNode ingrAltNames = resultJson.get("items");

        assertEquals(5, resultJson.get("totalCount").asInt());
        assertEquals(3, ingrAltNames.size());

        // Next page
        queryParams = "languageId=1&limit=3&offset=3";
        httpRequest = new Http.RequestBuilder().method(GET).uri(RESOURCE_PATH + "?" + queryParams);
        result = route(application.getApplication(), httpRequest);

        resultContentStr = contentAsString(result);
        resultJson = Json.parse(resultContentStr);
        ingrAltNames = resultJson.get("items");

        assertEquals(2, ingrAltNames.size());
    }

    @Test
    public void TestIngredientAltNames_noAltName() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: TestIngredientAltNames_noAltName");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "nameLike=3_hu&langId=1";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(RESOURCE_PATH + "?" + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        JsonNode resultIngrsJson = resultJson.get("items");

        assertEquals(1, resultIngrsJson.size());
        assertEquals(0, resultIngrsJson.get(0).get("altNames").size());
    }
}
