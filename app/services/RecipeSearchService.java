package services;

import com.typesafe.config.Config;
import data.repositories.*;
import data.repositories.exceptions.BusinessLogicViolationException;
import lombokized.dto.RecipeSearchDto;
import play.Logger;
import queryparams.RecipesQueryParams;

import javax.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class RecipeSearchService {
    private RecipeSearchRepository recipeSearchRepository;
    private IngredientNameRepository ingredientNameRepository;
    private IngredientTagRepository ingredientTagRepository;
    private SourcePageRepository sourcePageRepository;
    private LanguageService languageService;
    private Config config;
    private RecipeRepositoryQueryCheck queryCheck;

    private Integer validityDays;

    private RecipeSearchServiceGetHelper getHelper;
    private RecipeSearchServiceCreateUpdateHelper createHelper;

    private static final Logger.ALogger logger = Logger.of(RecipeSearchService.class);

    @Inject
    public RecipeSearchService(RecipeSearchRepository recipeSearchRepository, IngredientNameRepository ingredientNameRepository, IngredientTagRepository ingredientTagRepository, SourcePageRepository sourcePageRepository, LanguageService languageService, Config config, RecipeRepositoryQueryCheck queryCheck) {
        this.recipeSearchRepository = recipeSearchRepository;
        this.ingredientNameRepository = ingredientNameRepository;
        this.ingredientTagRepository = ingredientTagRepository;
        this.sourcePageRepository = sourcePageRepository;
        this.languageService = languageService;
        this.config = config;
        this.queryCheck = queryCheck;

        validityDays = config.getInt("receptnekem.recipesearches.validity.days");
        initGetHelper();
        initCreateHelper();
    }

    public CompletionStage<RecipeSearchDto> single(String id) {
        logger.info("get(): id = ", id);
        return getHelper.single(id);
    }

    public CompletionStage<String> createShared(RecipesQueryParams.Params query) {
        logger.info("create(): isPermanent = {}", query);
        // TODO: check if contains recipe book
        return containsUserDefinedIngredientTag(query)
                .thenComposeAsync(doesContain -> {
                    if (doesContain) {
                        throw new BusinessLogicViolationException("Recipe query to share contains user defined tags!");
                    }

                    return createHelper.create(query, false);
                });
    }

    public CompletionStage<Long> createWithLongId(RecipesQueryParams.Params query, boolean isPermanent) {
        logger.info("createWithLongId(): isPermanent = {}, query = {}", isPermanent, query);
        return createHelper.createWithLongId(query, isPermanent);
    }

    public void deleteExpired() {
        Instant thresholdDate = Instant.now().minus(validityDays, ChronoUnit.DAYS);
        List<Long> ids = recipeSearchRepository.queryNonPermanentOlderThan(thresholdDate);
        logger.info("deleteExpired(): about to delete {} old searches", ids.size());
        int deletedCount = recipeSearchRepository.deleteAll(ids);
        logger.info("deleteExpired(): deleted {} / {}", deletedCount, ids.size());
    }

    public CompletionStage<Void> update(RecipesQueryParams.Params query, boolean isPermanent, Long id) {
        logger.info("update(): isPermanent = {}, id = {}, query = {}", isPermanent, id, query);
        return createHelper.update(query, isPermanent, id);
    }

    private void initGetHelper() {
        getHelper = new RecipeSearchServiceGetHelper();
        getHelper.recipeSearchRepository = recipeSearchRepository;
        getHelper.ingredientNameRepository = ingredientNameRepository;
        getHelper.ingredientTagRepository = ingredientTagRepository;
        getHelper.languageService = languageService;
        getHelper.sourcePageRepository = sourcePageRepository;
    }

    private void initCreateHelper() {
        createHelper = new RecipeSearchServiceCreateUpdateHelper();
        createHelper.maxQuerySizeChars = config.getInt("receptnekem.recipesearches.maxquerysize");
        createHelper.maxQueryCount = config.getInt("receptnekem.recipesearches.maxquerycount");
        createHelper.ingredientNameRepository = ingredientNameRepository;
        createHelper.ingredientTagRepository = ingredientTagRepository;
        createHelper.languageService = languageService;
        createHelper.recipeSearchRepository = recipeSearchRepository;
        createHelper.sourcePageRepository = sourcePageRepository;
        createHelper.queryCheck = queryCheck;
    }

    private CompletionStage<Boolean> containsUserDefinedIngredientTag(RecipesQueryParams.Params queryParams) {
        List<Long> ingredientTagIds = new ArrayList<>();
        addAllOf(queryParams.inIngTags, ingredientTagIds);
        addAllOf(queryParams.exIngTags, ingredientTagIds);
        addAllOf(queryParams.addIngTags, ingredientTagIds);

        return ingredientTagRepository.containsUserDefined(ingredientTagIds);
    }

    private void addAllOf(List<Long> source, List<Long> target) {
        if (source != null) {
            target.addAll(source);
        }
    }
}
