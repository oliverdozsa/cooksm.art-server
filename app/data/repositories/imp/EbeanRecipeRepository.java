package data.repositories.imp;

import data.entities.Recipe;
import data.repositories.RecipeRepository;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Query;
import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import lombokized.repositories.Page;
import play.Logger;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;

import static lombokized.repositories.RecipeRepositoryParams.*;

public class EbeanRecipeRepository implements RecipeRepository {
    private EbeanServer ebean;
    private static final int DEFAULT_OFFSET = 0;
    private static final int DEFAULT_LIMIT = 50;

    private static final Logger.ALogger logger = Logger.of(EbeanRecipeRepository.class);

    @Inject
    public EbeanRecipeRepository(EbeanConfig dbConfig) {
        this.ebean = Ebean.getServer(dbConfig.defaultServer());
    }

    @Override
    public Page<Recipe> pageOfQueryTypeNumber(QueryTypeNumber params) {
        logger.info("pageOfQueryTypeNumber()");
        RecipeQuerySql.Configuration configuration = createConfig(params);
        String sqlString = RecipeQuerySql.create(configuration);

        sqlString = replaceByQueryTypeNumberParameters(sqlString, params);
        Query<Recipe> query = prepare(sqlString, params.getCommon());
        setIncludedIngredientsConditions(query, params.getIncludedIngredients(), params.getCommon().getUserId());

        if(params.getAdditionalIngredients().isPresent()) {
            setAdditionalIngredientsConditions(query, params.getAdditionalIngredients().get(), params.getCommon().getUserId());
        }

        return new Page<>(query.findList(), query.findCount());
    }

    @Override
    public Page<Recipe> pageOfQueryTypeRatio(QueryTypeRatio params) {
        logger.info("pageOfQueryTypeRatio()");
        RecipeQuerySql.Configuration configuration = createConfig(params);
        String sqlString = RecipeQuerySql.create(configuration);

        sqlString = replaceQueryTypeRatioParams(sqlString, params);

        Query<Recipe> query = prepare(sqlString, params.getCommon());
        setIncludedIngredientsConditions(query, params.getIncludedIngredients(), params.getCommon().getUserId());
        if(params.getAdditionalIngredients().isPresent()) {
            setAdditionalIngredientsConditions(query, params.getAdditionalIngredients().get(), params.getCommon().getUserId());
        }

        return new Page<>(query.findList(), query.findCount());
    }

    @Override
    public Page<Recipe> pageOfQueryTypeNone(Common params) {
        logger.info("pageOfQueryTypeNone()");
        RecipeQuerySql.Configuration config = new RecipeQuerySql.Configuration(
                true,
                useExclude(params),
                RecipeQuerySql.QueryType.NONE);
        setUseFavoritesOnly(config, params);
        setUseRecipeBooks(config, params);

        String sqlString = RecipeQuerySql.create(config);
        Query<Recipe> query = prepare(sqlString, params);

        return new Page<>(query.findList(), query.findCount());
    }

    @Override
    public Recipe single(Long id) {
        logger.info("single(): id = {}", id);
        return ebean.find(Recipe.class, id);
    }

    private String replaceByQueryTypeNumberParameters(String sql, QueryTypeNumber params) {
        // All parameters used are Integers, or Enums therefore it's safe to replace them.
        String replaced = sql;
        replaced = replaced.replace(":goodIngredientsRelation", params.getGoodIngredientsRelation().getStringRep());
        replaced = replaced.replace(":goodIngredients", params.getGoodIngredients().toString());
        replaced = replaced.replace(":unknownIngredientsRelation", params.getUnknownIngredientsRelation().getStringRep());
        replaced = replaced.replace(":unknownIngredients", params.getUnknownIngredients().toString());

        if (params.getAdditionalIngredients().isPresent()) {
            replaced = replaceAdditionalIngredients(replaced, params.getAdditionalIngredients().get());
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
        setTimesCondition(query, params);
        setNameLikeCondition(query, params);
        setPagingCondition(query, params);
        setExcludedIngredientsCondition(query, params);
        setUserIdCondition(query, params);
        setUsedRecipeBooksConditions(query, params);
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

    private void setTimesCondition(Query<Recipe> query, Common params) {
        if(params.getTimes() != null && params.getTimes().size() > 0) {
            query.where().in("time", params.getTimes());
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

    private void setIncludedIngredientsConditions(Query<Recipe> query, IncludedIngredients params, Long userId) {
        EbeanIngredientTagsResolver resolver = new EbeanIngredientTagsResolver(ebean, userId);
        IngredientsConditionSetter conditionSetter = new IngredientsConditionSetter(
                resolver, params.getIncludedIngredients(), params.getIncludedIngredientTags());
        conditionSetter.set(query, "includedIngredients");
    }

    private void setExcludedIngredientsCondition(Query<Recipe> query, Common params) {
        EbeanIngredientTagsResolver resolver = new EbeanIngredientTagsResolver(ebean, params.getUserId());
        IngredientsConditionSetter conditionSetter = new IngredientsConditionSetter(
                resolver, params.getExcludedIngredients(), params.getExcludedIngredientTags());
        conditionSetter.set(query, "excludedIngredients");
    }

    private void setAdditionalIngredientsConditions(Query<Recipe> query, AdditionalIngredients additionals, Long userId) {
        EbeanIngredientTagsResolver resolver = new EbeanIngredientTagsResolver(ebean, userId);
        IngredientsConditionSetter conditionSetter = new IngredientsConditionSetter(
                resolver, additionals.getAdditionalIngredients(), additionals.getAdditionalIngredientTags());
        conditionSetter.set(query, "additionalIngredientIds");
    }

    private void setUserIdCondition(Query<Recipe> query, Common params){
        query.setParameter("userId", params.getUserId());
    }

    private void setUsedRecipeBooksConditions(Query<Recipe> query, Common params) {
        query.setParameter("usedRecipeBooks", params.getUsedRecipeBooks());
    }

    private static boolean useExclude(Common params) {
        return (params.getExcludedIngredients() != null && params.getExcludedIngredients().size() > 0) ||
                (params.getExcludedIngredientTags() != null && params.getExcludedIngredientTags().size() > 0);
    }

    private String replaceQueryTypeRatioParams(String sql, QueryTypeRatio params) {
        String replaced = sql;
        replaced = replaced.replace(":ratio", params.getGoodIngredientsRatio().toString());
        if (params.getAdditionalIngredients().isPresent()) {
            replaced = replaceAdditionalIngredients(replaced, params.getAdditionalIngredients().get());
        }

        return replaced;
    }

    private static RecipeQuerySql.Configuration createConfig(QueryTypeNumber params) {
        RecipeQuerySql.Configuration configuration = createConfigForIncludedIngredients(RecipeQuerySql.QueryType.NUMBER, params.getCommon());
        if (params.getAdditionalIngredients().isPresent()) {
            configuration.useAdditionalIngrs = true;
        }

        return configuration;
    }

    private static RecipeQuerySql.Configuration createConfig(QueryTypeRatio params) {
        RecipeQuerySql.Configuration configuration = createConfigForIncludedIngredients(RecipeQuerySql.QueryType.RATIO, params.getCommon());
        if (params.getAdditionalIngredients().isPresent()) {
            configuration.useAdditionalIngrs = true;
        }

        return configuration;
    }

    private static RecipeQuerySql.Configuration createConfigForIncludedIngredients(RecipeQuerySql.QueryType queryType, Common params) {
        RecipeQuerySql.Configuration configuration = new RecipeQuerySql.Configuration(
                true,
                useExclude(params),
                queryType
        );
        setUseFavoritesOnly(configuration, params);
        setUseRecipeBooks(configuration, params);

        return configuration;
    }

    private static void setUseFavoritesOnly(RecipeQuerySql.Configuration config, Common params) {
        if (params.getUserId() != null && Boolean.TRUE.equals(params.getUseFavoritesOnly())) {
            config.useFavoritesOnly = true;
        }
    }

    private static void setUseRecipeBooks(RecipeQuerySql.Configuration config, Common params) {
        if(params.getUsedRecipeBooks() != null && params.getUsedRecipeBooks().size() > 0) {
            config.useRecipeBooks = true;
        }
    }

    private String replaceAdditionalIngredients(String currentSql, AdditionalIngredients additionals){
        String replaced = currentSql;
        Integer goodAdditionalIngredients = additionals.getGoodAdditionalIngredients();
        replaced = replaced.replace(":goodAdditionalIngredientIds", goodAdditionalIngredients.toString());
        String relation = additionals.getGoodAdditionalIngredientsRelation().getStringRep();
        replaced = replaced.replace(":goodAdditionalIngredientRelation", relation);

        return replaced;
    }
}
