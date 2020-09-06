package services;

import com.typesafe.config.Config;
import data.repositories.IngredientNameRepository;
import data.repositories.IngredientTagRepository;
import data.repositories.RecipeSearchRepository;
import data.repositories.SourcePageRepository;
import lombokized.dto.RecipeSearchDto;
import play.Logger;
import queryparams.RecipesQueryParams;

import javax.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

    public CompletionStage<String> create(RecipesQueryParams.Params query, boolean isPermanent) {
        return createHelper.create(query, isPermanent);
    }

    public CompletionStage<Long> createWithLongId(RecipesQueryParams.Params query, boolean isPermanent) {
        return createHelper.createWithLongId(query, isPermanent);
    }

    public void deleteExpired() {
        Instant thresholdDate = Instant.now().minus(validityDays, ChronoUnit.DAYS);
        List<Long> ids = recipeSearchRepository.queryNonPermanentOlderThan(thresholdDate);
        logger.info("deleteExpired(): about to delete {} old searches", ids.size());
        int deletedCount = recipeSearchRepository.deleteAll(ids);
        logger.info("deleteExpired(): deleted {} / {}", deletedCount, ids.size());
    }

    public CompletionStage<Void> update(RecipesQueryParams.Params query, boolean isPermanent, Long id){
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
}
