package controllers;

import clients.IngredientTagsTestClient;
import com.github.database.rider.core.api.dataset.DataSet;
import dto.IngredientTagCreateUpdateDto;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.mvc.Result;
import rules.RuleChainForTests;

import java.util.Arrays;

import static extractors.DataFromResult.statusOf;
import static extractors.IngredientTagsFromResult.ingredientIdsOfSingleIngredientTagOf;
import static extractors.IngredientTagsFromResult.singleIngredientTagNameOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static play.test.Helpers.*;

public class IngredientTagsControllerTest_UpdateTest {
    private final RuleChainForTests ruleChainForTests = new RuleChainForTests();

    @Rule
    public RuleChain chain = ruleChainForTests.getRuleChain();

    private IngredientTagsTestClient client;

    @Before
    public void setup() {
        client = new IngredientTagsTestClient(ruleChainForTests.getApplication());
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUserDefined_Update() throws InterruptedException {
        // When
        IngredientTagCreateUpdateDto dto = new IngredientTagCreateUpdateDto();
        dto.name = "user_1_ingredient_tag_1_updated";
        dto.ingredientIds = Arrays.asList(3L, 4L);

        Result result = client.update(10L, dto, 1L);

        // Then
        assertThat(statusOf(result), equalTo(NO_CONTENT));

        result = client.single(1L, 10L);

        assertThat(statusOf(result), equalTo(OK));
        assertThat(singleIngredientTagNameOf(result), equalTo("user_1_ingredient_tag_1_updated"));
        assertThat(ingredientIdsOfSingleIngredientTagOf(result), hasSize(2));
        assertThat(ingredientIdsOfSingleIngredientTagOf(result), containsInAnyOrder(3L, 4L));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUserDefined_Update_NotExistingIngredientId() {
        // When
        IngredientTagCreateUpdateDto dto = new IngredientTagCreateUpdateDto();
        dto.name = "user_1_ingredient_tag_1_updated";
        dto.ingredientIds = Arrays.asList(3L, 42L);

        Result result = client.update(10L, dto, 1L);

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUserDefined_Update_NoIngredientIds() {
        // When
        IngredientTagCreateUpdateDto dto = new IngredientTagCreateUpdateDto();
        dto.name = "user_1_ingredient_tag_1_updated";

        Result result = client.update(10L, dto, 1L);

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUserDefined_UpdateWithDuplicateIngredientIds() throws InterruptedException {
        IngredientTagCreateUpdateDto dto = new IngredientTagCreateUpdateDto();
        dto.name = "user_1_ingredient_tag_1_updated";
        dto.ingredientIds = Arrays.asList(3L, 3L, 4L, 4L);

        Result result = client.update(10L, dto, 1L);

        // Then
        assertThat(statusOf(result), equalTo(NO_CONTENT));

        result = client.single(1L, 10L);

        assertThat(statusOf(result), equalTo(OK));
        assertThat(singleIngredientTagNameOf(result), equalTo("user_1_ingredient_tag_1_updated"));
        assertThat(ingredientIdsOfSingleIngredientTagOf(result), hasSize(2));
        assertThat(ingredientIdsOfSingleIngredientTagOf(result), containsInAnyOrder(3L, 4L));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUserDefined_Update_InvalidName() {
        // When
        IngredientTagCreateUpdateDto dto = new IngredientTagCreateUpdateDto();
        dto.name = "u";
        dto.ingredientIds = Arrays.asList(3L, 4L);

        Result result = client.update(10L, dto, 1L);

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUserDefined_UpdateTagOfOtherUser() {
        // When
        IngredientTagCreateUpdateDto dto = new IngredientTagCreateUpdateDto();
        dto.name = "user_1_ingredient_tag_1_updated";
        dto.ingredientIds = Arrays.asList(3L, 4L);

        Result result = client.update(14L, dto, 1L);

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }
}
