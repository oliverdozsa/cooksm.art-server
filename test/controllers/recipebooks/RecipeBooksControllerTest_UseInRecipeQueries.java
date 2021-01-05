package controllers.recipebooks;

import clients.RecipesTestClient;
import com.github.database.rider.core.api.dataset.DataSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import rules.RuleChainForTests;

public class RecipeBooksControllerTest_UseInRecipeQueries {
    private final RuleChainForTests ruleChainForTests = new RuleChainForTests();

    @Rule
    public RuleChain chain = ruleChainForTests.getRuleChain();

    private RecipesTestClient client;

    @Before
    public void setup() {
        client = new RecipesTestClient(ruleChainForTests.getApplication());
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/recipes.yml", "datasets/yml/recipebooks-query.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUseInRecipeQuery() {
        // TODO
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/recipes.yml", "datasets/yml/recipebooks-query.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUseInRecipeQuery_NotExistingBook() {
        // TODO
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/recipes.yml", "datasets/yml/recipebooks-query.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUseInRecipeQuery_OtherUsersBook() {
        // TODO
    }
}
