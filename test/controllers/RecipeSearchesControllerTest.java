package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.database.rider.core.api.dataset.DataSet;
import controllers.v1.routes;
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

import static junit.framework.TestCase.assertEquals;
import static play.mvc.Http.HttpVerbs.GET;
import static play.mvc.Http.Status.OK;
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
        assertEquals("Result status is not OK!", OK, result.status());

        String responseContent = contentAsString(result);
        JsonNode responseJson = Json.parse(responseContent);
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

        // TODO
    }

    @Test
    @DataSet(value = "datasets/yml/recipesearches.yml", disableConstraints = true, cleanBefore = true)
    public void testExpires() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testExpires");
        logger.info("------------------------------------------------------------------------------------------------");

        // TODO
    }
}
