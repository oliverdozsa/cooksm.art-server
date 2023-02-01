package controllers;

import clients.ShoppingListTestClient;
import com.github.database.rider.core.api.dataset.DataSet;
import data.entities.ShoppingListItemCategory;
import data.entities.User;
import dto.ShoppingListAddItemsDto;
import dto.ShoppingListCreateDto;
import dto.ShoppingListItemRequestDto;
import dto.ShoppingListRemoveItemsDto;
import io.ebean.Ebean;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.mvc.Result;
import rules.RuleChainForTests;

import java.util.*;

import static extractors.DataFromResult.statusOf;
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

        ShoppingListItemCategory category = new ShoppingListItemCategory();
        category.setName("other");

        Ebean.save(category);

        ShoppingListCreateDto createRequest = new ShoppingListCreateDto();
        createRequest.setName("A shopping list");

        ShoppingListItemRequestDto item1 = new ShoppingListItemRequestDto();
        item1.setName("item_1");
        item1.categoryId = category.getId();

        ShoppingListItemRequestDto item2 = new ShoppingListItemRequestDto();
        item2.setName("item_2");
        item2.categoryId = category.getId();

        createRequest.setItems(Arrays.asList(item1, item2));

        // When
        Result result = client.create(user.getId(), createRequest);

        // Then
        assertThat(statusOf(result), equalTo(CREATED));
        assertThat(result, hasLocationHeader());

        String locationUrl = result.headers().get("Location");
        result = client.byLocation(locationUrl, 1L);

        assertThat(itemNamesOfShoppingListOf(result), containsInAnyOrder("item_1", "item_2"));
        assertThat(categoryIdsOfShoppingListOf(result), containsInAnyOrder(1L, 1L));
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

        ShoppingListItemRequestDto item1 = new ShoppingListItemRequestDto();
        item1.setName("item_1");

        ShoppingListItemRequestDto itemWithInvalidName = new ShoppingListItemRequestDto();
        itemWithInvalidName.setName("i");

        createRequest.setItems(Arrays.asList(item1, itemWithInvalidName));

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

        ShoppingListItemCategory category = new ShoppingListItemCategory();
        category.setName("other");

        Ebean.save(category);

        ShoppingListCreateDto createRequest = new ShoppingListCreateDto();
        createRequest.setName("A shopping list");

        ShoppingListItemRequestDto item1 = new ShoppingListItemRequestDto();
        item1.setName("item_1");
        item1.categoryId = category.getId();

        ShoppingListItemRequestDto item2 = new ShoppingListItemRequestDto();
        item2.setName("item_2");
        item2.categoryId = category.getId();

        createRequest.setItems(Arrays.asList(item1, item2));

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
        User user = new User();
        user.setFullName("John Doe");
        user.setEmail("john@doe.com");

        Ebean.save(user);

        ShoppingListCreateDto createRequest = new ShoppingListCreateDto();
        createRequest.setName("A shopping list");

        int itemsLimit = ruleChainForTests.getApplication().config()
                .getInt("receptnekem.shoppinglist.maxitems");

        createRequest.setItems(createNewItems(itemsLimit + 1));

        // When
        Result result = client.create(user.getId(), createRequest);

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

        assertThatItemIsNotCompleted(1L, 1L, 1L);
        assertThatItemIsCompleted(1L, 1L, 2L);
        assertThatItemIsNotCompleted(1L, 1L, 3L);
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
        ShoppingListAddItemsDto addItemsRequest = new ShoppingListAddItemsDto();
        ShoppingListItemRequestDto item = new ShoppingListItemRequestDto();
        item.name = "list_1_new_item";
        item.categoryId = 1L;

        addItemsRequest.setItems(Collections.singletonList(item));

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
        ShoppingListAddItemsDto addItemsRequest = new ShoppingListAddItemsDto();

        ShoppingListItemRequestDto item1 = new ShoppingListItemRequestDto();
        item1.name = "list_1_new_item";
        item1.categoryId = 1L;

        ShoppingListItemRequestDto item2 = new ShoppingListItemRequestDto();
        item2.name = "list_2_new_item";
        item2.categoryId = 1L;

        ShoppingListItemRequestDto item3 = new ShoppingListItemRequestDto();
        item3.name = "list_3_new_item";
        item3.categoryId = 1L;

        addItemsRequest.setItems(Arrays.asList(item1, item2, item3));

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
        ShoppingListAddItemsDto addItemsRequest = new ShoppingListAddItemsDto();

        ShoppingListItemRequestDto item = new ShoppingListItemRequestDto();
        item.name = "list_1_item_2";
        item.categoryId = 1L;

        addItemsRequest.setItems(Collections.singletonList(item));

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
        ShoppingListAddItemsDto addItemsRequest = new ShoppingListAddItemsDto();

        ShoppingListItemRequestDto item = new ShoppingListItemRequestDto();
        item.name = "new_item_1";
        item.categoryId = 1L;

        ShoppingListItemRequestDto itemWithInvalidName = new ShoppingListItemRequestDto();
        itemWithInvalidName.name = "l";
        itemWithInvalidName.categoryId = 1L;

        addItemsRequest.setItems(Arrays.asList(item, itemWithInvalidName));

        Result result = client.addItems(1L, 1L, addItemsRequest);

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    public void testAddItemsToNotExistingList() {
        // When
        ShoppingListAddItemsDto addItemsRequest = new ShoppingListAddItemsDto();

        ShoppingListItemRequestDto item1 = new ShoppingListItemRequestDto();
        item1.name = "new_item_1";
        item1.categoryId = 1L;

        ShoppingListItemRequestDto item2 = new ShoppingListItemRequestDto();
        item2.name = "new_item_2";
        item2.categoryId = 1L;

        addItemsRequest.setItems(Arrays.asList(item1, item2));

        Result result = client.addItems(1L, 42L, addItemsRequest);

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/shoppinglist.yml"}, disableConstraints = true, cleanBefore = true)
    public void testAddItemsWouldExceedListSizeLimit() {
        // When
        ShoppingListAddItemsDto addItemsRequest = new ShoppingListAddItemsDto();

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
        ShoppingListRemoveItemsDto itemsToRemove = new ShoppingListRemoveItemsDto();
        itemsToRemove.itemIds = new ArrayList<>();

        itemsToRemove.itemIds.add(2L);

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
        ShoppingListRemoveItemsDto itemsToRemove = new ShoppingListRemoveItemsDto();
        itemsToRemove.itemIds = new ArrayList<>();

        itemsToRemove.itemIds.add(2L);
        itemsToRemove.itemIds.add(42L);

        Result result = client.removeItems(1L, 1L, itemsToRemove);

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
    public void testRemoveFromNotExistingList() {
        // When
        ShoppingListRemoveItemsDto itemsToRemove = new ShoppingListRemoveItemsDto();
        itemsToRemove.itemIds = new ArrayList<>();

        itemsToRemove.itemIds.add(2L);
        itemsToRemove.itemIds.add(1L);

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
        assertThatItemIsNotCompleted(1L, 1L, 1L);

        // When
        Result result = client.completeAnItem(1L, 1L, 1L);

        // Then
        assertThat(statusOf(result), equalTo(NO_CONTENT));
        assertThatItemIsCompleted(1L, 1L, 1L);
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/shoppinglist.yml"}, disableConstraints = true, cleanBefore = true)
    public void testCompleteACompletedItem() {
        assertThatItemIsCompleted(1L, 1L, 2L);

        // When
        Result result = client.completeAnItem(1L, 1L, 2L);

        // Then
        assertThat(statusOf(result), equalTo(NO_CONTENT));
        assertThatItemIsCompleted(1L, 1L, 2L);
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/shoppinglist.yml"}, disableConstraints = true, cleanBefore = true)
    public void testCompleteANotExistingItem() {
        // When
        Result result = client.completeAnItem(1L, 1L, 42L);

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/shoppinglist.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUndoACompletedItem() {
        assertThatItemIsCompleted(1L, 1L, 2L);

        // When
        Result result = client.undoAnItem(1L, 1L, 2L);

        // Then
        assertThat(statusOf(result), equalTo(NO_CONTENT));
        assertThatItemIsNotCompleted(1L, 1L, 2L);
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/shoppinglist.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUndoANotCompletedItem() {
        assertThatItemIsNotCompleted(1L, 1L, 1L);

        // When
        Result result = client.undoAnItem(1L, 1L, 1L);

        // Then
        assertThat(statusOf(result), equalTo(NO_CONTENT));
        assertThatItemIsNotCompleted(1L, 1L, 1L);
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/shoppinglist.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUndoANotExistingItem() {
        // When
        Result result = client.undoAnItem(1L, 1L, 42L);

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    private List<ShoppingListItemRequestDto> createNewItems(int size) {
        List<ShoppingListItemRequestDto> items = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            ShoppingListItemRequestDto item = new ShoppingListItemRequestDto();
            item.name = "new_item_" + (i + 1);
            item.categoryId = 1L;

            items.add(item);
        }

        return items;
    }

    private void assertThatItemIsCompleted(Long userId, Long shoppingListId, Long itemId) {
        Map<Long, Boolean> itemStates = getItemStatesOf(userId, shoppingListId);
        assertTrue(itemId + " is not completed in list " + shoppingListId, itemStates.get(itemId));
    }

    private void assertThatItemIsNotCompleted(Long userId, Long shoppingListId, Long itemId) {
        Map<Long, Boolean> itemStates = getItemStatesOf(userId, shoppingListId);
        assertFalse(itemId + " is not completed in list " + shoppingListId, itemStates.get(itemId));
    }

    private Map<Long, Boolean> getItemStatesOf(Long userId, Long shoppingListId) {
        Result result = client.getSingle(userId, shoppingListId);
        assertThat(statusOf(result), equalTo(OK));
        return itemStatesOf(result);
    }

    private boolean isItemCompleted(Long itemId, Map<String, Boolean> itemStates) {
        return itemStates.get(itemId);
    }
}
