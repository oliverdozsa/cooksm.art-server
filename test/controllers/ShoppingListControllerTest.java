package controllers;

import clients.ShoppingListTestClient;
import com.github.database.rider.core.api.dataset.DataSet;
import dto.ShoppingListAddRemoveItemsDto;
import dto.ShoppingListCreateDto;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.mvc.Result;
import rules.RuleChainForTests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static extractors.DataFromResult.sizeAsJsonOf;
import static extractors.DataFromResult.statusOf;
import static extractors.ShoppingListFromResult.*;
import static matchers.ResultHasLocationHeader.hasLocationHeader;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.*;

public class ShoppingListControllerTest {
    private final RuleChainForTests ruleChainForTests = new RuleChainForTests();

    private ShoppingListTestClient client;

    @Rule
    public RuleChain chain = ruleChainForTests.getRuleChain();

    @Before
    public void setup() {
        client = new ShoppingListTestClient(ruleChainForTests.getApplication());
    }

    @Test
    public void testCreate() {
        // Given
        ShoppingListCreateDto createRequest = new ShoppingListCreateDto();
        createRequest.setName("A shopping list");
        createRequest.setItems(Arrays.asList("item_1", "item_2"));

        // When
        Result result = client.create(1L, createRequest);

        // Then
        assertThat(statusOf(result), equalTo(CREATED));
        assertThat(result, hasLocationHeader());

        String locationUrl = result.headers().get("Location");
        result = client.byLocation(locationUrl, 1L);

        assertThat(itemNamesOfShoppingListOf(result), containsInAnyOrder("item_1", "item_2"));
        assertThat(idOfShoppingListOf(result), notNullValue());
    }

    @Test
    public void testCreateEmpty() {
        // Given
        ShoppingListCreateDto createRequest = new ShoppingListCreateDto();
        createRequest.setName("A shopping list");

        // When
        Result result = client.create(1L, createRequest);

        // Then
        assertThat(statusOf(result), equalTo(CREATED));
        assertThat(result, hasLocationHeader());

        String locationUrl = result.headers().get("Location");
        result = client.byLocation(locationUrl, 1L);

        assertThat(itemNamesOfShoppingListOf(result), empty());
        assertThat(idOfShoppingListOf(result), notNullValue());
    }

    @Test
    public void testCreateWithInvalidName() {
        // Given
        ShoppingListCreateDto createRequest = new ShoppingListCreateDto();
        createRequest.setName("A");

        // When
        Result result = client.create(1L, createRequest);

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    public void testCreateWithInvalidItemsPresent() {
        // Given
        ShoppingListCreateDto createRequest = new ShoppingListCreateDto();
        createRequest.setName("A shopping list");
        createRequest.setItems(Arrays.asList("item_1", "i"));

        // When
        Result result = client.create(1L, createRequest);

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    public void testCreateWouldExceedPerUserLimit() {
        // Given
        ShoppingListCreateDto createRequest = new ShoppingListCreateDto();
        createRequest.setName("A shopping list");
        createRequest.setItems(Arrays.asList("item_1", "item_2"));

        int perUserLimit = ruleChainForTests.getApplication().config()
                .getInt("receptnekem.shoppinglist.maxperuser");

        // When
        Result result;
        for (int i = 0; i < perUserLimit; i++) {
            result = client.create(1L, createRequest);
            assertThat(statusOf(result), equalTo(CREATED));
        }

        // Then
        result = client.create(1L, createRequest);
        assertThat(statusOf(result), equalTo(FORBIDDEN));
    }

    @Test
    public void testCreateWouldExceedItemsLimit() {
        // Given
        ShoppingListCreateDto createRequest = new ShoppingListCreateDto();
        createRequest.setName("A shopping list");

        int itemsLimit = ruleChainForTests.getApplication().config()
                .getInt("receptnekem.shoppinglist.maxitems");

        List<String> items = new ArrayList<>();
        for (int i = 0; i < itemsLimit + 1; i++) {
            items.add("item_" + (i + 1));
        }

        createRequest.setItems(items);

        // When
        Result result = client.create(1L, createRequest);

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/shoppinglist.yml"}, disableConstraints = true, cleanBefore = true)
    public void testGetAllOfAUser() {
        // When
        Result result = client.allOfUser(1L);

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(shoppingListNamesOf(result), containsInAnyOrder("User 1's 1st List", "User 1's 2nd List"));
        assertThat(shoppingListIdsOf(result), containsInAnyOrder(1L, 3L));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/shoppinglist.yml"}, disableConstraints = true, cleanBefore = true)
    public void testGetSingle() {
        // When
        Result result = client.getSingle(1L, 1L);

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(itemNamesOfShoppingListOf(result), containsInAnyOrder(
                "list_1_item_1",
                "list_1_item_2",
                "list_1_item_3"
        ));

        Map<String, Boolean> itemStates = itemStatesOf(result);
        assertFalse(itemStates.get("list_1_item_1"));
        assertTrue(itemStates.get("list_1_item_2"));
        assertFalse(itemStates.get("list_1_item_3"));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/shoppinglist.yml"}, disableConstraints = true, cleanBefore = true)
    public void testGetSingleNotExistingList() {
        // When
        Result result = client.getSingle(1L, 42L);

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    @DataSet(value = {"datasets/yml/shoppinglist.yml"}, disableConstraints = true, cleanBefore = true)
    public void testDelete() {
        // Given
        Result result = client.getSingle(1L, 1L);
        assertThat(statusOf(result), equalTo(OK));

        // When
        result = client.delete(1L, 1L);

        // Then
        assertThat(statusOf(result), equalTo(NO_CONTENT));

        result = client.getSingle(1L, 1L);
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/shoppinglist.yml"}, disableConstraints = true, cleanBefore = true)
    public void testDeleteNotExistingList() {
        // When
        Result result = client.delete(1L, 42L);

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    @DataSet(value = {"datasets/yml/shoppinglist.yml"}, disableConstraints = true, cleanBefore = true)
    public void testAddItems() {
        // Given
        Result result = client.getSingle(1L, 1L);
        assertThat(statusOf(result), equalTo(OK));
        assertThat(sizeAsJsonOf(result), equalTo(3));

        // When
        ShoppingListAddRemoveItemsDto addItemsRequest = new ShoppingListAddRemoveItemsDto();
        addItemsRequest.setItems(Arrays.asList("list_1_new_item"));

        result = client.addItems(1L, 1L, addItemsRequest);

        // Then
        assertThat(statusOf(result), equalTo(NO_CONTENT));

        result = client.getSingle(1L, 1L);
        assertThat(statusOf(result), equalTo(OK));
        assertThat(sizeAsJsonOf(result), equalTo(4));
        assertThat(itemNamesOfShoppingListOf(result), containsInAnyOrder("list_1_new_item"));
    }

    @Test
    public void testAddItemsWithItemAlreadyPresent() {
        // Given
        // When
        // Then

        // TODO
    }

    @Test
    public void testAddItemsWithInvalidItemPresent() {
        // Given
        // When
        // Then

        // TODO
    }

    @Test
    public void testAddItemsToNotExistingList() {
        // Given
        // When
        // Then

        // TODO
    }

    @Test
    public void testAddItemsWouldExceedListSizeLimit() {
        // Given
        // When
        // Then

        // TODO
    }

    @Test
    public void testRemoveItems() {
        // Given
        // When
        // Then

        // TODO
    }

    @Test
    public void testRemoveNotExistingItems() {
        // Given
        // When
        // Then

        // TODO
    }

    @Test
    public void testRemoveFromNotExistingList() {
        // Given
        // When
        // Then

        // TODO
    }

    @Test
    public void testRename() {
        // Given
        // When
        // Then

        // TODO
    }

    @Test
    public void testRenameWithInvalidName() {
        // Given
        // When
        // Then

        // TODO
    }

    @Test
    public void testRenameNotExistingList() {
        // Given
        // When
        // Then

        // TODO
    }

    @Test
    public void testCompleteAnItem() {
        // Given
        // When
        // Then

        // TODO
    }

    @Test
    public void testCompleteANotExistingItem() {
        // Given
        // When
        // Then

        // TODO
    }

    @Test
    public void testUndoAnItem() {
        // Given
        // When
        // Then

        // TODO
    }

    @Test
    public void testUndoANotExistingItem() {
        // Given
        // When
        // Then

        // TODO
    }
}
