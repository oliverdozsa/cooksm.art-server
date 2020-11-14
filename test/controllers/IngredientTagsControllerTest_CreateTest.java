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
import rules.PlayApplicationWithGuiceDbRider;
import utils.JwtTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static controllers.IngredientTagsControllerTestUtils.toIngredientIdList;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static play.mvc.Http.HeaderNames.LOCATION;
import static play.test.Helpers.*;

public class IngredientTagsControllerTest_CreateTest {
    @Rule
    public PlayApplicationWithGuiceDbRider application = new PlayApplicationWithGuiceDbRider();

    private static final Logger.ALogger logger = Logger.of(IngredientTagsControllerTest.class);

    @Test
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUserDefined_Create() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testUserDefined_Create");
        logger.info("------------------------------------------------------------------------------------------------");

        IngredientTagCreateUpdateDto dto = new IngredientTagCreateUpdateDto();
        dto.name = "someName";
        dto.ingredientIds = Arrays.asList(1L, 2L);

        String jwt = JwtTestUtils.createToken(1000L, 1L, application.getApplication().config());
        Http.RequestBuilder request = new Http.RequestBuilder().method(POST)
                .bodyJson(Json.toJson(dto))
                .uri(routes.IngredientTagsController.create().url());
        JwtTestUtils.addJwtTokenTo(request, jwt);

        Result result = route(application.getApplication(), request);

        assertEquals("Status is not CREATED", Http.Status.CREATED, result.status());
        assertTrue("Missing Location header!", result.header(LOCATION).isPresent());

        String location = result.header(LOCATION).get();
        request = new Http.RequestBuilder().method(GET)
                .uri(location);
        JwtTestUtils.addJwtTokenTo(request, jwt);
        result = route(application.getApplication(), request);

        assertEquals("Status is not OK", Http.Status.OK, result.status());

        String jsonStr = contentAsString(result);
        JsonNode json = Json.parse(jsonStr);

        String name = json.get("name").asText();
        List<Long> ingredientIds = toIngredientIdList(json.get("ingredients"));

        assertEquals("someName", name);
        assertTrue(ingredientIds.containsAll(Arrays.asList(1L, 2L)));
    }

    @Test
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUserDefined_Create_InvalidName() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testUserDefined_Create_InvalidName");
        logger.info("------------------------------------------------------------------------------------------------");

        IngredientTagCreateUpdateDto dto = new IngredientTagCreateUpdateDto();
        dto.name = "s";
        dto.ingredientIds = Arrays.asList(1L, 2L);

        String jwt = JwtTestUtils.createToken(1000L, 1L, application.getApplication().config());
        Http.RequestBuilder request = new Http.RequestBuilder().method(POST)
                .bodyJson(Json.toJson(dto))
                .uri(routes.IngredientTagsController.create().url());
        JwtTestUtils.addJwtTokenTo(request, jwt);

        Result result = route(application.getApplication(), request);

        assertEquals("Status is not BAD_REQUEST!", Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUserDefined_Create_AlreadyExistingName() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testUserDefined_Create_AlreadyExistingName");
        logger.info("------------------------------------------------------------------------------------------------");

        IngredientTagCreateUpdateDto dto = new IngredientTagCreateUpdateDto();
        dto.name = "someName";
        dto.ingredientIds = Arrays.asList(1L, 2L);

        String jwt = JwtTestUtils.createToken(1000L, 1L, application.getApplication().config());
        Http.RequestBuilder request = new Http.RequestBuilder().method(POST)
                .bodyJson(Json.toJson(dto))
                .uri(routes.IngredientTagsController.create().url());
        JwtTestUtils.addJwtTokenTo(request, jwt);

        Result result = route(application.getApplication(), request);

        assertEquals("Status is not CREATED", Http.Status.CREATED, result.status());

        request = new Http.RequestBuilder().method(POST)
                .bodyJson(Json.toJson(dto))
                .uri(routes.IngredientTagsController.create().url());
        JwtTestUtils.addJwtTokenTo(request, jwt);

        result = route(application.getApplication(), request);
        assertEquals("Status is not BAD_REQUEST", Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUserDefined_Create_NoIngredientIds() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testUserDefined_Create_NoIngredientIds");
        logger.info("------------------------------------------------------------------------------------------------");

        IngredientTagCreateUpdateDto dto = new IngredientTagCreateUpdateDto();
        dto.name = "someName";
        dto.ingredientIds = Collections.emptyList();

        String jwt = JwtTestUtils.createToken(1000L, 1L, application.getApplication().config());
        Http.RequestBuilder request = new Http.RequestBuilder().method(POST)
                .bodyJson(Json.toJson(dto))
                .uri(routes.IngredientTagsController.create().url());
        JwtTestUtils.addJwtTokenTo(request, jwt);

        Result result = route(application.getApplication(), request);

        assertEquals("Status is not BAD REQUEST", Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUserDefined_Create_NotExistingIngredientId() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testUserDefined_Create_NotExistingIngredientId");
        logger.info("------------------------------------------------------------------------------------------------");

        IngredientTagCreateUpdateDto dto = new IngredientTagCreateUpdateDto();
        dto.name = "someName";
        dto.ingredientIds = Arrays.asList(1L, 42L);

        String jwt = JwtTestUtils.createToken(1000L, 1L, application.getApplication().config());
        Http.RequestBuilder request = new Http.RequestBuilder().method(POST)
                .bodyJson(Json.toJson(dto))
                .uri(routes.IngredientTagsController.create().url());
        JwtTestUtils.addJwtTokenTo(request, jwt);

        Result result = route(application.getApplication(), request);

        assertEquals("Status is not BAD REQUEST", Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUserDefined_Create_LimitReached() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testUserDefined_Create_LimitReached");
        logger.info("------------------------------------------------------------------------------------------------");

        Integer maxPerUser = application.getApplication().config().getInt("receptnekem.userdefinedtags.maxperuser");

        int i;
        String jwt = JwtTestUtils.createToken(1000L, 3L, application.getApplication().config());

        for(i = 0; i < maxPerUser; i++) {
            IngredientTagCreateUpdateDto dto = new IngredientTagCreateUpdateDto();
            dto.name = "someName" + i;
            dto.ingredientIds = Arrays.asList(1L, 2L);

            Http.RequestBuilder request = new Http.RequestBuilder().method(POST)
                    .bodyJson(Json.toJson(dto))
                    .uri(routes.IngredientTagsController.create().url());
            JwtTestUtils.addJwtTokenTo(request, jwt);

            Result result = route(application.getApplication(), request);
            assertEquals("Status is not CREATED!", Http.Status.CREATED, result.status());
        }

        IngredientTagCreateUpdateDto dto = new IngredientTagCreateUpdateDto();
        dto.name = "someName" + i;
        dto.ingredientIds = Arrays.asList(1L, 2L);

        Http.RequestBuilder request = new Http.RequestBuilder().method(POST)
                .bodyJson(Json.toJson(dto))
                .uri(routes.IngredientTagsController.create().url());
        JwtTestUtils.addJwtTokenTo(request, jwt);

        Result result = route(application.getApplication(), request);
        assertEquals("Status is not FORBIDDEN!", Http.Status.FORBIDDEN, result.status());
    }

    @Test
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUserDefined_CreateWithDuplicateIngredientIds() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testUserDefined_CreateWithDuplicateIngredientIds");
        logger.info("------------------------------------------------------------------------------------------------");

        IngredientTagCreateUpdateDto dto = new IngredientTagCreateUpdateDto();
        dto.name = "someName";
        dto.ingredientIds = Arrays.asList(1L, 1L, 2L, 2L);

        String jwt = JwtTestUtils.createToken(1000L, 1L, application.getApplication().config());
        Http.RequestBuilder request = new Http.RequestBuilder().method(POST)
                .bodyJson(Json.toJson(dto))
                .uri(routes.IngredientTagsController.create().url());
        JwtTestUtils.addJwtTokenTo(request, jwt);

        Result result = route(application.getApplication(), request);

        assertEquals("Status is not CREATED", Http.Status.CREATED, result.status());
        assertTrue("Missing Location header!", result.header(LOCATION).isPresent());

        String location = result.header(LOCATION).get();
        request = new Http.RequestBuilder().method(GET)
                .uri(location);
        JwtTestUtils.addJwtTokenTo(request, jwt);
        result = route(application.getApplication(), request);

        assertEquals("Status is not OK", Http.Status.OK, result.status());

        String jsonStr = contentAsString(result);
        JsonNode json = Json.parse(jsonStr);

        String name = json.get("name").asText();
        List<Long> ingredientIds = toIngredientIdList(json.get("ingredients"));

        assertEquals("someName", name);
        assertEquals("Ingredients size is not correct!", 2, ingredientIds.size());
        assertTrue(ingredientIds.containsAll(Arrays.asList(1L, 2L)));
    }
}
