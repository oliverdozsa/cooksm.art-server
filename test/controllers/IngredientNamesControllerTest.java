package controllers;

import clients.IngredientNamesTestClient;
import com.github.database.rider.core.api.dataset.DataSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.mvc.Result;
import rules.RuleChainForTests;

import static matchers.ResultHasIngredientNameWithAlts.hasIngredientNameWithAlts;
import static matchers.ResultHasIngredientNameWithNoAlts.hasIngredientNameWithNoAlts;
import static matchers.ResultHasItemsSize.hasItemsSize;
import static matchers.ResultStatusIs.statusIs;
import static org.junit.Assert.assertThat;
import static play.test.Helpers.OK;

public class IngredientNamesControllerTest {
    private final RuleChainForTests ruleChainForTests = new RuleChainForTests();

    @Rule
    public RuleChain chain = ruleChainForTests.getRuleChain();

    private IngredientNamesTestClient client;

    @Before
    public void setup() {
        client = new IngredientNamesTestClient(ruleChainForTests.getApplication());
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/ingredientnames.yml", disableConstraints = true, cleanBefore = true)
    public void testListNamesHu() {
        // When
        Result result = client.page("languageId=1&nameLike=hu");

        // Then
        assertThat(result, statusIs(OK));
        assertThat(result, hasItemsSize(5));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/ingredientnames.yml", disableConstraints = true, cleanBefore = true)
    public void testPaging() {
        // When
        Result result = client.page("languageId=1&nameLike=hu&offset=2&limit=6");

        // Then
        assertThat(result, statusIs(OK));
        assertThat(result, hasItemsSize(3));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/ingredientnames.yml", disableConstraints = true, cleanBefore = true)
    public void testIngredientAltNames_hasAltNames() {
        // When
        Result result = client.page("languageId=1&nameLike=hu_1&offset=0&limit=2");

        // Then
        assertThat(result, statusIs(OK));
        assertThat(result, hasIngredientNameWithAlts(0, "ingr_1_alt_1", "ingr_1_alt_2", "ingr_1_alt_3"));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/ingredientnames.yml", disableConstraints = true, cleanBefore = true)
    public void testIngredientAltNames_noAltName() {
        // When
        Result result = client.page("languageId=2&nameLike=en_6&offset=0&limit=2");

        // Then
        assertThat(result, statusIs(OK));
        assertThat(result, hasIngredientNameWithNoAlts(0));
    }
}
