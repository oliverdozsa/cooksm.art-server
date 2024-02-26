package controllers;

import clients.IngredientNamesTestClient;
import com.github.database.rider.core.api.dataset.DataSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.mvc.Result;
import rules.RuleChainForTests;

import java.util.List;

import static extractors.DataFromResult.itemsSizeOf;
import static extractors.DataFromResult.statusOf;
import static extractors.IngredientNamesFromResult.alternativeIngredientNamesOf;
import static extractors.IngredientNamesFromResult.ingredientNameIdsOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.*;
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
        assertThat(statusOf(result), equalTo(OK));
        assertThat(itemsSizeOf(result), equalTo(5));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/ingredientnames.yml", disableConstraints = true, cleanBefore = true)
    public void testPaging() {
        // When
        Result result = client.page("languageId=1&nameLike=hu&offset=2&limit=6");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(itemsSizeOf(result), equalTo(3));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/ingredientnames.yml", disableConstraints = true, cleanBefore = true)
    public void testIngredientAltNames_hasAltNames() {
        // When
        Result result = client.page("languageId=1&nameLike=hu_1&offset=0&limit=2");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(alternativeIngredientNamesOf(result, 0), hasSize(3));
        assertThat(alternativeIngredientNamesOf(result, 0), containsInAnyOrder("ingr_1_alt_1", "ingr_1_alt_2", "ingr_1_alt_3"));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/ingredientnames.yml", disableConstraints = true, cleanBefore = true)
    public void testIngredientAltNames_noAltName() {
        // When
        Result result = client.page("languageId=2&nameLike=en_6&offset=0&limit=2");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(alternativeIngredientNamesOf(result, 0), empty());
    }

    @DataSet(value = "datasets/yml/ingredientnames-translate.yml", disableConstraints = true, cleanBefore = true)
    public void testIngredientNames_Translate() {
        // Given
        Result result = client.page("languageId=1&nameLike=hu");
        List<Long> ingredientNamesIds = ingredientNameIdsOf(result);

        assertThat(ingredientNamesIds, containsInAnyOrder(1, 3, 5));

        // TODO
        // When
        // Then
    }
}
