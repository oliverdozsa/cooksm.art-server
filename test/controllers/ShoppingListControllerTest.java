package controllers;

import clients.ShoppingListTestClient;
import com.github.database.rider.core.api.dataset.DataSet;
import data.entities.User;
import dto.ShoppingListAddRemoveItemsDto;
import dto.ShoppingListCreateDto;
import extractors.DataFromResult;
import io.ebean.Ebean;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.mvc.Result;
import rules.RuleChainForTests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static extractors.DataFromResult.*;
import static extractors.ShoppingListFromResult.*;
import static matchers.ResultHasLocationHeader.hasLocationHeader;
import static org.hamcrest.CoreMatchers.*;
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
        User user = new User();
        user.setFullName("John Doe");
        user.setEmail("john@doe.com");

        Ebean.save(user);

        ShoppingListCreateDto createRequest = new ShoppingListCreateDto();
        createRequest.setName("A shopping list");
        createRequest.setItems(Arrays.asList("item_1", "item_2"));

        // When
        Result result = client.create(user.getId(), createRequest);

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
        User user = new User();
        user.setFullName("John Doe");
        user.setEmail("john@doe.com");

        Ebean.save(user);

        ShoppingListCreateDto createRequest = new ShoppingListCreateDto();
        createRequest.setName("A shopping list");

        // When
        Result result = client.create(user.getId(), createRequest);

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
        User user = new User();
        user.setFullName("John Doe");
        user.setEmail("john@doe.com");

        Ebean.save(user);

        ShoppingListCreateDto createRequest = new ShoppingListCreateDto();
        createRequest.setName("A shopping list");
        createRequest.setItems(Arrays.asList("item_1", "item_2"));

        int perUserLimit = ruleChainForTests.getApplication().config()
                .getInt("receptnekem.shoppinglist.maxperuser");

        // When
        Result result;
        for (int i = 0; i < perUserLimit; i++) {
            result = client.create(user.getId(), createRequest);
            assertThat(statusOf(result), equalTo(CREATED));
        }

        // Then
        result = client.create(user.getId(), createRequest);
        assertThat(statusOf(result), equalTo(FORBIDDEN));
    }

    @Test
    public void testCreateWouldExceedItemsLimit() {
        // Given
        ShoppingListCreateDto createRequest = new ShoppingListCreateDto();
        createRequest.setName("A shopping list");

        int itemsLimit = ruleChainForTests.getApplication().config()
                .getInt("receptnekem.shoppinglist.maxitems");

        createRequest.setItems(createNewItems(itemsLimit + 1));

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

        assertThatItemIsNotCompleted(1L, 1L, "list_1_item_1");
        assertThatItemIsCompleted(1L, 1L, "list_1_item_2");
        assertThatItemIsNotCompleted(1L, 1L, "list_1_item_3");
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
        assertThat(itemNamesOfShoppingListOf(result).size(), equalTo(3));

        // When
        ShoppingListAddRemoveItemsDto addItemsRequest = new ShoppingListAddRemoveItemsDto();
        addItemsRequest.setItems(Collections.singletonList("list_1_new_item"));

        result = client.addItems(1L, 1L, addItemsRequest);

        // Then
        assertThat(statusOf(result), equalTo(NO_CONTENT));

        result = client.getSingle(1L, 1L);
        assertThat(statusOf(result), equalTo(OK));
        assertThat(itemNamesOfShoppingListOf(result).size(), equalTo(4));
        assertThat(itemNamesOfShoppingListOf(result), hasItem("list_1_new_item"));
    }

    @Test
    @DataSet(value = {"datasets/yml/shoppinglist.yml"}, disableConstraints = true, cleanBefore = true)
    public void testAddItemsOrder() {
        // Given
        Result result = client.getSingle(1L, 1L);
        assertThat(statusOf(result), equalTo(OK));
        assertThat(itemNamesOfShoppingListOf(result).size(), equalTo(3));

        // When
        ShoppingListAddRemoveItemsDto addItemsRequest = new ShoppingListAddRemoveItemsDto();
        addItemsRequest.setItems(Arrays.asList("list_1_new_item", "list_2_new_item", "list_3_new_item"));

        result = client.addItems(1L, 1L, addItemsRequest);

        // Then
        assertThat(statusOf(result), equalTo(NO_CONTENT));

        result = client.getSingle(1L, 1L);
        assertThat(statusOf(result), equalTo(OK));
        assertThat(itemNamesOfShoppingListOf(result).size(), equalTo(6));
        // LIFO order is expected
        assertThat(itemNamesOfShoppingListOf(result),
                equalTo(Arrays.asList("list_3_new_item", "list_2_new_item", "list_1_new_item",
                        "list_1_item_3", "list_1_item_2", "list_1_item_1"))
        );
    }

    @Test
    @DataSet(value = {"datasets/yml/shoppinglist.yml"}, disableConstraints = true, cleanBefore = true)
    public void testAddItemsWithItemAlreadyPresent() {
        // Given
        Result result = client.getSingle(1L, 1L);
        assertThat(statusOf(result), equalTo(OK));
        assertThat(itemNamesOfShoppingListOf(result).size(), equalTo(3));

        // When
        ShoppingListAddRemoveItemsDto addItemsRequest = new ShoppingListAddRemoveItemsDto();
        addItemsRequest.setItems(Collections.singletonList("list_1_item_2"));

        result = client.addItems(1L, 1L, addItemsRequest);

        // Then
        assertThat(statusOf(result), equalTo(NO_CONTENT));

        result = client.getSingle(1L, 1L);
        assertThat(statusOf(result), equalTo(OK));
        assertThat(itemNamesOfShoppingListOf(result).size(), equalTo(3));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/shoppinglist.yml"}, disableConstraints = true, cleanBefore = true)
    public void testAddItemsWithInvalidItemPresent() {
        // When
        ShoppingListAddRemoveItemsDto addItemsRequest = new ShoppingListAddRemoveItemsDto();
        addItemsRequest.setItems(Arrays.asList("new_item_1", "l"));

        Result result = client.addItems(1L, 1L, addItemsRequest);

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    public void testAddItemsToNotExistingList() {
        // When
        ShoppingListAddRemoveItemsDto addItemsRequest = new ShoppingListAddRemoveItemsDto();
        addItemsRequest.setItems(Arrays.asList("new_item_1", "new_item_2"));

        Result result = client.addItems(1L, 42L, addItemsRequest);

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/shoppinglist.yml"}, disableConstraints = true, cleanBefore = true)
    public void testAddItemsWouldExceedListSizeLimit() {
        // When
        ShoppingListAddRemoveItemsDto addItemsRequest = new ShoppingListAddRemoveItemsDto();

        int itemsLimit = ruleChainForTests.getApplication().config()
                .getInt("receptnekem.shoppinglist.maxitems");

        addItemsRequest.setItems(createNewItems(itemsLimit + 1));
        Result result = client.addItems(1L, 1L, addItemsRequest);

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    @DataSet(value = {"datasets/yml/shoppinglist.yml"}, disableConstraints = true, cleanBefore = true)
    public void testRemoveItems() {
        // Given
        Result result = client.getSingle(1L, 1L);
        assertThat(statusOf(result), equalTo(OK));
        assertThat(itemNamesOfShoppingListOf(result).size(), equalTo(3));

        // When
        ShoppingListAddRemoveItemsDto itemsToRemove = new ShoppingListAddRemoveItemsDto();
        itemsToRemove.setItems(Arrays.asList("list_1_item_2"));

        result = client.removeItems(1L, 1L, itemsToRemove);

        // Then
        assertThat(statusOf(result), equalTo(NO_CONTENT));

        result = client.getSingle(1L, 1L);
        assertThat(statusOf(result), equalTo(OK));
        assertThat(itemNamesOfShoppingListOf(result).size(), equalTo(2));
        assertThat(itemNamesOfShoppingListOf(result), not(hasItem("list_1_item_2")));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/shoppinglist.yml"}, disableConstraints = true, cleanBefore = true)
    public void testRemoveNotExistingItems() {
        // When
        ShoppingListAddRemoveItemsDto itemsToRemove = new ShoppingListAddRemoveItemsDto();
        itemsToRemove.setItems(Arrays.asList("list_1_item_2", "list_1_item_42"));

        Result result = client.removeItems(1L, 1L, itemsToRemove);

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/shoppinglist.yml"}, disableConstraints = true, cleanBefore = true)
    public void testRemoveFromNotExistingList() {
        // When
        ShoppingListAddRemoveItemsDto itemsToRemove = new ShoppingListAddRemoveItemsDto();
        itemsToRemove.setItems(Arrays.asList("list_1_item_2", "list_1_item_42"));

        Result result = client.removeItems(1L, 42L, itemsToRemove);

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/shoppinglist.yml"}, disableConstraints = true, cleanBefore = true)
    public void testRename() {
        // Given
        Result result = client.getSingle(1L, 1L);
        assertThat(statusOf(result), equalTo(OK));
        assertThat(shoppingListNameOf(result), equalTo("User 1's 1st List"));

        // When
        result = client.rename(1L, 1L, "Renamed User 1's 1st List");

        // Then
        assertThat(statusOf(result), equalTo(NO_CONTENT));

        result = client.getSingle(1L, 1L);
        assertThat(statusOf(result), equalTo(OK));
        assertThat(shoppingListNameOf(result), equalTo("Renamed User 1's 1st List"));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/shoppinglist.yml"}, disableConstraints = true, cleanBefore = true)
    public void testRenameWithInvalidName() {
        // When
        Result result = client.rename(1L, 1L, "U");

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/shoppinglist.yml"}, disableConstraints = true, cleanBefore = true)
    public void testRenameNotExistingList() {
        // When
        Result result = client.rename(1L, 42L, "New list name");

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/shoppinglist.yml"}, disableConstraints = true, cleanBefore = true)
    public void testCompleteANotCompletedItem() {
        assertThatItemIsNotCompleted(1L, 1L, "list_1_item_1");

        // When
        Result result = client.completeAnItem(1L, 1L, "list_1_item_1");

        // Then
        assertThat(statusOf(result), equalTo(NO_CONTENT));
        assertThatItemIsCompleted(1L, 1L, "list_1_item_1");
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/shoppinglist.yml"}, disableConstraints = true, cleanBefore = true)
    public void testCompleteACompletedItem() {
        assertThatItemIsCompleted(1L, 1L, "list_1_item_2");

        // When
        Result result = client.completeAnItem(1L, 1L, "list_1_item_2");

        // Then
        assertThat(statusOf(result), equalTo(NO_CONTENT));
        assertThatItemIsCompleted(1L, 1L, "list_1_item_2");
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/shoppinglist.yml"}, disableConstraints = true, cleanBefore = true)
    public void testCompleteInvalidItem() {
        // When
        Result result = client.completeAnItem(1L, 1L, "l");

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/shoppinglist.yml"}, disableConstraints = true, cleanBefore = true)
    public void testCompleteANotExistingItem() {
        // When
        Result result = client.completeAnItem(1L, 1L, "list_1_item_42");

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/shoppinglist.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUndoACompletedItem() {
        assertThatItemIsCompleted(1L, 1L, "list_1_item_2");

        // When
        Result result = client.undoAnItem(1L, 1L, "list_1_item_2");

        // Then
        assertThat(statusOf(result), equalTo(NO_CONTENT));
        assertThatItemIsNotCompleted(1L, 1L, "list_1_item_2");
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/shoppinglist.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUndoInvalidItem() {
        // When
        Result result = client.undoAnItem(1L, 1L, "l");

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/shoppinglist.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUndoANotCompletedItem() {
        assertThatItemIsNotCompleted(1L, 1L, "list_1_item_1");

        // When
        Result result = client.undoAnItem(1L, 1L, "list_1_item_1");

        // Then
        assertThat(statusOf(result), equalTo(NO_CONTENT));
        assertThatItemIsNotCompleted(1L, 1L, "list_1_item_1");
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/shoppinglist.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUndoANotExistingItem() {
        // When
        Result result = client.undoAnItem(1L, 1L, "list_1_item_42");

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    private List<String> createNewItems(int size) {
        List<String> items = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            items.add("new_item_" + (i + 1));
        }

        return items;
    }

    private void assertThatItemIsCompleted(Long userId, Long shoppingListId, String item) {
        Map<String, Boolean> itemStates = getItemStatesOf(userId, shoppingListId);
        assertTrue(item + " is not completed in list " + shoppingListId, isItemCompleted(item, itemStates));
    }

    private void assertThatItemIsNotCompleted(Long userId, Long shoppingListId, String item) {
        Map<String, Boolean> itemStates = getItemStatesOf(userId, shoppingListId);
        assertFalse(item + " is completed in list " + shoppingListId, isItemCompleted(item, itemStates));
    }

    private Map<String, Boolean> getItemStatesOf(Long userId, Long shoppingListId) {
        Result result = client.getSingle(userId, shoppingListId);
        assertThat(statusOf(result), equalTo(OK));
        return itemStatesOf(result);
    }

    private boolean isItemCompleted(String item, Map<String, Boolean> itemStates) {
        return itemStates.get(item);
    }
}
