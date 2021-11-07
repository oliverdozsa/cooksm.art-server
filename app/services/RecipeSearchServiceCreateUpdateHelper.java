package services;

import data.entities.RecipeSearch;
import data.entities.SourcePage;
import data.repositories.IngredientNameRepository;
import data.repositories.IngredientTagRepository;
import data.repositories.RecipeSearchRepository;
import data.repositories.SourcePageRepository;
import data.repositories.exceptions.BusinessLogicViolationException;
import data.repositories.exceptions.ForbiddenExeption;
import lombokized.repositories.Page;
import lombokized.repositories.RecipeRepositoryParams;
import play.Logger;
import play.libs.Json;
import queryparams.RecipesQueryParams;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class RecipeSearchServiceCreateUpdateHelper {
    RecipeSearchRepository recipeSearchRepository;
    IngredientNameRepository ingredientNameRepository;
    IngredientTagRepository ingredientTagRepository;
    SourcePageRepository sourcePageRepository;
    LanguageService languageService;
    Integer maxQuerySizeChars;
    Integer maxQueryCount;
    RecipeRepositoryQueryCheck queryCheck;

    private static final Logger.ALogger logger = Logger.of(RecipeSearchServiceCreateUpdateHelper.class);

    public String create(RecipesQueryParams.Params query, boolean isPermanent) {
        Long id = createWithLongId(query, isPermanent);
        return Base62Conversions.encode(id);
    }

    public Long createWithLongId(RecipesQueryParams.Params query, boolean isPermanent) {
        prepareQuery(query);
        logger.info("createWithLongId(): query = {}, isPermanent = {}", query, isPermanent);

        checkQueryCount();
        checkQueryParams(query);

        String queryStr = Json.toJson(query).toString();
        checkQueryLength(queryStr);

        RecipeSearch recipeSearch = recipeSearchRepository.create(queryStr, isPermanent);
        return recipeSearch.getId();
    }

    public void update(RecipesQueryParams.Params query, boolean isPermanent, Long id) {
        prepareQuery(query);
        logger.info("update(): query = {}, isPermanent = {}, id = {}", query, isPermanent, id);

        checkQueryParams(query);

        String queryStr = Json.toJson(query).toString();
        checkQueryLength(queryStr);
        recipeSearchRepository.update(queryStr, isPermanent, id);
    }

    private void prepareQuery(RecipesQueryParams.Params query) {
        query.offset = null;
        query.limit = null;

        if (query.inIngs != null && query.inIngs.size() == 0) {
            query.inIngs = null;
        }

        if (query.exIngs != null && query.exIngs.size() == 0) {
            query.exIngs = null;
        }

        if (query.addIngs != null && query.addIngs.size() == 0) {
            query.addIngs = null;
        }

        if (query.inIngTags != null && query.inIngTags.size() == 0) {
            query.inIngTags = null;
        }

        if (query.exIngTags != null && query.exIngTags.size() == 0) {
            query.exIngTags = null;
        }

        if (query.addIngTags != null && query.addIngTags.size() == 0) {
            query.addIngTags = null;
        }
    }

    private void checkQueryParams(RecipesQueryParams.Params query) {
        RecipesQueryParams.SearchMode searchMode = RecipesQueryParams.Params.toEnum(query.searchMode);
        if (searchMode == RecipesQueryParams.SearchMode.COMPOSED_OF_NUMBER) {
            RecipeRepositoryParams.QueryTypeNumber queryTypeNumber = RecipesQueryParamsMapping.toQueryTypeNumber(query);
            queryCheck.check(queryTypeNumber);
        } else if (searchMode == RecipesQueryParams.SearchMode.COMPOSED_OF_RATIO) {
            RecipeRepositoryParams.QueryTypeRatio queryTypeRatio = RecipesQueryParamsMapping.toQueryTypeRatio(query);
            queryCheck.check(queryTypeRatio);
        }

        checkEntitiesExist(query);
    }

    private void checkEntitiesExist(RecipesQueryParams.Params query) {
        Long usedLanguageId = languageService.getLanguageIdOrDefault(query.languageId);

        List<List<Long>> ingredientIdsToCheck = new ArrayList<>();
        addIfNotEmpty(query.inIngs, ingredientIdsToCheck);
        addIfNotEmpty(query.exIngs, ingredientIdsToCheck);
        addIfNotEmpty(query.addIngs, ingredientIdsToCheck);
        for (List<Long> ids : ingredientIdsToCheck) {
            ingredientNameRepository.byIngredientIds(ids, usedLanguageId);
        }

        List<List<Long>> ingredientTagIdsToCheck = new ArrayList<>();
        addIfNotEmpty(query.inIngTags, ingredientTagIdsToCheck);
        addIfNotEmpty(query.exIngTags, ingredientTagIdsToCheck);
        addIfNotEmpty(query.addIngTags, ingredientTagIdsToCheck);
        for (List<Long> ids : ingredientTagIdsToCheck) {
            ingredientTagRepository.byIds(ids);
        }

        if (query.sourcePages != null) {
            Page<SourcePage> sourcePagesPage = sourcePageRepository.allSourcePages();
            List<Long> ids = sourcePagesPage.getItems()
                    .stream()
                    .map(SourcePage::getId)
                    .collect(Collectors.toList());
            if (!ids.containsAll(query.sourcePages)) {
                throw new IllegalArgumentException("Some source pages are not present in DB!");
            }
        }
    }

    private void addIfNotEmpty(List<Long> list, List<List<Long>> result) {
        if (list != null) {
            result.add(list);
        }
    }

    private void checkQueryCount() {
        if (recipeSearchRepository.countAll() >= maxQueryCount) {
            throw new ForbiddenExeption("Query count limit reached!");
        }
    }

    private void checkQueryLength(String queryStr) {
        logger.info("checkQueryLength(): query length = {}", queryStr.length());
        if (queryStr.length() > maxQuerySizeChars) {
            throw new BusinessLogicViolationException("Query is too long!");
        }
    }
}
