package controllers;

import clients.MenuTestClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.database.rider.core.api.dataset.DataSet;
import com.typesafe.config.Config;
import dto.MenuCreateUpdateDto;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.mvc.Result;
import rules.RuleChainForTests;

import java.util.ArrayList;
import java.util.List;

import static extractors.DataFromResult.statusOf;
import static extractors.MenuFromResult.*;
import static matchers.ResultHasLocationHeader.hasLocationHeader;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static play.mvc.Http.Status.*;
import static play.test.Helpers.contentAsString;

public class MenuControllerTest {
    private final RuleChainForTests ruleChainForTests = new RuleChainForTests();
    private MenuTestClient client;

    @Rule
    public RuleChain chain = ruleChainForTests.getRuleChain();

    @Before
    public void setup() {
        client = new MenuTestClient(ruleChainForTests.getApplication());
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/menu.yml", disableConstraints = true, cleanBefore = true)
    public void testCreate() {
        // Given
        MenuCreateUpdateDto menu = createAMenu();

        // When
        Result result = client.create(menu, 1L);

        // Then
        assertThat(statusOf(result), equalTo(CREATED));
        assertThat(result, hasLocationHeader());

        String locationUrl = result.headers().get("Location");
        result = client.byLocation(locationUrl, 1L);

        assertThat(contentAsString(result), statusOf(result), equalTo(OK));
        assertThat(nameOf(result), equalTo("A New Menu"));
        List<JsonNode> groups = groupsOf(result);
        assertThat("Groups size is not 2.", groups.size(), equalTo(2));

        groups.forEach(j -> {
            assertThat("Number of recipes in group is not 2.", j.get("recipes").size(), equalTo(2));
        });
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/menu.yml", disableConstraints = true, cleanBefore = true)
    public void testCreate_RecipeDoesNotExist() {
        // Given
        MenuCreateUpdateDto menu = createAMenuWithNotExistingRecipe();

        // When
        Result result = client.create(menu, 1L);

        // Then
        assertThat(contentAsString(result), statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/menu.yml", disableConstraints = true, cleanBefore = true)
    public void testDelete() {
        // When
        Result result = client.delete(1L, 1L);

        // Then
        assertThat(contentAsString(result), statusOf(result), equalTo(NO_CONTENT));
        result = client.delete(1L, 1L);
        assertThat(contentAsString(result), statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/menu.yml", disableConstraints = true, cleanBefore = true)
    public void testDelete_MenuOfOtherUser() {
        // When
        Result result = client.delete(2L, 1L);

        // Then
        assertThat(contentAsString(result), statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/menu.yml", disableConstraints = true, cleanBefore = true)
    public void testDelete_MenuDoesNotExist() {
        // When
        Result result = client.delete(42L, 1L);

        // Then
        assertThat(contentAsString(result), statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/menu.yml", disableConstraints = true, cleanBefore = true)
    public void testReplace() {
        // Given
        MenuCreateUpdateDto menuToReplace = createAMenu();
        MenuCreateUpdateDto.Group newGroup = new MenuCreateUpdateDto.Group();
        newGroup.recipes = new ArrayList<>();
        newGroup.recipes.add(5L);

        menuToReplace.groups.add(newGroup);

        // When
        Result result = client.update(1L, menuToReplace, 1L);

        // Then
        assertThat(contentAsString(result), statusOf(result), equalTo(NO_CONTENT));

        result = client.getById(1L, 1L);
        assertThat(contentAsString(result), statusOf(result), equalTo(OK));
        assertThat(nameOf(result), equalTo("A New Menu"));
        List<JsonNode> menuGroups = groupsOf(result);

        assertThat("Updated menu groups size should be 3", menuGroups.size(), equalTo(3));
        Long newRecipeId = menuGroups.get(2).get("recipes").get(0).get("id").asLong();

        assertThat(newRecipeId, equalTo(5L));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/menu.yml", disableConstraints = true, cleanBefore = true)
    public void testReplace_MenuOfOtherUser() {
        // Given
        MenuCreateUpdateDto menuToReplace = createAMenu();

        // When
        Result result = client.update(2L, menuToReplace, 1L);

        // Then
        assertThat(contentAsString(result), statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/menu.yml", disableConstraints = true, cleanBefore = true)
    public void testReplace_RecipeDoesNotExist() {
        // Given
        MenuCreateUpdateDto menuToReplace = createAMenuWithNotExistingRecipe();

        // When
        Result result = client.update(2L, menuToReplace, 1L);

        // Then
        assertThat(contentAsString(result), statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/menu.yml", disableConstraints = true, cleanBefore = true)
    public void testGetById() {
        // When
        Result result = client.getById(1L, 1L);

        // Then
        assertThat(contentAsString(result), statusOf(result), equalTo(OK));
        assertThat(nameOf(result), equalTo("Alice's Menu"));
        List<JsonNode> groups = groupsOf(result);
        assertThat("Groups size is not 2.", groups.size(), equalTo(2));

        groups.forEach(j -> {
            assertThat("Number of recipes in group is not 2.", j.get("recipes").size(), equalTo(2));
        });
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/menu.yml", disableConstraints = true, cleanBefore = true)
    public void testGetById_OtherUserIsTheOwner() {
        // When
        Result result = client.getById(2L, 1L);

        // Then
        assertThat(contentAsString(result), statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/menu.yml", disableConstraints = true, cleanBefore = true)
    public void testGetAll() {
        // When
        Result result = client.getAllOf(1L);

        // Then
        assertThat(contentAsString(result), statusOf(result), equalTo(OK));
        List<Long> menuIds = menuIdsOf(result);
        assertThat(menuIds.size(), equalTo(1));
        assertThat(menuIds.get(0), equalTo(1L));
    }

    @Test
    @DataSet(value = "datasets/yml/menu.yml", disableConstraints = true, cleanBefore = true)
    public void testTooManyMenusCreated() {
        // Given
        Config config = ruleChainForTests.getApplication().config();
        int maxItems = config.getInt("cooksm.art.menu.maxperuser");

        for(int i = 0; i < maxItems; i++) {
            MenuCreateUpdateDto menu = createAMenu();
            Result result = client.create(menu, 4L);
            assertThat(contentAsString(result), statusOf(result), equalTo(CREATED));
        }

        // When
        MenuCreateUpdateDto menu = createAMenu();
        Result result = client.create(menu, 4L);

        // Then
        assertThat(contentAsString(result), statusOf(result), equalTo(FORBIDDEN));
    }

    @Test
    @DataSet(value = "datasets/yml/menu.yml", disableConstraints = true, cleanBefore = true)
    public void testTooManyRecipesInAMenu() {
        // Given
        MenuCreateUpdateDto menuWithTooManyItems = createMenuWithTooManyItems();

        // When
        Result result = client.create(menuWithTooManyItems, 3L);

        // Then
        assertThat(contentAsString(result), statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    @DataSet(value = "datasets/yml/menu.yml", disableConstraints = true, cleanBefore = true)
    public void testTooManyRecipesInAMenuDuringUpdate() {
        // Given
        MenuCreateUpdateDto menuWithTooManyItems = createMenuWithTooManyItems();

        // When
        Result result = client.update(1L, menuWithTooManyItems, 3L);

        // Then
        assertThat(contentAsString(result), statusOf(result), equalTo(BAD_REQUEST));
    }

    private MenuCreateUpdateDto createAMenu() {
        MenuCreateUpdateDto menu = new MenuCreateUpdateDto();
        menu.name = "A New Menu";

        MenuCreateUpdateDto.Group group1 = new MenuCreateUpdateDto.Group();
        group1.recipes = new ArrayList<>();
        group1.recipes.add(1L);
        group1.recipes.add(2L);

        MenuCreateUpdateDto.Group group2 = new MenuCreateUpdateDto.Group();
        group2.recipes = new ArrayList<>();
        group2.recipes.add(3L);
        group2.recipes.add(4L);

        menu.groups = new ArrayList<>();
        menu.groups.add(group1);
        menu.groups.add(group2);

        return menu;
    }

    private MenuCreateUpdateDto createAMenuWithNotExistingRecipe() {
        MenuCreateUpdateDto menu = new MenuCreateUpdateDto();
        menu.name = "A New Menu";

        MenuCreateUpdateDto.Group group1 = new MenuCreateUpdateDto.Group();
        group1.recipes = new ArrayList<>();
        group1.recipes.add(1L);
        group1.recipes.add(2L);

        MenuCreateUpdateDto.Group group2 = new MenuCreateUpdateDto.Group();
        group2.recipes = new ArrayList<>();
        group2.recipes.add(3L);
        group2.recipes.add(42L);

        menu.groups = new ArrayList<>();
        menu.groups.add(group1);
        menu.groups.add(group2);

        return menu;
    }

    private MenuCreateUpdateDto createMenuWithTooManyItems() {
        Config config = ruleChainForTests.getApplication().config();
        int maxItems = config.getInt("cooksm.art.menu.maxrecipes");

        MenuCreateUpdateDto menuWithTooManyItems = createAMenu();
        menuWithTooManyItems.groups.clear();

        MenuCreateUpdateDto.Group group = new MenuCreateUpdateDto.Group();
        group.recipes = new ArrayList<>();

        for(int i = 0; i < maxItems + 1; i++) {
            group.recipes.add(1L);
        }

        return menuWithTooManyItems;
    }
}
