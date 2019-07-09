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

    @Test
    @DataSet("datasets/yml/recipes.yml")
    public void testGetRecipesByIngredients_IncludedExcludedNotMutEx() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetRecipesByIngredients_IncludedExcludedNotMutEx");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "searchMode=1&languageId=1&unknownIngs=4&unknownIngsRel=le&goodIngs=2&goodIngsRel=ge&limit=50&unknownIngs=4&offset=0&orderBy=name&orderBySort=asc&minIngs=1&maxIngs=5&inIngs[0]=1&inIngs[1]=2&exIngs[0]=4&exIngs[1]=2";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(RESOURCE_PATH + "?" + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);

        assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    @DataSet("datasets/yml/recipes.yml")
    public void testGetRecipesByIngredients_ComposedOf_Commons() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetRecipesByIngredients_ComposedOf_Commons");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "searchMode=1&languageId=1&unknownIngs=4&unknownIngsRel=le&goodIngs=2&goodIngsRel=ge&limit=50&offset=0&orderBy=name&orderBySort=asc&&minIngs=1&maxIngs=5&inIngs[0]=1&inIngs[1]=3&exIngs[0]=5";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(RESOURCE_PATH + "?" + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        resultJson = resultJson.get("items");

        assertEquals(2, resultJson.size());
        assertEquals(1L, resultJson.get(0).get("id").asLong());
        assertEquals(2L, resultJson.get(1).get("id").asLong());
    }

    @Test
    @DataSet("datasets/yml/recipes.yml")
    public void testGetRecipesByIngredients_ComposedOf_CommonsWithTags() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetRecipesByIngredients_ComposedOf_CommonsWithTags");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "searchMode=1&languageId=1&unknownIngs=4&unknownIngsRel=le&goodIngs=2&goodIngsRel=ge&limit=50&offset=0&orderBy=name&orderBySort=asc&minIngs=1&maxIngs=5&inIngs[0]=1&inIngs[1]=3&exIngTags[0]=6";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(RESOURCE_PATH + "?" + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        resultJson = resultJson.get("items");

        assertEquals(2, resultJson.size());
        assertEquals(1L, resultJson.get(0).get("id").asLong());
        assertEquals(2L, resultJson.get(1).get("id").asLong());
    }

    @Test
    @DataSet("datasets/yml/recipes.yml")
    public void testGetRecipesByIngredients_ComposedOfStrict() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetRecipesByIngredients_ComposedOfStrict");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "searchMode=3&languageId=1&goodIngsRatio=1.0&limit=50&offset=0&orderBy=name&orderBySort=asc&isAdditiveIngs=true&minIngs=1&maxIngs=4&inIngs[0]=1&inIngs[1]=2&inIngs[2]=3&inIngs[3]=4";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(RESOURCE_PATH + "?" + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        resultJson = resultJson.get("items");

        assertEquals(1, resultJson.size());
        assertEquals(1L, resultJson.get(0).get("id").asLong());
    }

    @Test
    @DataSet("datasets/yml/recipes.yml")
    public void testGetRecipesByIngredients_ComposedOfStrictTagsOnly() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetRecipesByIngredients_ComposedOfStrictTags");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "searchMode=3&languageId=1&goodIngsRatio=1.0&limit=50&offset=0&orderBy=name&orderBySort=asc&minIngs=1&maxIngs=4&inIngs[0]=10&inIngTags[0]=1&inIngTags[1]=2";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(RESOURCE_PATH + "?" + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        resultJson = resultJson.get("items");

        assertEquals(1, resultJson.size());
        assertEquals(3L, resultJson.get(0).get("id").asLong());
    }

    @Test
    @DataSet("datasets/yml/recipes.yml")
    public void testGetRecipesByIngredients_ComposedOfAnyOf() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetRecipesByIngredients_ComposedOfAnyOf");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "searchMode=1&languageId=1&unknownIngs=0&unknownIngsRel=ge&goodIngs=0&goodIngsRel=gt&limit=50&offset=0&orderBy=name&orderBySort=asc&minIngs=1&maxIngs=4&inIngs[0]=4";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(RESOURCE_PATH + "?" + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        resultJson = resultJson.get("items");

        assertEquals(1, resultJson.size());
        assertEquals(3L, resultJson.get(0).get("id").asLong());
    }

    @Test
    @DataSet("datasets/yml/recipes.yml")
    public void testGetRecipesByIngredients_ComposedOfAnyOfTagsOnly() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetRecipesByIngredients_ComposedOfAnyOfTagsOnly");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "searchMode=1&languageId=1&unknownIngs=0&unknownIngsRel=ge&goodIngs=0&goodIngsRel=gt&limit=50&offset=0&orderBy=name&orderBySort=asc&minIngs=1&maxIngs=8&inIngTags[0]=5";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(RESOURCE_PATH + "?" + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        resultJson = resultJson.get("items");

        assertEquals(OK, result.status());
        assertEquals(4, resultJson.size());
    }

    @Test
    @DataSet("datasets/yml/recipes.yml")
    public void testGetRecipesByIngredients_ComposedOfAnyOfTagsAndIngrs() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetRecipesByIngredients_ComposedOfAnyOfTagsAndIngrs");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "searchMode=1&languageId=1&unknownIngs=0&unknownIngsRel=ge&goodIngs=0&goodIngsRel=gt&limit=50&offset=0&orderBy=name&orderBySort=asc&minIngs=1&maxIngs=8&inIngs[0]=4&inIngTags[0]=1";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(RESOURCE_PATH + "?" + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        resultJson = resultJson.get("items");

        assertEquals(OK, result.status());
        assertEquals(3, resultJson.size());
    }

    @Test
    @DataSet("datasets/yml/recipes.yml")
    public void testGetRecipesByIngredients_ComposedOfExact() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetRecipesByIngredients_ComposedOfExact");
        logger.info("------------------------------------------------------------------------------------------------");

        String reqParams = "searchMode=1&languageId=1&unknownIngs=0&unknownIngsRel=le&goodIngs=3&goodIngsRel=eq&limit=50&offset=0&orderBy=name&orderBySort=asc&inIngs[0]=5&inIngs[1]=6&inIngs[2]=7";
        Http.RequestBuilder httpRequest = new Http.RequestBuilder().method(GET).uri(RESOURCE_PATH + "?" + reqParams);
        Result result = route(application.getApplication(), httpRequest);

        String resultContentStr = contentAsString(result);
        JsonNode resultJson = Json.parse(resultContentStr);
        resultJson = resultJson.get("items");

        assertEquals(1, resultJson.size());
        assertEquals(4L, resultJson.get(0).get("id").asLong());
    }
}
