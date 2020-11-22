package controllers;

import clients.IngredientTagsTestClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.database.rider.core.api.dataset.DataSet;
import controllers.v1.routes;
import dto.IngredientTagCreateUpdateDto;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.Logger;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import rules.PlayApplicationWithGuiceDbRiderRule;
import rules.RuleChainForTests;
import utils.JwtTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static controllers.IngredientTagsControllerTestUtils.toIngredientIdList;
import static extractors.DataFromResult.statusOf;
import static extractors.IngredientTagsFromResult.ingredientIdsOfSingleIngredientTagOf;
import static extractors.IngredientTagsFromResult.singleIngredientTagNameOf;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static matchers.ResultHasLocationHeader.hasLocationHeader;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static play.mvc.Http.HeaderNames.LOCATION;
import static play.test.Helpers.*;

public class IngredientTagsControllerTest_CreateTest {
    private final RuleChainForTests ruleChainForTests = new RuleChainForTests();

    @Rule
    public RuleChain chain = ruleChainForTests.getRuleChain();

    private IngredientTagsTestClient client;

    @Before
    public void setup(){
        client = new IngredientTagsTestClient(ruleChainForTests.getApplication());
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUserDefined_Create() {
        // When
        IngredientTagCreateUpdateDto dto = new IngredientTagCreateUpdateDto();
        dto.name = "someName";
        dto.ingredientIds = Arrays.asList(1L, 2L);

        Result result = client.create(dto, 1L);

        // Then
        assertThat(statusOf(result), equalTo(CREATED));
        assertThat(result, hasLocationHeader());

        String location = result.header(LOCATION).get();
        result = client.byLocation(location, 1L);

        assertThat(statusOf(result), equalTo(OK));
        assertThat(singleIngredientTagNameOf(result), equalTo("someName"));
        assertThat(ingredientIdsOfSingleIngredientTagOf(result), hasSize(2));
        assertThat(ingredientIdsOfSingleIngredientTagOf(result), containsInAnyOrder(1L, 2L));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUserDefined_Create_InvalidName() {
        // When
        IngredientTagCreateUpdateDto dto = new IngredientTagCreateUpdateDto();
        dto.name = "s";
        dto.ingredientIds = Arrays.asList(1L, 2L);

        Result result = client.create(dto, 1L);

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUserDefined_Create_AlreadyExistingName() {
        // When
        IngredientTagCreateUpdateDto dto = new IngredientTagCreateUpdateDto();
        dto.name = "someName";
        dto.ingredientIds = Arrays.asList(1L, 2L);

        Result result = client.create(dto, 1L);

        assertThat(statusOf(result), equalTo(CREATED));

        result = client.create(dto, 1L);

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUserDefined_Create_NoIngredientIds() {
        // When
        IngredientTagCreateUpdateDto dto = new IngredientTagCreateUpdateDto();
        dto.name = "someName";
        dto.ingredientIds = Collections.emptyList();

        Result result = client.create(dto, 1L);

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUserDefined_Create_NotExistingIngredientId() {
        // When
        IngredientTagCreateUpdateDto dto = new IngredientTagCreateUpdateDto();
        dto.name = "someName";
        dto.ingredientIds = Arrays.asList(1L, 42L);

        Result result = client.create(dto, 1L);

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUserDefined_Create_LimitReached() {
        // When
        Integer maxPerUser = ruleChainForTests.getApplication().config().getInt("receptnekem.userdefinedtags.maxperuser");

        int i;
        for(i = 0; i < maxPerUser; i++) {
            IngredientTagCreateUpdateDto dto = new IngredientTagCreateUpdateDto();
            dto.name = "someName" + i;
            dto.ingredientIds = Arrays.asList(1L, 2L);

            Result result = client.create(dto, 3L);

            assertThat(statusOf(result), equalTo(CREATED));
        }

        IngredientTagCreateUpdateDto dto = new IngredientTagCreateUpdateDto();
        dto.name = "someName" + i;
        dto.ingredientIds = Arrays.asList(1L, 2L);

        Result result = client.create(dto, 3L);

        // Then
        assertThat(statusOf(result), equalTo(FORBIDDEN));
    }

    @Test
    // Given
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUserDefined_CreateWithDuplicateIngredientIds() {
        // When
        IngredientTagCreateUpdateDto dto = new IngredientTagCreateUpdateDto();
        dto.name = "someName";
        dto.ingredientIds = Arrays.asList(1L, 1L, 2L, 2L);

        Result result = client.create(dto, 1L);

        // Then
        assertThat(statusOf(result), equalTo(CREATED));
        assertThat(result, hasLocationHeader());

        String location = result.header(LOCATION).get();
        result = client.byLocation(location, 1L);

        assertThat(statusOf(result), equalTo(OK));
        assertThat(singleIngredientTagNameOf(result), equalTo("someName"));
        assertThat(ingredientIdsOfSingleIngredientTagOf(result), hasSize(2));
        assertThat(ingredientIdsOfSingleIngredientTagOf(result), containsInAnyOrder(1L, 2L));
    }
}
