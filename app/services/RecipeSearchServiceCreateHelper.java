package services;

import data.entities.SourcePage;
import data.repositories.IngredientNameRepository;
import data.repositories.IngredientTagRepository;
import data.repositories.RecipeSearchRepository;
import data.repositories.SourcePageRepository;
import data.repositories.exceptions.BusinessLogicViolationException;
import data.repositories.exceptions.ForbiddenExeption;
import io.seruco.encoding.base62.Base62;
import lombokized.repositories.RecipeRepositoryParams;
import play.Logger;
import play.libs.Json;
import queryparams.RecipesQueryParams;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.runAsync;

class RecipeSearchServiceCreateHelper {
    RecipeSearchRepository recipeSearchRepository;
    IngredientNameRepository ingredientNameRepository;
    IngredientTagRepository ingredientTagRepository;
    SourcePageRepository sourcePageRepository;
    LanguageService languageService;
    Integer maxQuerySizeChars;
    Integer maxQueryCount;

    private Base62 base62 = Base62.createInstance();

    private CompletionStage<Void> noop = runAsync(() -> {
    });

    private static final Logger.ALogger logger = Logger.of(RecipeSearchServiceCreateHelper.class);

    public CompletionStage<String> create(RecipesQueryParams.Params query, boolean isPermanent) {
        return createWithLongId(query, isPermanent)
                .thenApplyAsync(id -> {
                    byte[] encodedId = base62.encode(BigInteger.valueOf(id).toByteArray());
                    return new String(encodedId);
                });
    }

    public CompletionStage<Long> createWithLongId(RecipesQueryParams.Params query, boolean isPermanent) {
        prepareQuery(query);
        logger.info("createWithLongId(): query = {}, isPermanent = {}", query, isPermanent);

        return runAsync(this::checkQueryCount)
                .thenComposeAsync(v -> checkQueryParams(query))
                .thenComposeAsync(dto -> {
                    String queryStr = Json.toJson(query).toString();
                    logger.info("create(): query length = {}", queryStr.length());
                    if (queryStr.length() > maxQuerySizeChars) {
                        throw new BusinessLogicViolationException("Query is too long!");
                    }

                    return recipeSearchRepository.create(queryStr, isPermanent);
                });
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

    private CompletionStage<Void> checkQueryParams(RecipesQueryParams.Params query) {
        return runAsync(() -> {
            RecipesQueryParams.SearchMode searchMode = RecipesQueryParams.Params.toEnum(query.searchMode);
            if (searchMode == RecipesQueryParams.SearchMode.COMPOSED_OF_NUMBER) {
                RecipeRepositoryParams.QueryTypeNumber queryTypeNumber = RecipesQueryParamsMapping.toQueryTypeNumber(query);
                RecipeRepositoryQueryCheck.check(queryTypeNumber);
            } else if (searchMode == RecipesQueryParams.SearchMode.COMPOSED_OF_RATIO) {
                RecipeRepositoryParams.QueryTypeRatio queryTypeRatio = RecipesQueryParamsMapping.toQueryTypeRatio(query);
                RecipeRepositoryQueryCheck.check(queryTypeRatio);
            }
        }).thenComposeAsync(v -> checkEntitiesExist(query));
    }

    private CompletionStage<Void> checkEntitiesExist(RecipesQueryParams.Params query) {
        CompletionStage<Void> checkStage = runAsync(() -> {
        });
        Long usedLanguageId = languageService.getLanguageIdOrDefault(query.languageId);

        List<List<Long>> ingredientIdsToCheck = new ArrayList<>();
        addIfNotEmpty(query.inIngs, ingredientIdsToCheck);
        addIfNotEmpty(query.exIngs, ingredientIdsToCheck);
        addIfNotEmpty(query.addIngs, ingredientIdsToCheck);
        for (List<Long> ids : ingredientIdsToCheck) {
            checkStage = checkStage.thenComposeAsync(v -> ingredientNameRepository.byIngredientIds(ids, usedLanguageId))
                    .thenComposeAsync(l -> noop);
        }

        List<List<Long>> ingredientTagIdsToCheck = new ArrayList<>();
        addIfNotEmpty(query.inIngTags, ingredientTagIdsToCheck);
        addIfNotEmpty(query.exIngTags, ingredientTagIdsToCheck);
        addIfNotEmpty(query.addIngTags, ingredientTagIdsToCheck);
        for (List<Long> ids : ingredientTagIdsToCheck) {
            checkStage = checkStage.thenComposeAsync(v -> ingredientTagRepository.byIds(ids))
                    .thenComposeAsync(l -> noop);
        }

        if (query.sourcePages != null) {
            sourcePageRepository.allSourcePages().thenAcceptAsync(p -> {
                List<Long> ids = p.getItems()
                        .stream()
                        .map(SourcePage::getId)
                        .collect(Collectors.toList());
                if (!ids.containsAll(query.sourcePages)) {
                    throw new IllegalArgumentException("Some source pages are not present in DB!");
                }
            });
        }

        return checkStage;
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
}
