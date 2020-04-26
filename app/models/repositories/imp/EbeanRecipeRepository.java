package models.repositories.imp;

import io.ebean.*;
import models.DatabaseExecutionContext;
import models.entities.IngredientTag;
import models.entities.Recipe;
import lombokized.repositories.Page;
import models.repositories.RecipeRepository;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static lombokized.repositories.RecipeRepositoryParams.*;

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
    public CompletionStage<Page<Recipe>> pageOfQueryTypeNumber(QueryTypeNumber params) {
        return supplyAsync(() -> {
            RecipeQuerySql.Configuration configuration = createConfig(params);
            String sqlString = RecipeQuerySql.create(configuration);

            sqlString = replaceByQueryTypeNumberParameters(sqlString, params);
            Query<Recipe> query = prepare(sqlString, params.getCommon());
            setIncludedIngredientsConditions(query, params.getIncludedIngredients());
            setAdditionalIngredientsConditions(query, params.getAdditionalIngredients());
            checkMutuallyExclusive(params.getIncludedIngredients(), params.getCommon().getExcludedIngredients());
            checkMutuallyExclusive(params.getAdditionalIngredients(), params.getCommon().getExcludedIngredients());
            checkMutuallyExclusive(params.getAdditionalIngredients(), params.getIncludedIngredients());

            return new Page<>(query.findList(), query.findCount());
        }, executionContext);
    }

    @Override
    public CompletionStage<Page<Recipe>> pageOfQueryTypeRatio(QueryTypeRatio params) {
        return supplyAsync(() -> {
            RecipeQuerySql.Configuration configuration = createConfig(params);
            String sqlString = RecipeQuerySql.create(configuration);

            sqlString = replaceQueryTypeRatioParams(sqlString, params);
            Query<Recipe> query = prepare(sqlString, params.getCommon());
            setIncludedIngredientsConditions(query, params.getIncludedIngredients());
            checkMutuallyExclusive(params.getIncludedIngredients(), params.getCommon().getExcludedIngredients());

            return new Page<>(query.findList(), query.findCount());

        }, executionContext);
    }

    @Override
    public CompletionStage<Page<Recipe>> pageOfQueryTypeNone(Common params) {
        return supplyAsync(() -> {
            RecipeQuerySql.Configuration config = new RecipeQuerySql.Configuration(
                    true,
                    useExclude(params),
                    RecipeQuerySql.QueryType.NONE,
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

    private String replaceByQueryTypeNumberParameters(String sql, QueryTypeNumber params) {
        // All parameters used are Integers, or Enums therefore it's safe to replace them.
        String replaced = sql;
        replaced = replaced.replace(":goodIngredientsRelation", params.getGoodIngredientsRelation().getStringRep());
        replaced = replaced.replace(":goodIngredients", params.getGoodIngredients().toString());
        replaced = replaced.replace(":unknownIngredientsRelation", params.getUnknownIngredientsRelation().getStringRep());
        replaced = replaced.replace(":unknownIngredients", params.getUnknownIngredients().toString());
        if (params.getAdditionalIngredients().isPresent()) {
            Integer goodAdditionalIngredients = params.getAdditionalIngredients().get().getGoodAdditionalIngredients();
            replaced = replaced.replace(":goodAdditionalIngredientIds", goodAdditionalIngredients.toString());
        }

        return replaced;
    }

    private Query<Recipe> prepare(String sql, Common params) {
        RawSql rawSql = setColumnMappings(RawSqlBuilder.parse(sql)).create();
        Query<Recipe> query = ebean.createQuery(Recipe.class).setRawSql(rawSql);
        setCommonConditions(query, params);

        return query;
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

    private void setCommonConditions(Query<Recipe> query, Common params) {
        setNumberOfIngredientsCondition(query, params);
        setOrderByCondition(query, params);
        setSourcePagesCondition(query, params);
        setNameLikeCondition(query, params);
        setPagingCondition(query, params);
        setExcludedIngredientsCondition(query, params);
    }

    private void setNumberOfIngredientsCondition(Query<Recipe> query, Common params) {
        if (params.getMaximumNumberOfIngredients() != null && params.getMaximumNumberOfIngredients() > 0) {
            query.where().le("numofings", params.getMaximumNumberOfIngredients());
        }

        if (params.getMinimumNumberOfIngredients() != null && params.getMinimumNumberOfIngredients() >= 0) {
            query.where().ge("numofings", params.getMinimumNumberOfIngredients());
        }
    }

    private void setOrderByCondition(Query<Recipe> query, Common params) {
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

    private void setSourcePagesCondition(Query<Recipe> query, Common params) {
        if (params.getSourcePageIds() != null && params.getSourcePageIds().size() > 0) {
            query.where().in("sourcePage.id", params.getSourcePageIds());
        }
    }

    private void setNameLikeCondition(Query<Recipe> query, Common params) {
        // Name like
        if (params.getNameLike() != null) {
            query.where().ilike("name", "%" + params.getNameLike() + "%");
        }
    }

    private void setPagingCondition(Query<Recipe> query, Common params) {
        // Paging
        int offset = params.getOffset() == null ? DEFAULT_OFFSET : params.getOffset();
        int limit = params.getLimit() == null ? DEFAULT_LIMIT : params.getLimit();

        query.setFirstRow(offset);
        query.setMaxRows(limit);
    }

    private void setIncludedIngredientsConditions(Query<Recipe> query, IncludedIngredients params) {
        // Merge tags
        if (params.getIncludedIngredientTags() != null) {
            mergeIngredientIds(params.getIncludedIngredients(), getIngredientIdsForTags(params.getIncludedIngredientTags()));
        }

        query.setParameter("includedIngredients", params.getIncludedIngredients());
    }

    private void setExcludedIngredientsCondition(Query<Recipe> query, Common params) {
        if (params.getExcludedIngredients() != null) {
            if (params.getExcludedIngredientTags() != null) {
                // Merge tags
                mergeIngredientIds(params.getExcludedIngredients(), getIngredientIdsForTags(params.getExcludedIngredientTags()));
            }

            query.setParameter("excludedIngredients", params.getExcludedIngredients());
        }
    }

    private void setAdditionalIngredientsConditions(Query<Recipe> query, Optional<AdditionalIngredients> params) {
        if (params.isPresent()) {
            AdditionalIngredients additionals = params.get();
            if (additionals.getAdditionalIngredientTags() != null) {
                mergeIngredientIds(additionals.getAdditionalIngredients(), getIngredientIdsForTags(additionals.getAdditionalIngredientTags()));
            }

            query.setParameter("additionalIngredientIds", additionals.getAdditionalIngredients());
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

    private static boolean useExclude(Common params) {
        return (params.getExcludedIngredients() != null && params.getExcludedIngredients().size() > 0) ||
                (params.getExcludedIngredientTags() != null && params.getExcludedIngredientTags().size() > 0);
    }

    private String replaceQueryTypeRatioParams(String sql, QueryTypeRatio params) {
        String replaced = sql;
        replaced = replaced.replace(":ratio", params.getGoodIngredientsRatio().toString());
        return replaced;
    }

    private static void checkMutuallyExclusive(Optional<AdditionalIngredients> additional, List<Long> excluded) {
        if(additional.isPresent() && !areMutuallyExclusive(additional.get().getAdditionalIngredients(), excluded)){
            throw new IllegalArgumentException("Additional and excluded ingredients are not mutually exclusive!");
        }
    }

    private static void checkMutuallyExclusive(Optional<AdditionalIngredients> additional, IncludedIngredients included) {
        if(additional.isPresent() && !areMutuallyExclusive(additional.get().getAdditionalIngredients(), included.getIncludedIngredients())){
            throw new IllegalArgumentException("Additional and included ingredients are not mutually exclusive!");
        }
    }

    private static void checkMutuallyExclusive(IncludedIngredients included, List<Long> excluded) {
        if(!areMutuallyExclusive(included.getIncludedIngredients(), excluded)){
            throw new IllegalArgumentException("Included and excluded ingredients are not mutually exclusive!");
        }
    }

    private static boolean areMutuallyExclusive(List<Long> included, List<Long> excluded) {
        if (included == null || excluded == null) {
            return true;
        }

        for (Long id : included) {
            if (excluded.contains(id)) {
                return false;
            }
        }

        return true;
    }

    private static RecipeQuerySql.Configuration createConfig(QueryTypeNumber params) {
        RecipeQuerySql.Configuration configuration = createConfigForIncludedIngredients(RecipeQuerySql.QueryType.NUMBER, params.getCommon());
        if (params.getAdditionalIngredients().isPresent()) {
            configuration.useAdditionalIngrs = true;
        }

        return configuration;
    }

    private static RecipeQuerySql.Configuration createConfig(QueryTypeRatio params) {
        return createConfigForIncludedIngredients(RecipeQuerySql.QueryType.RATIO, params.getCommon());
    }

    private static RecipeQuerySql.Configuration createConfigForIncludedIngredients(RecipeQuerySql.QueryType queryType, Common params) {
        return new RecipeQuerySql.Configuration(
                true,
                useExclude(params),
                queryType
        );
    }
}
