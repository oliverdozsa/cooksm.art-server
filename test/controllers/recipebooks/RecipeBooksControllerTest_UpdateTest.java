package controllers.recipebooks;

import clients.RecipeBooksTestClient;
import com.github.database.rider.core.api.dataset.DataSet;
import dto.RecipeBookCreateUpdateDto;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.mvc.Result;
import rules.RuleChainForTests;

import static extractors.DataFromResult.statusOf;
import static extractors.RecipeBooksFromResult.recipeBookNameOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static play.mvc.Http.Status.*;

public class RecipeBooksControllerTest_UpdateTest {
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
    public void testUpdateName() {
        // When
        RecipeBookCreateUpdateDto dto = new RecipeBookCreateUpdateDto();
        dto.name = "some-new-name";

        Result updateResult = client.update(1L, dto, 1L);
        Result singleResult = client.single(1L, 1L);

        // Then
        assertThat(statusOf(updateResult), equalTo(NO_CONTENT));
        assertThat(statusOf(singleResult), equalTo(OK));
        assertThat(recipeBookNameOf(singleResult), equalTo("some-new-name"));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipebooks.yml", disableConstraints = true, cleanBefore = true)
    public void testUpdateNameNotChanged() {
        // When
        RecipeBookCreateUpdateDto dto = new RecipeBookCreateUpdateDto();
        dto.name = "recipe-book-2-user-2";

        Result updateResult = client.update(2L, dto, 2L);
        Result singleResult = client.single(2L, 2L);

        // Then
        assertThat(statusOf(updateResult), equalTo(NO_CONTENT));
        assertThat(statusOf(singleResult), equalTo(OK));
        assertThat(recipeBookNameOf(singleResult), equalTo("recipe-book-2-user-2"));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipebooks.yml", disableConstraints = true, cleanBefore = true)
    public void testUpdateNameInvalid() {
        // When
        RecipeBookCreateUpdateDto dto = new RecipeBookCreateUpdateDto();
        dto.name = "s";

        Result result = client.update(1L, dto, 1L);

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipebooks.yml", disableConstraints = true, cleanBefore = true)
    public void testUpdateNewNameAlreadyExistsButForOtherRecipeBook() {
        // When
        RecipeBookCreateUpdateDto dto = new RecipeBookCreateUpdateDto();
        dto.name = "recipe-book-3-user-2";

        Result result = client.update(2L, dto, 2L);

        // Then
        assertThat(statusOf(result), equalTo(FORBIDDEN));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipebooks.yml", disableConstraints = true, cleanBefore = true)
    public void testUpdateNewNameAlreadyExistsButForAnotherUser() {
        // When
        RecipeBookCreateUpdateDto dto = new RecipeBookCreateUpdateDto();
        dto.name = "recipe-book-1-user-1";

        Result updateResult = client.update(2L, dto, 2L);
        Result singleResult = client.single(2L, 2L);

        // Then
        assertThat(statusOf(updateResult), equalTo(NO_CONTENT));
        assertThat(statusOf(singleResult), equalTo(OK));
        assertThat(recipeBookNameOf(singleResult), equalTo("recipe-book-1-user-1"));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipebooks.yml", disableConstraints = true, cleanBefore = true)
    public void testUpdateNameUserDoesNotExist() {
        // When
        RecipeBookCreateUpdateDto dto = new RecipeBookCreateUpdateDto();
        dto.name = "some-new-name";

        Result result = client.update(2L, dto, 42L);

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipebooks.yml", disableConstraints = true, cleanBefore = true)
    public void testUpdateNameRecipeBookDoesNotExist() {
        // When
        RecipeBookCreateUpdateDto dto = new RecipeBookCreateUpdateDto();
        dto.name = "some-new-name";

        Result result = client.update(42L, dto, 2L);

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }
}
