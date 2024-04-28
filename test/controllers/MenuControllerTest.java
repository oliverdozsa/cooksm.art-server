package controllers;

import clients.MenuTestClient;
import com.github.database.rider.core.api.dataset.DataSet;
import data.entities.MenuItem;
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
import static extractors.MenuFromResult.itemsOf;
import static extractors.MenuFromResult.nameOf;
import static matchers.ResultHasLocationHeader.hasLocationHeader;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static play.mvc.Http.Status.*;

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
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/menu.yml", disableConstraints = true, cleanBefore = true)
    public void testDelete() {
        // When
        Result result = client.delete(1L, 1L);

        // Then
        assertThat(statusOf(result), equalTo(NO_CONTENT));
        result = client.delete(1L, 1L);
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/menu.yml", disableConstraints = true, cleanBefore = true)
    public void testDelete_MenuOfOtherUser() {
        // When
        Result result = client.delete(2L, 1L);

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/menu.yml", disableConstraints = true, cleanBefore = true)
    public void testDelete_MenuDoesNotExist() {
        // When
        Result result = client.delete(42L, 1L);

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/menu.yml", disableConstraints = true, cleanBefore = true)
    public void testReplace() {
        // Given
        MenuCreateUpdateDto menuToReplace = createAMenu();

        MenuCreateUpdateDto.Item newItem = new MenuCreateUpdateDto.Item();
        newItem.recipeId = 5L;
        newItem.group = 1;
        newItem.order = 1;
        menuToReplace.items.add(newItem);

        // When
        Result result = client.update(1L, menuToReplace, 1L);

        // Then
        assertThat(statusOf(result), equalTo(NO_CONTENT));
        assertThat(nameOf(result), equalTo("A New Menu"));
        List<MenuItemDto> menuItems = itemsOf(result);

        assertThat(menuItems.size(), equalTo(5));
        MenuItemDto newlyAddedItem = menuItems.stream()
                .filter(i -> i.getRecipeDto().getId() == 5L)
                .findFirst()
                .get();

        assertThat(newlyAddedItem.getGroup(), equalTo(1));
        assertThat(newlyAddedItem.getOrder(), equalTo(1));

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
        assertThat(statusOf(result), equalTo(NOT_FOUND));
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
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/menu.yml", disableConstraints = true, cleanBefore = true)
    public void testGetById() {
        // When
        Result result = client.getById(1L, 1L);

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(nameOf(result), equalTo("Alice's Menu"));
        List<MenuItemDto> items = itemsOf(result);
        List<MenuItemDto> sortedItems = items.stream()
                .sorted(Comparator.comparing(i -> i.getRecipeDto().getId()))
                .collect(Collectors.toList());

        List<Integer> groups = sortedItems.stream().map(MenuItemDto::getGroup).collect(Collectors.toList());
        List<Integer> orders = sortedItems.stream().map(MenuItemDto::getOrder).collect(Collectors.toList());

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
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/menu.yml", disableConstraints = true, cleanBefore = true)
    public void testGetAll() {
        // When
        Result result = client.getAllOf(1L);

        // Then
        assertThat(statusOf(result), equalTo(OK));
        fail("Assert that other data is OK.");
    }

    @Test
    public void testTooManyMenusCreated() {
        fail("Implement too many menus created scenario.");
    }

    @Test
    public void testTooManyRecipesInAMenu() {
        fail("Implement too many recipes created in a menu scenario.");
    }

    @Test
    public void testTooManyRecipesInAMenuDuringUpdate() {
        fail("Implement too many recipes created in a menu during update scenario.");
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
}
