package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import data.repositories.IngredientNameRepository;
import data.repositories.IngredientTagRepository;
import data.repositories.RecipeSearchRepository;
import data.repositories.SourcePageRepository;
import data.repositories.exceptions.BusinessLogicViolationException;
import data.repositories.exceptions.ForbiddenExeption;
import data.repositories.exceptions.NotFoundException;
import io.seruco.encoding.base62.Base62;
import lombokized.dto.RecipeSearchDto;
import lombokized.repositories.RecipeRepositoryParams;
import play.Logger;
import play.libs.Json;
import queryparams.RecipesQueryParams;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.runAsync;

public class RecipeSearchService {
    @Inject
    private RecipeSearchRepository recipeSearchRepository;

    @Inject
    private IngredientNameRepository ingredientNameRepository;

    @Inject
    private IngredientTagRepository ingredientTagRepository;

    @Inject
    private SourcePageRepository sourcePageRepository;

    @Inject
    private LanguageService languageService;

    @Inject
    private Config config;

    private Base62 base62 = Base62.createInstance();

    private Integer maxQuerySizeChars;
    private Integer maxQueryCount;

    private CompletionStage<Void> noop = runAsync(() -> {
    });

    private static final Logger.ALogger logger = Logger.of(RecipeSearchService.class);

    public CompletionStage<RecipeSearchDto> get(String id) {
        byte[] idBytes = base62.decode(id.getBytes());
        long decodedId = new BigInteger(idBytes).longValue();
        RecipeSearchQueryDtoResolver resolver = new RecipeSearchQueryDtoResolver();
        resolver.setIngredientNameRepository(ingredientNameRepository);
        resolver.setIngredientTagRepository(ingredientTagRepository);
        resolver.setSourcePageRepository(sourcePageRepository);

        return recipeSearchRepository.read(decodedId)
                .thenComposeAsync(entity -> {
                    if (entity == null) {
                        throw new NotFoundException("RecipeSearch not found. decodedId = " + decodedId);
                    }

                    JsonNode queryJson = Json.parse(entity.getQuery());
                    RecipesQueryParams.Params queryParams = Json.fromJson(queryJson, RecipesQueryParams.Params.class);
                    resolver.setQueryParams(queryParams);
                    resolver.setUsedLanguageId(languageService.getLanguageIdOrDefault(queryParams.languageId));
                    return resolver.resolve();
                })
                .thenApplyAsync(RecipeSearchDto::new);
    }

    public CompletionStage<String> create(RecipesQueryParams.Params query, boolean isPermanent) {
        prepareQuery(query);
        logger.info("create(): query = {}, isPermanent = {}", query, isPermanent);

        return runAsync(this::checkQueryCount)
                .thenComposeAsync(v -> checkQueryParams(query))
                .thenComposeAsync(dto -> {
                    String queryStr = Json.toJson(query).toString();
                    if (queryStr.length() > getMaxQuerySizeChars()) {
                        throw new BusinessLogicViolationException("Query is too long! length = " + queryStr.length());
                    }

                    return recipeSearchRepository.create(queryStr, isPermanent);
                })
                .thenApplyAsync(id -> {
                    byte[] encodedId = base62.encode(BigInteger.valueOf(id).toByteArray());
                    return new String(encodedId);
                });
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

    private CompletionStage<Void> checkEntitiesExist(RecipesQueryParams.Params query) {
        CompletionStage<Void> checkStage = runAsync(() -> {
        });
        Long usedLanguageId = languageService.getLanguageIdOrDefault(query.languageId);

        if (query.inIngs != null) {
            checkStage = checkStage.thenComposeAsync(v -> ingredientNameRepository.byIngredientIds(query.inIngs, usedLanguageId))
                    .thenComposeAsync(l -> noop);
        }

        if (query.exIngs != null) {
            checkStage = checkStage.thenComposeAsync(v -> ingredientNameRepository.byIngredientIds(query.exIngs, usedLanguageId))
                    .thenComposeAsync(l -> noop);
        }

        if (query.addIngs != null) {
            checkStage = checkStage.thenComposeAsync(v -> ingredientNameRepository.byIngredientIds(query.addIngs, usedLanguageId))
                    .thenComposeAsync(l -> noop);
        }

        if (query.inIngTags != null) {
            checkStage = checkStage.thenComposeAsync(v -> ingredientTagRepository.byIds(query.inIngTags))
                    .thenComposeAsync(l -> noop);
        }

        if (query.exIngTags != null) {
            checkStage = checkStage.thenComposeAsync(v -> ingredientTagRepository.byIds(query.exIngTags))
                    .thenComposeAsync(l -> noop);
        }

        if (query.addIngTags != null) {
            checkStage = checkStage.thenComposeAsync(v -> ingredientTagRepository.byIds(query.addIngTags))
                    .thenComposeAsync(l -> noop);
        }

        return checkStage;
    }

    public Integer getMaxQuerySizeChars() {
        if (maxQuerySizeChars == null) {
            maxQuerySizeChars = config.getInt("receptnekem.recipesearches.maxquerysize");
        }

        return maxQuerySizeChars;
    }

    private void checkQueryCount() {
        if (recipeSearchRepository.getCount() >= getMaxQueryCount()) {
            throw new ForbiddenExeption("Query count limit reached!");
        }
    }

    public Integer getMaxQueryCount() {
        if (maxQueryCount == null) {
            maxQueryCount = config.getInt("receptnekem.recipesearches.maxquerycount");
        }

        return maxQueryCount;
    }
}
