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
import java.util.Map;

import static extractors.DataFromResult.itemsSizeOf;
import static extractors.DataFromResult.statusOf;
import static extractors.IngredientNamesFromResult.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static play.test.Helpers.OK;
import static play.test.Helpers.contentAsString;

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
    @Test
    public void testIngredientNames_Translate() {
        // Given
        Result result = client.page("languageId=1&nameLike=hu");
        assertThat(ingredientIdsOf(result), containsInAnyOrder(1L, 2L, 3L));
        assertThat(ingredientNamesOf(result), containsInAnyOrder("hu_1", "hu_2", "hu_3"));
        Map<String, List<String>> alternativeNamesByIngredientNames = alternativesOfIngredientNames(result);

        assertThat(alternativeNamesByIngredientNames.get("hu_1"),
                containsInAnyOrder("ingr_1_hu_alt_1", "ingr_1_hu_alt_2"));

        assertThat(alternativeNamesByIngredientNames.get("hu_2"),
                containsInAnyOrder("ingr_2_hu_alt_3"));

        // When
        result = client.byIngredientIds("languageId=2&ingredientIds[0]=1&ingredientIds[1]=2&ingredientIds[2]=3");

        // Then
        assertThat(ingredientIdsAsListOf(result), containsInAnyOrder(1L, 2L, 3L));
        assertThat(ingredientNamesAsListOf(result), containsInAnyOrder("en_1", "en_2", "en_3"));

        alternativeNamesByIngredientNames = alternativesOfIngredientNamesAsList(result);

        assertThat(alternativeNamesByIngredientNames.get("en_2"),
                containsInAnyOrder("ingr_2_en_alt_4"));
    }
}
