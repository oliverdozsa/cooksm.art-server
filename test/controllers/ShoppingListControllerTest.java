package controllers;

import clients.ShoppingListTestClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import rules.RuleChainForTests;

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
        // TODO
    }

    @Test
    public void testCreateWithInvalidName() {
        // TODO
    }

    @Test
    public void testCreateWithInvalidItemsPresent() {
        // TODO
    }

    @Test
    public void testGetAllOfAUser() {
        // TODO
    }

    @Test
    public void testGetSingle() {
        // TODO
    }

    @Test
    public void testGetSingleNotExistingList() {
        // TODO
    }

    @Test
    public void testDelete() {
        // TODO
    }

    @Test
    public void testDeleteNotExistingList() {
        // TODO
    }

    @Test
    public void testAddItems() {
        // TODO
    }

    @Test
    public void testAddItemsWithItemAlreadyPresent() {
        // TODO
    }

    @Test
    public void testAddItemsWithInvalidItemPresent() {
        // TODO
    }

    @Test
    public void testAddItemsToNotExistingList() {
        // TODO
    }

    @Test
    public void testRemoveItems() {
        // TODO
    }

    @Test
    public void testRemoveNotExistingItems() {
        // TODO
    }

    @Test
    public void testRemoveFromNotExistingList() {
        // TODO
    }

    @Test
    public void testRename() {
        // TODO
    }

    @Test
    public void testRenameWithInvalidName() {
        // TODO
    }

    @Test
    public void testRenameNotExistingList() {
        // TODO
    }

    @Test
    public void testCompleteAnItem() {
        // TODO
    }

    @Test
    public void testCompleteANotExistingItem() {
        // TODO
    }

    @Test
    public void testUndoAnItem() {
        // TODO
    }

    @Test
    public void testUndoANotExistingItem() {
        // TODO
    }
}
