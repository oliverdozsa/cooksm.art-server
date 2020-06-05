package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.database.rider.core.api.dataset.DataSet;
import controllers.v1.routes;
import data.entities.*;
import lombokized.dto.FavoriteRecipeCreateDto;
import io.ebean.Ebean;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import play.Logger;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import rules.PlayApplicationWithGuiceDbRider;
import utils.JwtTestUtils;

import java.io.IOException;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

public class FavoriteRecipesControllerTest {
    @Rule
    public PlayApplicationWithGuiceDbRider application = new PlayApplicationWithGuiceDbRider();

    private static final Logger.ALogger logger = Logger.of(FavoriteRecipesControllerTest.class);

    @Before
    public void setup() {
    }

    @Test
    @DataSet(value = "datasets/yml/favoriterecipes.yml", disableConstraints = true, cleanBefore = true)
    public void testGetEmpty() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testGetEmpty");
        logger.info("------------------------------------------------------------------------------------------------");

        Http.RequestBuilder httpRequest = new Http.RequestBuilder().uri(routes.FavoriteRecipesController.allOfUser().url());

        String token = JwtTestUtils.createToken(10000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequest, token);

        Result result = route(application.getApplication(), httpRequest);
        assertEquals("Result of request is wrong!", OK, result.status());
        JsonNode resultJson = Json.parse(contentAsString(result));
        assertEquals("Total count is wrong!", 0, resultJson.get("totalCount").asInt());
    }

    @Test
    @DataSet(value = "datasets/yml/favoriterecipes.yml", disableConstraints = true, cleanBefore = true)
    public void testCreate_Get() throws IOException {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testCreate_Get");
        logger.info("------------------------------------------------------------------------------------------------");

        FavoriteRecipeCreateDto favoriteRecipe = new FavoriteRecipeCreateDto();
        favoriteRecipe.setRecipeId(1L);

        Http.RequestBuilder httpRequestCreate = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(Json.toJson(favoriteRecipe))
                .uri(routes.FavoriteRecipesController.create().url());

        String token = JwtTestUtils.createToken(10000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequestCreate, token);

        Result result = route(application.getApplication(), httpRequestCreate);
        assertEquals("Result of request is wrong!", CREATED, result.status());
        assertNotNull("Missing header: Location!", result.headers().get("Location"));

        // Get by id
        Http.RequestBuilder httpRequestGet = new Http.RequestBuilder()
                .uri(result.headers().get("Location"));
        JwtTestUtils.addJwtTokenTo(httpRequestGet, token);
        result = route(application.getApplication(), httpRequestGet);
        JsonNode favoriteRecipesJson = Json.parse(contentAsString(result));

        assertEquals("Unexpected recipe name!", "recipe_1", favoriteRecipesJson.get("name").asText());
        assertEquals("Unexpected recipe url!", "recipe_1_url", favoriteRecipesJson.get("url").asText());

        // Get all
        httpRequestGet = new Http.RequestBuilder()
                .uri(routes.FavoriteRecipesController.allOfUser().url());
        JwtTestUtils.addJwtTokenTo(httpRequestGet, token);

        result = route(application.getApplication(), httpRequestGet);
        JsonNode resultJson = Json.parse(contentAsString(result));

        assertEquals("Total count is wrong!", 1, resultJson.get("totalCount").asInt());
        assertEquals("Recipe name is wrong!", "recipe_1", resultJson.get("items").get(0).get("name").asText());
        assertEquals("Recipe url is wrong!", "recipe_1_url", resultJson.get("items").get(0).get("url").asText());
    }

    @Test
    @DataSet(value = "datasets/yml/favoriterecipes.yml", disableConstraints = true, cleanBefore = true)
    public void testDelete() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testDelete");
        logger.info("------------------------------------------------------------------------------------------------");

        // Create a favorite recipes
        FavoriteRecipe favoriteRecipe = new FavoriteRecipe();
        favoriteRecipe.setRecipe(Ebean.find(Recipe.class, 1L));
        favoriteRecipe.setUser(Ebean.find(User.class, 1L));
        Ebean.save(favoriteRecipe);

        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(DELETE)
                .uri(routes.FavoriteRecipesController.delete(favoriteRecipe.getId()).url());

        String token = JwtTestUtils.createToken(10000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequest, token);

        Result result = route(application.getApplication(), httpRequest);
        assertEquals("Result of request is wrong!", NO_CONTENT, result.status());
    }

    @Test
    public void testCreate_Invalid_NoJson() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testCreate_Invalid_NoJson");
        logger.info("------------------------------------------------------------------------------------------------");

        User user = new User();
        user.setEmail("some@one.com");
        Ebean.save(user);

        Http.RequestBuilder httpRequestCreate = new Http.RequestBuilder()
                .method(POST)
                .bodyText("{asd")
                .uri(routes.FavoriteRecipesController.create().url());

        String token = JwtTestUtils.createToken(10000L, user.getId(), application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequestCreate, token);

        Result result = route(application.getApplication(), httpRequestCreate);
        assertEquals("Result of request is wrong!", BAD_REQUEST, result.status());
    }

    @Test
    public void testCreate_Invalid_RecipeNotExisting() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testCreate_Invalid_RecipeNotExisting");
        logger.info("------------------------------------------------------------------------------------------------");

        FavoriteRecipeCreateDto favoriteRecipe = new FavoriteRecipeCreateDto();
        favoriteRecipe.setRecipeId(4284L);

        Http.RequestBuilder httpRequestCreate = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(Json.toJson(favoriteRecipe))
                .uri(routes.FavoriteRecipesController.create().url());

        String token = JwtTestUtils.createToken(10000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequestCreate, token);

        Result result = route(application.getApplication(), httpRequestCreate);
        assertEquals("Result of request is wrong!", NOT_FOUND, result.status());
    }

    @Test
    public void testCreate_Invalid_MaxReached() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testCreate_Invalid_MaxReached");
        logger.info("------------------------------------------------------------------------------------------------");

        Language language = new Language();
        language.setIsoName("hu");
        Ebean.save(language);

        SourcePage sourcePage = new SourcePage();
        sourcePage.setName("someSourcePage");
        sourcePage.setLanguage(language);
        Ebean.save(sourcePage);

        User user = new User();
        user.setEmail("some@user.com");
        Ebean.save(user);

        int max = application.getApplication().config().getInt("receptnekem.favoriterecipes.maxperuser");
        for (int i = 0; i < max; i++) {
            Recipe recipe = new Recipe();
            recipe.setName("recipe_t_" + i);
            recipe.setSourcePage(sourcePage);
            Ebean.save(recipe);

            FavoriteRecipe favoriteRecipe = new FavoriteRecipe();
            favoriteRecipe.setUser(user);
            favoriteRecipe.setRecipe(Ebean.find(Recipe.class, recipe.getId()));
            Ebean.save(favoriteRecipe);
        }

        FavoriteRecipeCreateDto favoriteRecipe = new FavoriteRecipeCreateDto();
        favoriteRecipe.setRecipeId(1L);

        Http.RequestBuilder httpRequestCreate = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(Json.toJson(favoriteRecipe))
                .uri(routes.FavoriteRecipesController.create().url());

        String token = JwtTestUtils.createToken(10000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequestCreate, token);

        Result result = route(application.getApplication(), httpRequestCreate);
        assertEquals("Result of request is wrong!", BAD_REQUEST, result.status());
    }

    @Test
    @DataSet(value = "datasets/yml/favoriterecipes.yml", disableConstraints = true, cleanBefore = true)
    public void testCreate_Already_Existing() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testCreate_Already_Existing");
        logger.info("------------------------------------------------------------------------------------------------");

        FavoriteRecipe favoriteRecipe = new FavoriteRecipe();
        favoriteRecipe.setUser(Ebean.find(User.class, 1L));
        favoriteRecipe.setRecipe(Ebean.find(Recipe.class, 1L));
        Ebean.save(favoriteRecipe);

        FavoriteRecipeCreateDto favoriteRecipeDTO = new FavoriteRecipeCreateDto();
        favoriteRecipeDTO.setRecipeId(1L);

        Http.RequestBuilder httpRequestCreate = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(Json.toJson(favoriteRecipeDTO))
                .uri(routes.FavoriteRecipesController.create().url());

        String token = JwtTestUtils.createToken(10000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequestCreate, token);

        Result result = route(application.getApplication(), httpRequestCreate);
        assertEquals("Result of request is wrong!", BAD_REQUEST, result.status());
    }

    @Test
    @DataSet(value = "datasets/yml/favoriterecipes.yml", disableConstraints = true, cleanBefore = true)
    public void testSingle_UserDoesntOwn() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testSingle_UserDoesntOwn");
        logger.info("------------------------------------------------------------------------------------------------");

        Http.RequestBuilder httpRequestCreate = new Http.RequestBuilder()
                .method(GET)
                .uri(routes.FavoriteRecipesController.single(2).url());

        String token = JwtTestUtils.createToken(10000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequestCreate, token);

        Result result = route(application.getApplication(), httpRequestCreate);
        assertEquals("Result of request is wrong!", NOT_FOUND, result.status());
    }
}
