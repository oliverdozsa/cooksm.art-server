package controllers;

import clients.MenuTestClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.database.rider.core.api.dataset.DataSet;
import com.typesafe.config.Config;
import dto.MenuCreateUpdateDto;
import lombokized.dto.MenuItemDto;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.mvc.Result;
import rules.RuleChainForTests;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static extractors.DataFromResult.statusOf;
import static extractors.MenuFromResult.*;
import static matchers.ResultHasLocationHeader.hasLocationHeader;
import static org.hamcrest.Matchers.contains;
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

        MenuCreateUpdateDto.Item newItem = new MenuCreateUpdateDto.Item();
        newItem.recipeId = 5L;
        newItem.group = 3;
        newItem.order = 4;
        menuToReplace.items.add(newItem);

        // When
        Result result = client.update(1L, menuToReplace, 1L);

        // Then
        assertThat(contentAsString(result), statusOf(result), equalTo(NO_CONTENT));

        result = client.getById(1L, 1L);
        assertThat(contentAsString(result), statusOf(result), equalTo(OK));
        assertThat(nameOf(result), equalTo("A New Menu"));
        List<JsonNode> menuItems = itemsOf(result);

        assertThat("Updated menu size should be 5", menuItems.size(), equalTo(5));
        JsonNode newlyAddedItem = menuItems.stream()
                .filter(i -> i.get("recipeDto").get("id").asLong() == 5L)
                .findFirst()
                .get();

        assertThat(newlyAddedItem.get("group").asInt(), equalTo(3));
        assertThat(newlyAddedItem.get("order").asInt(), equalTo(4));

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
        List<JsonNode> items = itemsOf(result);
        List<JsonNode> sortedItems = items.stream()
                .sorted(Comparator.comparing(i -> i.get("recipeDto").get("id").asLong()))
                .collect(Collectors.toList());

        List<Integer> groups = sortedItems.stream().map(i -> i.get("group").asInt()).collect(Collectors.toList());
        List<Integer> orders = sortedItems.stream().map(i -> i.get("order").asInt()).collect(Collectors.toList());

        assertThat(groups, contains(1, 1, 2, 2));
        assertThat(orders, contains(1, 2, 1, 2));
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

    @Test
    @DataSet(value = "datasets/yml/menu.yml", disableConstraints = true, cleanBefore = true)
    public void testItemsAreNotUniqueWhenCreating() {
        // Given
        MenuCreateUpdateDto menu = createAMenu();
        menu.items.add(menu.items.get(0));

        // When
        Result result = client.create(menu, 1L);

        // Then
        assertThat(contentAsString(result), statusOf(result), equalTo(BAD_REQUEST));
    }

    private MenuCreateUpdateDto createAMenu() {
        MenuCreateUpdateDto menu = new MenuCreateUpdateDto();
        menu.name = "A New Menu";

        MenuCreateUpdateDto.Item item1 = new MenuCreateUpdateDto.Item();
        item1.recipeId = 1L;
        item1.group = 1;
        item1.order = 1;

        MenuCreateUpdateDto.Item item2 = new MenuCreateUpdateDto.Item();
        item2.recipeId = 2L;
        item2.group = 1;
        item2.order = 2;

        MenuCreateUpdateDto.Item item3 = new MenuCreateUpdateDto.Item();
        item3.recipeId = 3L;
        item3.group = 2;
        item3.order = 1;

        MenuCreateUpdateDto.Item item4 = new MenuCreateUpdateDto.Item();
        item4.recipeId = 4L;
        item4.group = 2;
        item4.order = 2;

        menu.items = new ArrayList<>();
        menu.items.add(item1);
        menu.items.add(item2);
        menu.items.add(item3);
        menu.items.add(item4);

        return menu;
    }

    private MenuCreateUpdateDto createAMenuWithNotExistingRecipe() {
        MenuCreateUpdateDto menu = new MenuCreateUpdateDto();
        menu.name = "A Bad Menu";

        MenuCreateUpdateDto.Item item1 = new MenuCreateUpdateDto.Item();
        item1.recipeId = 1L;
        item1.group = 1;
        item1.order = 1;

        MenuCreateUpdateDto.Item item2 = new MenuCreateUpdateDto.Item();
        item2.recipeId = 2L;
        item2.group = 1;
        item2.order = 2;

        MenuCreateUpdateDto.Item item3 = new MenuCreateUpdateDto.Item();
        // Not existing recipe
        item3.recipeId = 42L;
        item3.group = 2;
        item3.order = 1;

        MenuCreateUpdateDto.Item item4 = new MenuCreateUpdateDto.Item();
        item4.recipeId = 4L;
        item4.group = 2;
        item4.order = 2;

        menu.items = new ArrayList<>();
        menu.items.add(item1);
        menu.items.add(item2);
        menu.items.add(item3);
        menu.items.add(item4);

        return menu;
    }

    private MenuCreateUpdateDto createMenuWithTooManyItems() {
        Config config = ruleChainForTests.getApplication().config();
        int maxItems = config.getInt("cooksm.art.menu.maxitems");

        MenuCreateUpdateDto menuWithTooManyItems = createAMenu();
        menuWithTooManyItems.items.clear();

        for(int i = 0; i < maxItems + 1; i++) {
            MenuCreateUpdateDto.Item item = new MenuCreateUpdateDto.Item();
            item.order = i + 1;
            item.group = i % 5 + 1;
            menuWithTooManyItems.items.add(item);
        }

        return menuWithTooManyItems;
    }
}
