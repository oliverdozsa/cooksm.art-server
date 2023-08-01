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

        Result result = client.create("someRecipeBook", 1L);

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
        Result result = client.create("s", 1L);

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipebooks.yml", disableConstraints = true, cleanBefore = true)
    public void testCreateWithAlreadyExistingName() {
        // When
        Result result = client.create("recipe-book-1-user-1", 1L);

        // Then
        assertThat(statusOf(result), equalTo(FORBIDDEN));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipebooks.yml", disableConstraints = true, cleanBefore = true)
    public void testCreateWithAlreadyExistingNameButForOtherUser() {
        // When
        Instant beforeCreate = Instant.now();

        Result result = client.create("recipe-book-1-user-1", 2L);

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
        Result result = client.create("some-recipe-book", 3L);

        // Then
        assertThat(statusOf(result), equalTo(FORBIDDEN));
    }

    private void createMaxRecipeBooks(Long userId) {
        Config config = ruleChainForTests.getApplication().config();
        int maxPerUser = config.getInt("cooksm.art.recipebooks.maxperuser");

        for (int i = 0; i < maxPerUser; i++) {
            String name = "recipe-book-" + (i + 1) + "-user-" + userId;

            Result result = client.create(name, userId);
            assertThat(statusOf(result), equalTo(CREATED));
        }
    }
}
