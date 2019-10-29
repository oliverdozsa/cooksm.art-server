package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.database.rider.core.api.dataset.DataSet;
import dto.FavoriteRecipeCreateDto;
import io.ebean.Ebean;
import models.entities.FavoriteRecipe;
import models.entities.Recipe;
import models.entities.SourcePage;
import models.entities.User;
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
    // TODO: Test for getFavorite recipe, where user doesn't own the favorite with the given id.
    @Rule
    public PlayApplicationWithGuiceDbRider application = new PlayApplicationWithGuiceDbRider();

    private static final Logger.ALogger logger = Logger.of(IngredientNamesControllerTest.class);
    private static final String RESOURCE_PATH = "/v1/favoriterecipes";

    @Before
    public void setup(){
    }

    @Test
    @DataSet(value = "datasets/yml/favoriterecipes.yml", disableConstraints = true, cleanBefore = true)
    public void testFavoriteRecipes_GetEmpty() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testFavoriteRecipes_GetEmpty");
        logger.info("------------------------------------------------------------------------------------------------");

        Http.RequestBuilder httpRequest = new Http.RequestBuilder().uri(RESOURCE_PATH);

        String token = JwtTestUtils.createToken(10000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequest, token);

        Result result = route(application.getApplication(), httpRequest);
        assertEquals("Result of request is wrong!", OK, result.status());
        JsonNode resultJson = Json.parse(contentAsString(result));
        assertEquals("Total count is wrong!", 0, resultJson.get("totalCount").asInt());
    }

    @Test
    @DataSet(value = "datasets/yml/favoriterecipes.yml", disableConstraints = true, cleanBefore = true)
    public void testFavoriteRecipes_Create_Get() throws IOException {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testFavoriteRecipes_Create_Get");
        logger.info("------------------------------------------------------------------------------------------------");

        FavoriteRecipeCreateDto favoriteRecipe = new FavoriteRecipeCreateDto(1L);

        Http.RequestBuilder httpRequestCreate = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(Json.toJson(favoriteRecipe))
                .uri(RESOURCE_PATH);

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
                .uri(RESOURCE_PATH);
        JwtTestUtils.addJwtTokenTo(httpRequestGet, token);

        result = route(application.getApplication(), httpRequestGet);
        JsonNode resultJson = Json.parse(contentAsString(result));

        assertEquals("Total count is wrong!", 1, resultJson.get("totalCount").asInt());
        assertEquals("Recipe name is wrong!","recipe_1", resultJson.get("items").get(0).get("name").asText());
        assertEquals("Recipe url is wrong!","recipe_1_url", resultJson.get("items").get(0).get("url").asText());
    }

    @Test
    @DataSet(value = "datasets/yml/favoriterecipes.yml", disableConstraints = true, cleanBefore = true)
    public void testFavoriteRecipes_Delete() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testFavoriteRecipes_Delete");
        logger.info("------------------------------------------------------------------------------------------------");

        // Create a favorite recipes
        FavoriteRecipe favoriteRecipe = new FavoriteRecipe();
        favoriteRecipe.setRecipe(Ebean.find(Recipe.class, 1L));
        favoriteRecipe.setUser(Ebean.find(User.class, 1L));
        Ebean.save(favoriteRecipe);

        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(DELETE)
                .uri(RESOURCE_PATH + "/" + favoriteRecipe.getId());

        String token = JwtTestUtils.createToken(10000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequest, token);

        Result result = route(application.getApplication(), httpRequest);
        assertEquals("Result of request is wrong!", NO_CONTENT, result.status());
    }

    @Test
    public void testFavoriteRecipes_Create_Invalid_NoJson() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testFavoriteRecipes_Create_Invalid_NoJson");
        logger.info("------------------------------------------------------------------------------------------------");

        Http.RequestBuilder httpRequestCreate = new Http.RequestBuilder()
                .method(POST)
                .bodyText("{asd")
                .uri(RESOURCE_PATH);

        String token = JwtTestUtils.createToken(10000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequestCreate, token);

        Result result = route(application.getApplication(), httpRequestCreate);
        assertEquals("Result of request is wrong!", BAD_REQUEST, result.status());
    }

    @Test
    public void testFavoriteRecipes_Create_Invalid_RecipeNotExisting() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testFavoriteRecipes_Create_Invalid_RecipeNotExisting");
        logger.info("------------------------------------------------------------------------------------------------");

        FavoriteRecipeCreateDto favoriteRecipe = new FavoriteRecipeCreateDto(4284L);

        Http.RequestBuilder httpRequestCreate = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(Json.toJson(favoriteRecipe))
                .uri(RESOURCE_PATH);

        String token = JwtTestUtils.createToken(10000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequestCreate, token);

        Result result = route(application.getApplication(), httpRequestCreate);
        assertEquals("Result of request is wrong!", NOT_FOUND, result.status());
    }

    @Test
    public void testFavoriteRecipes_Create_Invalid_MaxReached() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testFavoriteRecipes_Create_Invalid_MaxReached");
        logger.info("------------------------------------------------------------------------------------------------");

        int max = application.getApplication().config().getInt("receptnekem.favoriterecipes.maxperuser");
        for(int i = 0; i < max; i++){
            Recipe recipe = new Recipe();
            recipe.setName("recipe_t_" + i);
            recipe.setSourcePage(Ebean.find(SourcePage.class, 1L));
            Ebean.save(recipe);

            FavoriteRecipe favoriteRecipe = new FavoriteRecipe();
            favoriteRecipe.setUser(Ebean.find(User.class, 1L));
            favoriteRecipe.setRecipe(Ebean.find(Recipe.class, recipe.getId()));
            Ebean.save(favoriteRecipe);
        }

        FavoriteRecipeCreateDto favoriteRecipe = new FavoriteRecipeCreateDto(1L);

        Http.RequestBuilder httpRequestCreate = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(Json.toJson(favoriteRecipe))
                .uri(RESOURCE_PATH);

        String token = JwtTestUtils.createToken(10000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequestCreate, token);

        Result result = route(application.getApplication(), httpRequestCreate);
        assertEquals("Result of request is wrong!", BAD_REQUEST, result.status());
    }

    @Test
    @DataSet(value = "datasets/yml/favoriterecipes.yml", disableConstraints = true, cleanBefore = true)
    public void testFavoriteRecipes_Create_Already_Existing() {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: testFavoriteRecipes_Create_Already_Existing");
        logger.info("------------------------------------------------------------------------------------------------");

        FavoriteRecipe favoriteRecipe = new FavoriteRecipe();
        favoriteRecipe.setUser(Ebean.find(User.class, 1L));
        favoriteRecipe.setRecipe(Ebean.find(Recipe.class, 1L));
        Ebean.save(favoriteRecipe);

        FavoriteRecipeCreateDto favoriteRecipeDTO = new FavoriteRecipeCreateDto(1L);

        Http.RequestBuilder httpRequestCreate = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(Json.toJson(favoriteRecipeDTO))
                .uri(RESOURCE_PATH);

        String token = JwtTestUtils.createToken(10000L, 1L, application.getApplication().config());
        JwtTestUtils.addJwtTokenTo(httpRequestCreate, token);

        Result result = route(application.getApplication(), httpRequestCreate);
        assertEquals("Result of request is wrong!", BAD_REQUEST, result.status());
    }
}
