package controllers.recipebooks;

import clients.RecipeBooksTestClient;
import com.github.database.rider.core.api.dataset.DataSet;
import com.typesafe.config.Config;
import dto.RecipeBookCreateUpdateDto;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.mvc.Result;
import rules.RuleChainForTests;

import java.time.Instant;

import static extractors.DataFromResult.statusOf;
import static extractors.RecipeBooksFromResult.lastAccessedDateOfRecipeBookOf;
import static extractors.RecipeBooksFromResult.recipeBookNameOf;
import static matchers.ResultHasLocationHeader.hasLocationHeader;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static play.mvc.Http.HeaderNames.LOCATION;
import static play.mvc.Http.Status.*;

public class RecipeBooksControllerTest_CreateTest {
    private final RuleChainForTests ruleChainForTests = new RuleChainForTests();

    @Rule
    public RuleChain chain = ruleChainForTests.getRuleChain();

    private RecipeBooksTestClient client;

    @Before
    public void setup() {
        client = new RecipeBooksTestClient(ruleChainForTests.getApplication());
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipebooks.yml", disableConstraints = true, cleanBefore = true)
    public void testCreate() {
        // When
        Instant beforeCreate = Instant.now();
        RecipeBookCreateUpdateDto dto = new RecipeBookCreateUpdateDto();
        dto.name = "someRecipeBook";

        Result result = client.create(dto, 1L);

        // Then
        assertThat(statusOf(result), equalTo(CREATED));
        assertThat(result, hasLocationHeader());

        String location = result.header(LOCATION).get();
        result = client.byLocation(location, 1L);

        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeBookNameOf(result), equalTo("someRecipeBook"));
        assertThat(lastAccessedDateOfRecipeBookOf(result), greaterThan(beforeCreate));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipebooks.yml", disableConstraints = true, cleanBefore = true)
    public void testCreateWithInvalidName() {
        // When
        RecipeBookCreateUpdateDto dto = new RecipeBookCreateUpdateDto();
        dto.name = "s";

        Result result = client.create(dto, 1L);

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipebooks.yml", disableConstraints = true, cleanBefore = true)
    public void testCreateWithAlreadyExistingName() {
        // When
        RecipeBookCreateUpdateDto dto = new RecipeBookCreateUpdateDto();
        dto.name = "recipe-book-1-user-1";

        Result result = client.create(dto, 1L);

        // Then
        assertThat(statusOf(result), equalTo(FORBIDDEN));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipebooks.yml", disableConstraints = true, cleanBefore = true)
    public void testCreateWithAlreadyExistingNameButForOtherUser() {
        // When
        Instant beforeCreate = Instant.now();
        RecipeBookCreateUpdateDto dto = new RecipeBookCreateUpdateDto();
        dto.name = "recipe-book-1-user-1";

        Result result = client.create(dto, 2L);

        // Then
        assertThat(statusOf(result), equalTo(CREATED));
        assertThat(result, hasLocationHeader());

        String location = result.header(LOCATION).get();
        result = client.byLocation(location, 2L);

        assertThat(statusOf(result), equalTo(OK));
        assertThat(recipeBookNameOf(result), equalTo("recipe-book-1-user-1"));
        assertThat(lastAccessedDateOfRecipeBookOf(result), greaterThan(beforeCreate));
    }

    @Test
    @DataSet(value = "datasets/yml/recipebooks.yml", disableConstraints = true, cleanBefore = true)
    public void testCreateLimitReached() {
        // Given
        createMaxRecipeBooks(3L);

        // When
        RecipeBookCreateUpdateDto dto = new RecipeBookCreateUpdateDto();
        dto.name = "some-recipe-book";

        Result result = client.create(dto, 3L);

        // Then
        assertThat(statusOf(result), equalTo(FORBIDDEN));
    }

    private void createMaxRecipeBooks(Long userId) {
        Config config = ruleChainForTests.getApplication().config();
        int maxPerUser = config.getInt("receptnekem.recipebooks.maxperuser");

        for (int i = 0; i < maxPerUser; i++) {
            RecipeBookCreateUpdateDto dto = new RecipeBookCreateUpdateDto();
            dto.name = "recipe-book-" + (i + 1) + "-user-" + userId;

            Result result = client.create(dto, userId);
            assertThat(statusOf(result), equalTo(CREATED));
        }
    }
}
