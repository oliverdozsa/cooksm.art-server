package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.database.rider.core.api.dataset.DataSet;
import controllers.v1.routes;
import dto.IngredientTagCreateUpdateDto;
import org.junit.Rule;
import org.junit.Test;
import play.Logger;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import rules.PlayApplicationWithGuiceDbRiderRule;
import utils.JwtTestUtils;

import java.util.Arrays;
import java.util.List;

import static controllers.IngredientTagsControllerTestUtils.toIngredientIdList;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static play.test.Helpers.*;

public class IngredientTagsControllerTest_UpdateTest {
    @Rule
    public PlayApplicationWithGuiceDbRiderRule application = new PlayApplicationWithGuiceDbRiderRule();

    private static final Logger.ALogger logger = Logger.of(IngredientTagsControllerTest_UpdateTest.class);

    @Test
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUserDefined_Update() throws InterruptedException {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testUserDefined_Update");
        logger.info("------------------------------------------------------------------------------------------------");


        IngredientTagCreateUpdateDto dto = new IngredientTagCreateUpdateDto();
        dto.name = "user_1_ingredient_tag_1_updated";
        dto.ingredientIds = Arrays.asList(3L, 4L);

        String jwt = JwtTestUtils.createToken(10000L, 1L, application.getApplication().config());
        Http.RequestBuilder request = new Http.RequestBuilder().method(PUT)
                .bodyJson(Json.toJson(dto))
                .uri(routes.IngredientTagsController.update(10L).url());
        JwtTestUtils.addJwtTokenTo(request, jwt);

        Result result = route(application.getApplication(), request);

        assertEquals("Status is not NO CONTENT", Http.Status.NO_CONTENT, result.status());

        // Because of parallel execution of SQL command, we need o wait until PUT finishes.
        // I haven't found any better way here :(. This can make the test flaky.
        Thread.sleep(2000);

        request = new Http.RequestBuilder().method(GET)
                .uri(routes.IngredientTagsController.single(10L, 0L).url());
        JwtTestUtils.addJwtTokenTo(request, jwt);
        result = route(application.getApplication(), request);

        assertEquals("Status is not OK", Http.Status.OK, result.status());

        String jsonStr = contentAsString(result);
        JsonNode json = Json.parse(jsonStr);

        String name = json.get("name").asText();
        List<Long> ingredientIds = toIngredientIdList(json.get("ingredients"));

        assertEquals("user_1_ingredient_tag_1_updated", name);
        assertTrue(ingredientIds.containsAll(Arrays.asList(3L, 4L)));
    }

    @Test
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUserDefined_Update_NotExistingIngredientId() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testUserDefined_Update_NotExistingIngredientId");
        logger.info("------------------------------------------------------------------------------------------------");

        IngredientTagCreateUpdateDto dto = new IngredientTagCreateUpdateDto();
        dto.name = "user_1_ingredient_tag_1_updated";
        dto.ingredientIds = Arrays.asList(3L, 42L);

        String jwt = JwtTestUtils.createToken(1000L, 1L, application.getApplication().config());
        Http.RequestBuilder request = new Http.RequestBuilder().method(PUT)
                .bodyJson(Json.toJson(dto))
                .uri(routes.IngredientTagsController.update(10L).url());
        JwtTestUtils.addJwtTokenTo(request, jwt);

        Result result = route(application.getApplication(), request);

        assertEquals("Status is not BAD REQUEST", Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUserDefined_Update_NoIngredientIds() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testUserDefined_Update_NoIngredientIds");
        logger.info("------------------------------------------------------------------------------------------------");

        IngredientTagCreateUpdateDto dto = new IngredientTagCreateUpdateDto();
        dto.name = "user_1_ingredient_tag_1_updated";

        String jwt = JwtTestUtils.createToken(1000L, 1L, application.getApplication().config());
        Http.RequestBuilder request = new Http.RequestBuilder().method(PUT)
                .bodyJson(Json.toJson(dto))
                .uri(routes.IngredientTagsController.update(10L).url());
        JwtTestUtils.addJwtTokenTo(request, jwt);

        Result result = route(application.getApplication(), request);

        assertEquals("Status is not BAD REQUEST", Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUserDefined_UpdateWithDuplicateIngredientIds() throws InterruptedException {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testUserDefined_UpdateWithDuplicateIngredientIds");
        logger.info("------------------------------------------------------------------------------------------------");

        IngredientTagCreateUpdateDto dto = new IngredientTagCreateUpdateDto();
        dto.name = "user_1_ingredient_tag_1_updated";
        dto.ingredientIds = Arrays.asList(3L, 3L, 4L, 4L);

        String jwt = JwtTestUtils.createToken(10000L, 1L, application.getApplication().config());
        Http.RequestBuilder request = new Http.RequestBuilder().method(PUT)
                .bodyJson(Json.toJson(dto))
                .uri(routes.IngredientTagsController.update(10L).url());
        JwtTestUtils.addJwtTokenTo(request, jwt);

        Result result = route(application.getApplication(), request);

        assertEquals("Status is not NO CONTENT", Http.Status.NO_CONTENT, result.status());

        // Because of parallel execution of SQL command, we need o wait until PUT finishes.
        // I haven't found any better way here :(. This can make the test flaky.
        Thread.sleep(2000);

        request = new Http.RequestBuilder().method(GET)
                .uri(routes.IngredientTagsController.single(10L, 0L).url());
        JwtTestUtils.addJwtTokenTo(request, jwt);
        result = route(application.getApplication(), request);

        assertEquals("Status is not OK", Http.Status.OK, result.status());

        String jsonStr = contentAsString(result);
        JsonNode json = Json.parse(jsonStr);

        String name = json.get("name").asText();
        logger.warn("name = " + name);
        List<Long> ingredientIds = toIngredientIdList(json.get("ingredients"));

        assertEquals("user_1_ingredient_tag_1_updated", name);
        assertTrue(ingredientIds.containsAll(Arrays.asList(3L, 4L)));
        assertEquals("Ids size is not correct!", 2, ingredientIds.size());
    }

    @Test
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUserDefined_Update_InvalidName() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testUserDefined_Update_InvalidName");
        logger.info("------------------------------------------------------------------------------------------------");

        IngredientTagCreateUpdateDto dto = new IngredientTagCreateUpdateDto();
        dto.name = "u";
        dto.ingredientIds = Arrays.asList(3L, 4L);

        String jwt = JwtTestUtils.createToken(1000L, 1L, application.getApplication().config());
        Http.RequestBuilder request = new Http.RequestBuilder().method(PUT)
                .bodyJson(Json.toJson(dto))
                .uri(routes.IngredientTagsController.update(10L).url());
        JwtTestUtils.addJwtTokenTo(request, jwt);

        Result result = route(application.getApplication(), request);

        assertEquals("Status is not BAD_REQUEST", Http.Status.BAD_REQUEST, result.status());
    }
}
