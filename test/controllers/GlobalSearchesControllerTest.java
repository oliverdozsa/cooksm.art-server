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
import utils.Base62Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.test.Helpers.*;

public class GlobalSearchesControllerTest {
    @Rule
    public PlayApplicationWithGuiceDbRider application = new PlayApplicationWithGuiceDbRider();

    private static final Logger.ALogger logger = Logger.of(GlobalSearchesControllerTest.class);

    @Test
    @DataSet(value = "datasets/yml/globalsearches.yml", disableConstraints = true, cleanBefore = true)
    public void testAll() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testListTags");
        logger.info("------------------------------------------------------------------------------------------------");

        Http.RequestBuilder request = new Http.RequestBuilder().method(GET)
                .uri(routes.GlobalSearchesController.all().url());

        Result response = route(application.getApplication(), request);

        assertEquals(OK, response.status());

        String jsonStr = contentAsString(response);
        JsonNode json = Json.parse(jsonStr);

        assertEquals(3, json.size());

        List<String> names = new ArrayList<>();
        List<String> searchIds = new ArrayList<>();
        List<String> urlFriendlyNames = new ArrayList<>();
        json.forEach(n -> names.add(n.get("name").asText()));
        json.forEach(n -> searchIds.add(n.get("searchId").asText()));
        json.forEach(n -> urlFriendlyNames.add(n.get("urlFriendlyName").asText()));
        assertTrue(names.containsAll(Arrays.asList("globalQuery1", "globalQuery2", "globalQuery3")));
        assertTrue(urlFriendlyNames.containsAll(Arrays.asList("global-query-1", "global-query-2", "global-query-3")));
        assertTrue(searchIds.containsAll(Arrays.asList(Base62Utils.encode(239328L), Base62Utils.encode(239329L), Base62Utils.encode(239330L))));
    }
}
