package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.database.rider.core.api.dataset.DataSet;
import io.ebean.Ebean;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import play.Logger;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import rules.PlayApplicationWithGuiceDbRider;

import static junit.framework.TestCase.assertEquals;
import static play.test.Helpers.*;

public class RecipesControllerTest {
    @Rule
    public PlayApplicationWithGuiceDbRider application = new PlayApplicationWithGuiceDbRider();

    private static final Logger.ALogger logger = Logger.of(IngredientNamesControllerTest.class);
    private static final String RESOURCE_PATH = "/v1/recipes";

    @Before
    public void before() {
        Ebean.createSqlUpdate("update recipe " +
                "set numofings = (select count(*) from recipe_ingredient where recipe.id = recipe_ingredient.recipe_id)")
        .execute();
    }

    @Test
    @DataSet("datasets/yml/recipes.yml")
    public void testGetRecipesByIngredients_ComposedOf_ExcludedOverlaps() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetRecipesByIngredients_ComposedOf_ExcludedOverlaps");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "searchMode=1&languageId=1&limit=50&unknownIngs=4&unknownIngsRel=le&goodIngs=2&goodIngsRel=ge&offset=0&orderBy=name&orderBySort=asc&minIngs=1&maxIngs=5&inIngs[0]=1&inIngs[1]=2&exIngs[0]=4&exIngs[1]=5";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(RESOURCE_PATH + "?" + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        resultJson = resultJson.get("items");

        assertEquals(1, resultJson.size());
        assertEquals(1L, resultJson.get(0).get("id").asLong());
    }
}
