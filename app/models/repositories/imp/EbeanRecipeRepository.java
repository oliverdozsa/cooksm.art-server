package models.repositories.imp;

import io.ebean.*;
import models.DatabaseExecutionContext;
import models.entities.IngredientTag;
import models.entities.Recipe;
import lombokized.repositories.Page;
import models.repositories.RecipeRepository;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static lombokized.repositories.RecipeRepositoryQuery.*;

public class EbeanRecipeRepository implements RecipeRepository {
    private EbeanServer ebean;
    private DatabaseExecutionContext executionContext;
    private static final int DEFAULT_OFFSET = 0;
    private static final int DEFAULT_LIMIT = 50;

    @Inject
    public EbeanRecipeRepository(EbeanConfig dbConfig, DatabaseExecutionContext executionContext) {
        this.ebean = Ebean.getServer(dbConfig.defaultServer());
        this.executionContext = executionContext;
    }

    @Override
    public CompletionStage<Page<Recipe>> pageOfByGoodIngredientsNumber(WithGoodIngredientsNumberParams params) {
        return supplyAsync(() -> {
            String sqlString = createByGoodIngredientsSqlString(RecipeQuerySql.QueryType.NUMBER,
                    params.getCommonParams());

            sqlString = replaceByGoodIngredientsNumberParameters(sqlString, params);
            Query<Recipe> query = prepare(sqlString, params.getCommonParams());
            setIncludedIngredientsConditions(query, params.getRecipesWithIncludedIngredientsParams());
            checkIncludedExcludedIngredientsMutuallyExclusive(
                    params.getRecipesWithIncludedIngredientsParams(), params.getCommonParams());

            return new Page<>(query.findList(), query.findCount());
        }, executionContext);
    }

    @Override
    public CompletionStage<Page<Recipe>> pageOfByGoodIngredientsRatio(WithGoodIngredientsRatioParams params) {
        return supplyAsync(() -> {
            String sqlString = createByGoodIngredientsSqlString(RecipeQuerySql.QueryType.RATIO,
                    params.getCommonParams());

            sqlString = replaceByGoodIngredientsRatioParameters(sqlString, params);
            Query<Recipe> query = prepare(sqlString, params.getCommonParams());
            setIncludedIngredientsConditions(query, params.getRecipesWithIncludedIngredientsParams());
            checkIncludedExcludedIngredientsMutuallyExclusive(
                    params.getRecipesWithIncludedIngredientsParams(), params.getCommonParams());

            return new Page<>(query.findList(), query.findCount());

        }, executionContext);
    }

    @Override
    public CompletionStage<Page<Recipe>> pageOfAll(CommonParams params) {
        return supplyAsync(() -> {
            RecipeQuerySql.Configuration config = new RecipeQuerySql.Configuration(
                    true,
                    useExclude(params),
                    RecipeQuerySql.QueryType.ALL,
                    false);

            String sqlString = RecipeQuerySql.create(config);
            Query<Recipe> query = prepare(sqlString, params);

            return new Page<>(query.findList(), query.findCount());

        }, executionContext);
    }

    @Override
    public CompletionStage<Recipe> single(Long id) {
        return supplyAsync(() -> ebean.find(Recipe.class, id), executionContext);
    }

    private RawSqlBuilder setColumnMappings(RawSqlBuilder builder) {
        builder
                .columnMapping("recipe.id", "id")
                .columnMapping("recipe.name", "name")
                .columnMapping("recipe.url", "url")
                .columnMapping("recipe.date_added", "dateAdded")
                .columnMapping("recipe.numofings", "numofings")
                .columnMapping("recipe.time", "time")
                .columnMapping("recipe.source_page_id", "sourcePage.id");

        return builder;
    }

    private void setCommonConditions(Query<Recipe> query, CommonParams params) {
        setNumberOfIngredientsCondition(query, params);
        setOrderByCondition(query, params);
        setSourcePagesCondition(query, params);
        setNameLikeCondition(query, params);
        setPagingCondition(query, params);
        setExcludedIngredientsCondition(query, params);
    }

    private void setNumberOfIngredientsCondition(Query<Recipe> query, CommonParams params) {
        if (params.getMaximumNumberOfIngredients() != null && params.getMaximumNumberOfIngredients() > 0) {
            query.where().le("numofings", params.getMaximumNumberOfIngredients());
        }

        if (params.getMinimumNumberOfIngredients() != null && params.getMinimumNumberOfIngredients() >= 0) {
            query.where().ge("numofings", params.getMinimumNumberOfIngredients());
        }
    }

    private void setOrderByCondition(Query<Recipe> query, CommonParams params) {
        /*
           Sorting by id is necessary to avoid occurring the same results across different pages.
           E.g. sorting by number of ingredient is not a unique sorting as many recipes have the same number
           of ingredients.
        */
        if (params.getOrderBy() != null && params.getOrderBySort() != null) {
            query.orderBy(params.getOrderBy() + " " + params.getOrderBySort() + ", id");
        } else {
            query.orderBy("id");
        }
    }

    private void setSourcePagesCondition(Query<Recipe> query, CommonParams params) {
        if (params.getSourcePageIds() != null && params.getSourcePageIds().size() > 0) {
            query.where().in("sourcePage.id", params.getSourcePageIds());
        }
    }

    private void setNameLikeCondition(Query<Recipe> query, CommonParams params) {
        // Name like
        if (params.getNameLike() != null) {
            query.where().ilike("name", "%" + params.getNameLike() + "%");
        }
    }

    private void setPagingCondition(Query<Recipe> query, CommonParams params) {
        // Paging
        int offset = params.getOffset() == null ? DEFAULT_OFFSET : params.getOffset();
        int limit = params.getLimit() == null ? DEFAULT_LIMIT : params.getLimit();

        query.setFirstRow(offset);
        query.setMaxRows(limit);
    }

    private void setIncludedIngredientsConditions(Query<Recipe> query, WithIncludedIngredientsParams params) {
        // Merge tags
        if (params.getIncludedIngredientTags() != null) {
            mergeIngredientIds(params.getIncludedIngredients(), getIngredientIdsForTags(params.getIncludedIngredientTags()));
        }

        query.setParameter("includedIngredients", params.getIncludedIngredients());
    }

    private void setExcludedIngredientsCondition(Query<Recipe> query, CommonParams params) {
        if (params.getExcludedIngredients() != null) {
            if (params.getExcludedIngredientTags() != null) {
                // Merge tags
                mergeIngredientIds(params.getExcludedIngredients(), getIngredientIdsForTags(params.getExcludedIngredientTags()));
            }

            query.setParameter("excludedIngredients", params.getExcludedIngredients());
        }
    }

    private List<Long> getIngredientIdsForTags(List<Long> ingredientTagIds) {
        List<Long> result = new ArrayList<>();
        if (ingredientTagIds != null && ingredientTagIds.size() > 0) {
            ebean.createQuery(IngredientTag.class)
                    .where()
                    .in("id", ingredientTagIds)
                    .findList()
                    .forEach(tag -> tag.getIngredients().forEach(ingr -> result.add(ingr.getId())));
        }

        return result;
    }

    // Modifies target.
    private static void mergeIngredientIds(List<Long> target, List<Long> source) {
        Set<Long> ids = new HashSet<>(target);
        ids.addAll(source);
        target.clear();
        target.addAll(ids);
    }

    private static boolean useExclude(CommonParams params) {
        return (params.getExcludedIngredients() != null && params.getExcludedIngredients().size() > 0) ||
                (params.getExcludedIngredientTags() != null && params.getExcludedIngredientTags().size() > 0);
    }

    private Query<Recipe> prepare(String sql, CommonParams params) {
        RawSql rawSql = setColumnMappings(RawSqlBuilder.parse(sql)).create();
        Query<Recipe> query = ebean.createQuery(Recipe.class).setRawSql(rawSql);
        setCommonConditions(query, params);

        return query;
    }

    private String replaceByGoodIngredientsNumberParameters(String sql, WithGoodIngredientsNumberParams params) {
        // All parameters used are Integers, or Enums therefore it's safe to replace them.
        String replaced = sql;
        replaced = replaced.replace(":goodIngredientsRelation", params.getGoodIngredientsRelation().getStringRep());
        replaced = replaced.replace(":goodIngredients", params.getGoodIngredients().toString());
        replaced = replaced.replace(":unknownIngredientsRelation", params.getUnknownIngredientsRelation().getStringRep());
        replaced = replaced.replace(":unknownIngredients", params.getUnknownIngredients().toString());

        return replaced;
    }

    private String replaceByGoodIngredientsRatioParameters(String sql, WithGoodIngredientsRatioParams params) {
        String replaced = sql;

        replaced = replaced.replace(":ratio", params.getGoodIngredientsRatio().toString());

        return replaced;
    }

    private void checkIncludedExcludedIngredientsMutuallyExclusive(
            WithIncludedIngredientsParams params, CommonParams baseParams) {
        // Check, that excluded and included ingredients are mutually exclusive
        if (baseParams.getExcludedIngredients() != null && params.getIncludedIngredients() != null) {
            checkMutuallyExclusive(params.getIncludedIngredients(), baseParams.getExcludedIngredients());
        }
    }

    private static void checkMutuallyExclusive(List<Long> included, List<Long> excluded){
        for (Long id : included) {
            if (excluded.contains(id)) {
                throw new IllegalArgumentException(("Included and excluded ingredients are not mutually exclusive!"));
            }
        }
    }

    private static String createByGoodIngredientsSqlString(RecipeQuerySql.QueryType queryType, CommonParams params) {
        return RecipeQuerySql.create(
                new RecipeQuerySql.Configuration(
                        true,
                        useExclude(params),
                        queryType)
        );
    }
}
