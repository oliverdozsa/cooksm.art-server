package models.repositories.imp;

import io.ebean.*;
import models.DatabaseExecutionContext;
import models.entities.IngredientTag;
import models.entities.Recipe;
import models.repositories.Page;
import models.repositories.RecipeRepositoryQueryParams;
import models.repositories.RecipeRepository;
import play.Logger;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class EbeanRecipeRepository implements RecipeRepository {
    private EbeanServer ebean;
    private DatabaseExecutionContext executionContext;
    private static final Logger.ALogger logger = Logger.of(EbeanRecipeRepository.class);

    @Inject
    public EbeanRecipeRepository(EbeanConfig dbConfig, DatabaseExecutionContext executionContext) {
        this.ebean = Ebean.getServer(dbConfig.defaultServer());
        this.executionContext = executionContext;
    }

    @Override
    public CompletionStage<Page<Recipe>> pageOfByGoodIngredientsNumber(RecipeRepositoryQueryParams.OfGoodIngredientsNumber params) {
        return supplyAsync(() -> {
            String sqlString = RecipeQuerySql.create(
                    new RecipeQuerySql.Configuration(
                            true,
                            useExclude(params.getBase()),
                            RecipeQuerySql.QueryType.NUMBER)

            );

            sqlString = sqlString.replace(":goodIngredientsRelation", params.getGoodIngredientsRelation().getStringRep());
            sqlString = sqlString.replace(":goodIngredients", params.getGoodIngredients().toString());
            sqlString = sqlString.replace(":unknownIngredientsRelation", params.getUnknownIngredientsRelation().getStringRep());
            sqlString = sqlString.replace(":unknownIngredients", params.getUnknownIngredients().toString());

            RawSql rawSql = setColumnMappings(RawSqlBuilder.parse(sqlString)).create();

            Query<Recipe> query = ebean.createQuery(Recipe.class).setRawSql(rawSql);
            setBaseParamConditions(query, params.getBase());

            logger.info("pageOfByGoodIngredientsNumber(): params = {}", params.toString());

            return new Page<>(query.findList(), query.findCount());
        }, executionContext);
    }

    @Override
    public CompletionStage<Page<Recipe>> pageOfByGoodIngredientsRatio(RecipeRepositoryQueryParams.OfGoodIngredientsRatio params) {
        return supplyAsync(() -> {
            String sqlString = RecipeQuerySql.create(
                    new RecipeQuerySql.Configuration(
                            true,
                            useExclude(params.getBase()),
                            RecipeQuerySql.QueryType.RATIO)

            );

            sqlString = sqlString.replace(":ratio", params.getGoodIngredientsRatio().toString());

            RawSql rawSql = setColumnMappings(RawSqlBuilder.parse(sqlString)).create();

            Query<Recipe> query = ebean.createQuery(Recipe.class).setRawSql(rawSql);
            setBaseParamConditions(query, params.getBase());

            logger.info("pageOfByGoodIngredientsRatio(): params = {}", params.toString());

            return new Page<>(query.findList(), query.findCount());

        }, executionContext);
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

    private void setBaseParamConditions(Query<Recipe> query, RecipeRepositoryQueryParams.Base params) {
        setIncludedIngredientsCondition(query, params);
        setExcludedIngredientsCondition(query, params);
        setNumberOfIngredientsCondition(query, params);
        setOrderByCondition(query, params);
        setSourcePagesCondition(query, params);
        setNameLikeCondition(query, params);
        setPagingCondition(query, params);

        // Check, that excluded and included ingredients are mutually exclusive
        if (params.getExcludedIngredients() != null && params.getIncludedIngredients() != null) {
            for (Long id : params.getExcludedIngredients()) {
                if (params.getIncludedIngredients().contains(id)) {
                    throw new IllegalArgumentException(("Included and excluded ingredients are not mutually exclusive!"));
                }
            }
        }
    }

    private void setNumberOfIngredientsCondition(Query<Recipe> query, RecipeRepositoryQueryParams.Base params) {
        if (params.getMaximumNumberOfIngredients() > 0) {
            query.where().le("numofings", params.getMaximumNumberOfIngredients());
        }

        if (params.getMinimumNumberOfIngredients() > 0) {
            query.where().ge("numofings", params.getMinimumNumberOfIngredients());
        }
    }

    private void setIncludedIngredientsCondition(Query<Recipe> query, RecipeRepositoryQueryParams.Base params) {
        // Merge tags
        if (params.getIncludedIngredientTags() != null) {
            mergeIngredientIds(params.getIncludedIngredients(), getIngredientIdsForTags(params.getIncludedIngredientTags()));
        }

        query.setParameter("includedIngredients", params.getIncludedIngredients());
    }

    private void setExcludedIngredientsCondition(Query<Recipe> query, RecipeRepositoryQueryParams.Base params) {
        if (params.getExcludedIngredients() != null && params.getExcludedIngredients().size() > 0) {
            if (params.getExcludedIngredientTags() != null) {
                mergeIngredientIds(params.getExcludedIngredients(), getIngredientIdsForTags(params.getExcludedIngredientTags()));
            }

            query.setParameter("excludedIngredients", params.getExcludedIngredients());
        }
    }

    private void setOrderByCondition(Query<Recipe> query, RecipeRepositoryQueryParams.Base params) {
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

    private void setSourcePagesCondition(Query<Recipe> query, RecipeRepositoryQueryParams.Base params) {
        if (params.getSourcePageIds() != null && params.getSourcePageIds().size() > 0) {
            query.where().in("sourcePage.id", params.getSourcePageIds());
        }
    }

    private void setNameLikeCondition(Query<Recipe> query, RecipeRepositoryQueryParams.Base params) {
        // Name like
        if (params.getNameLike() != null) {
            query.where().ilike("name", "%" + params.getNameLike() + "%");
        }
    }

    private void setPagingCondition(Query<Recipe> query, RecipeRepositoryQueryParams.Base params) {
        // Paging
        query.setFirstRow(params.getOffset());
        query.setMaxRows(params.getLimit());
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

    private static void mergeIngredientIds(List<Long> target, List<Long> source) {
        Set<Long> ids = new HashSet<>(target);
        ids.addAll(source);
        target.clear();
        target.addAll(ids);
    }

    private static boolean useExclude(RecipeRepositoryQueryParams.Base params){
        return params.getExcludedIngredients() != null && params.getExcludedIngredients().size() > 0;
    }
}
