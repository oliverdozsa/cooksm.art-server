package controllers;

import clients.RecipeSearchesTestClient;
import com.github.database.rider.core.api.dataset.DataSet;
import data.entities.*;
import data.repositories.RecipeSearchRepository;
import data.repositories.imp.EbeanRecipeSearchRepository;
import io.ebean.Ebean;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.Application;
import play.mvc.Result;
import rules.RuleChainForTests;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static extractors.DataFromResult.statusOf;
import static extractors.RecipeSearchesFromResult.*;
import static junit.framework.TestCase.assertEquals;
import static matchers.ResultHasLocationHeader.hasLocationHeader;
import static matchers.ResultHasNullForField.hasNullForField;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;
import static play.mvc.Http.HeaderNames.LOCATION;
import static play.mvc.Http.Status.*;
import static play.test.Helpers.contentAsString;

public class RecipeSearchesControllerTest {
    private final RuleChainForTests ruleChainForTests = new RuleChainForTests();

    @Rule
    public RuleChain chain = ruleChainForTests.getRuleChain();

    private RecipeSearchesTestClient client;
    private List<Long> createdIds = new ArrayList<>();

    @Before
    public void setup() {
        client = new RecipeSearchesTestClient(ruleChainForTests.getApplication());
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipesearches.yml", disableConstraints = true, cleanBefore = true)
    public void testSingle() {
        // When
        Result result = client.single(239329L);

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(searchModeOfSingleRecipeSearchOf(result), equalTo("composed-of-ratio"));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipesearches.yml", disableConstraints = true, cleanBefore = true)
    public void testCreate() {
        // When
        Result result = client.create(
                "{" +
                        "  \"searchMode\": \"composed-of-number\"," +
                        "  \"goodIngs\": 3," +
                        "  \"goodIngsRel\": \"ge\"," +
                        "  \"unknownIngs\": \"0\"," +
                        "  \"unknownIngsRel\": \"ge\"," +
                        "  \"goodAdditionalIngs\": 2," +
                        "  \"goodAdditionalIngsRel\": \"ge\"," +
                        "  \"inIngs\": [1, 2, 3]," +
                        "  \"inIngTags\": [1]," +
                        "  \"exIngs\": [4, 7]," +
                        "  \"exIngTags\": [2]," +
                        "  \"addIngs\": [5]," +
                        "  \"addIngTags\": [6]," +
                        "  \"sourcePages\": [1, 2]" +
                        "}");

        // Then
        assertThat(statusOf(result), equalTo(CREATED));
        assertThat(result, hasLocationHeader());

        String url = result.header(LOCATION).get();

        result = client.byLocation(url);

        assertThat(statusOf(result), equalTo(OK));
        assertThat(searchModeOfSingleRecipeSearchOf(result), equalTo("composed-of-number"));
        assertThat(goodIngredientsOfSingleRecipeSearchOf(result), equalTo(3));
        assertThat(includedIngredientsOfSingleRecipeSearchOf(result), hasSize(3));
        assertThat(includedIngredientTagsOfSingleRecipeSearchOf(result), hasSize(1));
        assertThat(excludedIngredientsOfSingleRecipeSearchOf(result), hasSize(2));
        assertThat(excludedIngredientTagsOfSingleRecipeSearchOf(result), hasSize(1));
        assertThat(additionalIngredientsOfSingleRecipeSearchOf(result), hasSize(1));
        assertThat(additionalIngredientTagsOfSingleRecipeSearchOf(result), hasSize(1));
        assertThat(sourcePagesOfSingleRecipeSearchOf(result), hasSize(2));

        assertThat(includedIngredientsOfSingleRecipeSearchOf(result), containsInAnyOrder("ingredient_1", "ingredient_2", "ingredient_3"));
        assertThat(includedIngredientTagsOfSingleRecipeSearchOf(result), containsInAnyOrder("ingredient_tag_1"));
        assertThat(excludedIngredientsOfSingleRecipeSearchOf(result), containsInAnyOrder("ingredient_4", "ingredient_7"));
        assertThat(excludedIngredientTagsOfSingleRecipeSearchOf(result), containsInAnyOrder("ingredient_tag_2"));
        assertThat(additionalIngredientsOfSingleRecipeSearchOf(result), containsInAnyOrder("ingredient_5"));
        assertThat(additionalIngredientTagsOfSingleRecipeSearchOf(result), containsInAnyOrder("ingredient_tag_6"));
        assertThat(sourcePagesOfSingleRecipeSearchOf(result), containsInAnyOrder("src_pg_1", "src_pg_2"));
    }

    @Test
    @DataSet(value = "datasets/yml/recipesearches.yml", disableConstraints = true, cleanBefore = true)
    public void testExpires() throws InterruptedException {
        // Given
        RecipeSearch search = new RecipeSearch();
        search.setQuery("someQuery");
        search.setLastAccessed(Instant.now());
        search.setPermanent(false);
        Ebean.save(search);

        // When
        Thread.sleep(15000L);

        // Then
        int count = Ebean.createQuery(RecipeSearch.class).findCount();
        assertEquals("Expired searches are not deleted!", 2, count);

    }

    @Test
    @DataSet(value = "datasets/yml/recipesearches-limit.yml", disableConstraints = true, cleanBefore = true)
    public void testQueryCountLimitReached() throws Exception {
        // Given
        createMaxNumberOfSearches();

        // When
        Result result = client.create(
                "{" +
                        "  \"searchMode\": \"composed-of-number\"," +
                        "  \"goodIngs\": 3," +
                        "  \"goodIngsRel\": \"ge\"," +
                        "  \"inIngs\": [1, 2, 3]," +
                        "  \"unknownIngs\": 0," +
                        "  \"unknownIngsRel\": \"ge\"" +
                        "}");

        // Then
        assertThat(statusOf(result), equalTo(FORBIDDEN));

        dropCreatedMaxNumberSearches();
    }

    @Test
    public void testCreate_InvalidSearchMode() {
        // When
        Result result = client.create(
                "{" +
                        "  \"searchMode\": \"some-random-search-mode\"," +
                        "  \"goodIngs\": 3," +
                        "  \"goodIngsRel\": \"ge\"," +
                        "  \"inIngs\": [1, 2, 3]" +
                        "}");

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipesearches.yml", disableConstraints = true, cleanBefore = true)
    public void testCreate_InvalidNotMutuallyExclusive() {
        boolean isMutualExclusiveCheckDisabled = ruleChainForTests
                .getApplication().config().getBoolean("receptnekem.disable.mutual.exclusion.check");
        assumeFalse("Mutual exclusion check is disabled.", isMutualExclusiveCheckDisabled);

        // When
        Result result = client.create(
                "{" +
                        "  \"searchMode\": \"composed-of-number\"," +
                        "  \"goodIngs\": 3," +
                        "  \"goodIngsRel\": \"ge\"," +
                        "  \"unknownIngs\": \"0\"," +
                        "  \"unknownIngsRel\": \"ge\"," +
                        "  \"unknownIngsRel\": \"ge\"," +
                        "  \"inIngs\": [1, 2, 3]," +
                        "  \"exIngs\": [1, 5, 6]" +
                        "}");

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipesearches.yml", disableConstraints = true, cleanBefore = true)
    public void testCreate_InvalidNotMutuallyExclusive_Disabled() {
        boolean isMutualExclusiveCheckDisabled = ruleChainForTests
                .getApplication().config().getBoolean("receptnekem.disable.mutual.exclusion.check");
        assumeTrue("Mutual exclusion check is enabled.", isMutualExclusiveCheckDisabled);

        // When
        Result result = client.create(
                "{" +
                        "  \"searchMode\": \"composed-of-number\"," +
                        "  \"goodIngs\": 3," +
                        "  \"goodIngsRel\": \"ge\"," +
                        "  \"unknownIngs\": \"0\"," +
                        "  \"unknownIngsRel\": \"ge\"," +
                        "  \"unknownIngsRel\": \"ge\"," +
                        "  \"inIngs\": [1, 2, 3]," +
                        "  \"exIngs\": [1, 5, 6]" +
                        "}");

        // Then
        assertThat(statusOf(result), equalTo(CREATED));
    }

    @Test
    public void testQuerySizeLimitReached() {
        // Given
        prepareForQuerySizeLimit();

        // When
        Result result = client.create(
                "{" +
                        "  \"searchMode\": \"composed-of-number\"," +
                        "  \"goodIngs\": 3," +
                        "  \"goodIngsRel\": \"ge\"," +
                        "  \"unknownIngs\": \"0\"," +
                        "  \"unknownIngsRel\": \"ge\"," +
                        "  \"goodAdditionalIngs\": 2," +
                        "  \"goodAdditionalIngsRel\": \"ge\"," +
                        "  \"inIngs\": [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20]," +
                        "  \"inIngTags\": [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11]," +
                        "  \"exIngs\": [21, 22, 23, 24, 25]," +
                        "  \"exIngTags\": [6, 7, 8]," +
                        "  \"addIngs\": [26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40]," +
                        "  \"addIngTags\": [9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27]," +
                        "  \"sourcePages\": [1, 2]" +
                        "}");

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
        assertThat(contentAsString(result), containsString("too long"));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipesearches.yml", disableConstraints = true, cleanBefore = true)
    public void testCreate_InvalidEntityDoesntExist() {
        // When
        Result result = client.create(
                "{" +
                        "  \"searchMode\": \"composed-of-number\"," +
                        "  \"goodIngs\": 3," +
                        "  \"goodIngsRel\": \"ge\"," +
                        "  \"unknownIngs\": \"0\"," +
                        "  \"unknownIngsRel\": \"ge\"," +
                        "  \"goodAdditionalIngs\": 2," +
                        "  \"goodAdditionalIngsRel\": \"ge\"," +
                        "  \"inIngs\": [1, 2, 3, 42]," +
                        "  \"inIngTags\": [1]," +
                        "  \"exIngs\": [4, 7]," +
                        "  \"exIngTags\": [2]," +
                        "  \"addIngs\": [5]," +
                        "  \"addIngTags\": [6]," +
                        "  \"sourcePages\": [1, 2]" +
                        "}");

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipesearches.yml", disableConstraints = true, cleanBefore = true)
    public void testCreate_UseFavoritesOnly() {
        // When
        Result result = client.create(
                "{" +
                        "  \"searchMode\": \"composed-of-number\"," +
                        "  \"goodIngs\": 3," +
                        "  \"goodIngsRel\": \"ge\"," +
                        "  \"unknownIngs\": \"0\"," +
                        "  \"unknownIngsRel\": \"ge\"," +
                        "  \"inIngs\": [1, 2, 3]," +
                        "  \"inIngTags\": [1]," +
                        "  \"exIngs\": [4, 7]," +
                        "  \"exIngTags\": [2]," +
                        "  \"useFavoritesOnly\": true" +
                        "}",
                1L);

        // Then
        assertThat(statusOf(result), equalTo(CREATED));
        assertThat(result, hasLocationHeader());

        String location = result.header(LOCATION).get();

        result = client.byLocation(location);

        assertThat(statusOf(result), equalTo(OK));
        assertThat(result, hasNullForField("$.query.useFavoritesOnly"));
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/recipesearches.yml", disableConstraints = true, cleanBefore = true)
    public void createWithUserDefinedTags() {
        // When
        Result result = client.create(
                "{" +
                        "  \"searchMode\": \"composed-of-number\"," +
                        "  \"goodIngs\": 3," +
                        "  \"goodIngsRel\": \"ge\"," +
                        "  \"unknownIngs\": \"0\"," +
                        "  \"unknownIngsRel\": \"ge\"," +
                        "  \"goodAdditionalIngs\": 2," +
                        "  \"goodAdditionalIngsRel\": \"ge\"," +
                        "  \"inIngs\": [1, 2, 3]," +
                        "  \"inIngTags\": [1]," +
                        "  \"exIngs\": [4, 7]," +
                        "  \"exIngTags\": [7]," +
                        "  \"addIngs\": [5]," +
                        "  \"addIngTags\": [6]," +
                        "  \"sourcePages\": [1, 2]" +
                        "}");

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    private void createMaxNumberOfSearches() throws Exception {
        Application application = ruleChainForTests.getApplication();
        EbeanRecipeSearchRepository repository = (EbeanRecipeSearchRepository) (application.injector().instanceOf(RecipeSearchRepository.class));
        Class repoClass = Class.forName("data.repositories.imp.EbeanRecipeSearchRepository");
        Field countField = repoClass.getDeclaredField("count");
        countField.setAccessible(true);
        AtomicInteger count = (AtomicInteger) (countField.get(repository));
        count.set(0);

        // Fill DB with max number of searches.
        int maxSearches = application.config().getInt("receptnekem.recipesearches.maxquerycount");
        createdIds = new ArrayList<>();
        for (int i = 0; i < maxSearches; i++) {
            Long id = repository.create("someQuery", true).getId();
            createdIds.add(id);
        }
    }

    private void dropCreatedMaxNumberSearches() throws ExecutionException, InterruptedException {
        Application application = ruleChainForTests.getApplication();
        EbeanRecipeSearchRepository repository = (EbeanRecipeSearchRepository) (application.injector().instanceOf(RecipeSearchRepository.class));
        for (Long id : createdIds) {
            repository.delete(id);
        }
    }

    private void prepareForQuerySizeLimit() {
        Language language = new Language();
        language.setIsoName("hu");
        Ebean.save(language);

        SourcePage sourcePage1 = new SourcePage();
        sourcePage1.setId(1L);
        sourcePage1.setName("source-1");
        sourcePage1.setLanguage(language);
        Ebean.save(sourcePage1);

        SourcePage sourcePage2 = new SourcePage();
        sourcePage2.setId(2L);
        sourcePage2.setName("source-2");
        sourcePage2.setLanguage(language);
        Ebean.save(sourcePage2);

        for (int i = 0; i < 60; i++) {
            Ingredient ingredient = new Ingredient();
            Ebean.save(ingredient);
            IngredientName ingredientName = new IngredientName();
            ingredientName.setLanguage(language);
            ingredientName.setName("" + i);
            ingredientName.setIngredient(ingredient);
            Ebean.save(ingredientName);
        }

        for (int i = 0; i < 60; i++) {
            IngredientTag ingredientTag = new IngredientTag();
            Ebean.save(ingredientTag);
        }
    }
}
