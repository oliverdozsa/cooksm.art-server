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
    public CompletionStage<Page<Recipe>> pageOfByGoodIngredientsRatio(RecipeRepositoryQueryParams.ByGoodIngredientsNumber params) {
        return supplyAsync(() -> {
            String sqlString = RecipeQuerySql.createRecipesByGoodIngredientsNumberSql(
                    true,
                    params.getExcludedIngredients() != null && params.getExcludedIngredients().size() > 0
            );

            sqlString = setBaseParamsByReplace(sqlString, params);
            sqlString = sqlString.replace(":goodIngredients", params.getGoodIngredients().toString());

            RawSql rawSql = setColumnMappings(RawSqlBuilder.parse(sqlString)).create();

            Query<Recipe> query = ebean.createQuery(Recipe.class).setRawSql(rawSql);
            setBaseParams(query, params);

            return new Page<>(query.findList(), query.findCount());
        }, executionContext);
    }

    @Override
    public CompletionStage<Page<Recipe>> pageOfByGoodIngredientsRatio(RecipeRepositoryQueryParams.ByGoodIngredientsRatio params) {
        return null;
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

    private String setBaseParamsByReplace(String sql, RecipeRepositoryQueryParams.Base params) {
        // These are not supported by query.setParameter(). All parameters are enum, or Integers, therefore it's safe to use replace()
        String result = sql.replace(":goodIngredientsRelation", params.getGoodIngredientsRelation().getStringRep());
        result = result.replace(":unknownIngredientsRelation", params.getUnknownIngredientRelation().getStringRep());
        result = result.replace(":unknownIngredients", params.getUnknownIngredients().toString());
        return result;
    }

    private void setBaseParams(Query<Recipe> query, RecipeRepositoryQueryParams.Base params) {
        setIncludedIngredients(query, params);
        setExcludedIngredient(query, params);
        setNumberOfIngredients(query, params);
        setOrderBy(query, params);
        setSourcePages(query, params);
        setNameLike(query, params);

        // Check, that excluded and included are mutually exclusive
        if (params.getExcludedIngredients() != null && params.getIncludedIngredients() != null) {
            for (Long id : params.getExcludedIngredients()) {
                if (params.getIncludedIngredients().contains(id)) {
                    throw new IllegalArgumentException(("Included and excluded ingredients are not mutually exclusive!"));
                }
            }
        }
    }

    private void setNumberOfIngredients(Query<Recipe> query, RecipeRepositoryQueryParams.Base params) {
        // Number of ingredients
        if (params.getMaximumNumberOfIngredients() > 0) {
            query.where().le("numofings", params.getMaximumNumberOfIngredients());
        }

        if (params.getMinimumNumberOfIngredients() > 0) {
            query.where().ge("numofings", params.getMinimumNumberOfIngredients());
        }
    }

    private void setIncludedIngredients(Query<Recipe> query, RecipeRepositoryQueryParams.Base params) {
        // Merge tags
        if (params.getIncludedIngredientTags() != null) {
            mergeIngredientIds(params.getIncludedIngredients(), getIngredientIdsForTags(params.getIncludedIngredientTags()));
        }

        query.setParameter("includedIngredients", params.getIncludedIngredients());
    }

    private void setExcludedIngredient(Query<Recipe> query, RecipeRepositoryQueryParams.Base params) {
        if (params.getExcludedIngredients() != null && params.getExcludedIngredients().size() > 0) {
            if (params.getExcludedIngredientTags() != null) {
                mergeIngredientIds(params.getExcludedIngredients(), getIngredientIdsForTags(params.getExcludedIngredientTags()));
            }

            query.setParameter("excludedIngredients", params.getExcludedIngredients());
        }
    }

    private void setOrderBy(Query<Recipe> query, RecipeRepositoryQueryParams.Base params) {
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

    private void setSourcePages(Query<Recipe> query, RecipeRepositoryQueryParams.Base params) {
        if (params.getSourcePageIds() != null && params.getSourcePageIds().size() > 0) {
            query.where().in("sourcePage.id", params.getSourcePageIds());
        }
    }

    private void setNameLike(Query<Recipe> query, RecipeRepositoryQueryParams.Base params) {
        // Name like
        if (params.getNameLike() != null) {
            query.where().ilike("name", "%" + params.getNameLike() + "%");
        }
    }

    private void setPaging(Query<Recipe> query, RecipeRepositoryQueryParams.Base params) {
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
}
