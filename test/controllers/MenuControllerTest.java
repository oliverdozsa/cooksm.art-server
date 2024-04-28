package controllers;

import clients.MenuTestClient;
import com.github.database.rider.core.api.dataset.DataSet;
import dto.MenuCreateUpdateDto;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.mvc.Result;
import rules.RuleChainForTests;

import static extractors.DataFromResult.statusOf;
import static matchers.ResultHasLocationHeader.hasLocationHeader;
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

        // When
        Result result = client.update(1L, menuToReplace, 1L);

        // Then
        assertThat(statusOf(result), equalTo(NO_CONTENT));
        fail("Assert that data has changed");
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
        fail("Assert that data returned is OK.");
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

    private MenuCreateUpdateDto createAMenu() {
        // TODO
        return null;
    }

    private MenuCreateUpdateDto createAMenuWithNotExistingRecipe() {
        // TODO
        return null;
    }
}
