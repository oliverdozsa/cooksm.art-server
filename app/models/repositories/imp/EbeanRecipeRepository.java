package models.repositories.imp;

import io.ebean.*;
import models.DatabaseExecutionContext;
import models.entities.Recipe;
import models.repositories.Page;
import models.repositories.RecipeQueryParameters;
import models.repositories.RecipeRepository;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class EbeanRecipeRepository implements RecipeRepository {
    private EbeanServer ebean;
    private DatabaseExecutionContext executionContext;

    @Inject
    public EbeanRecipeRepository(EbeanConfig dbConfig, DatabaseExecutionContext executionContext) {
        this.ebean = Ebean.getServer(dbConfig.defaultServer());
        this.executionContext = executionContext;
    }

    @Override
    public CompletionStage<Page<Recipe>> pageOfComposedOfIngredients(RecipeQueryParameters.ByGoodIngredientsNumber params) {
        String sqlString = createRecipesByGoodIngredientsNumberSql(
                true,
                params.getExcludedIngredients() != null && params.getExcludedIngredients().size() > 0
        );

        RawSql rawSql = setColumnMappings(RawSqlBuilder.parse(sqlString)).create();

        Query<Recipe> query = ebean.createQuery(Recipe.class).setRawSql(rawSql);
        query.setParameter("includedIngredients", params.getIncludedIngredients());
        query.setParameter("goodIngredientsRelation", params.getGoodIngredientsRelation().getStringRep());
        query.setParameter("goodIngredients", params.getGoodIngredients());
        query.setParameter("unknownIngredientsRelation", params.getUnknownIngredientRelation().getStringRep());
        query.setParameter("unknownIngredients", params.getUnknownIngredients());

        if (params.getExcludedIngredients() != null && params.getExcludedIngredients().size() > 0) {
            query.setParameter("excludedIngredients", params.getExcludedIngredients());
        }

        return null;
    }

    @Override
    public CompletionStage<Page<Recipe>> pageOfComposedOfIngredients(RecipeQueryParameters.ByGoodIngredientsRatio params) {
        return null;
    }

    private String createRecipesByGoodIngredientsNumberSql(boolean selectOtherFields, boolean useExclude) {
        String otherFields = ", recipe.name, recipe.url, recipe.date_added, recipe.numofings, recipe.time, recipe.source_page_id ";
        otherFields = selectOtherFields ? otherFields : "";

        String excludedJoin = useExclude ? createExcludedJoin() : "";
        String excludedCondition = useExclude ? createExcludedCondition(true) : "";

        return "" +
                "" +
                "SELECT " +
                "  recipe.id " +
                otherFields +
                "FROM " +
                "  recipe " +
                "  JOIN recipe_ingredient ON recipe.id = recipe_ingredient.recipe_id " +
                excludedJoin + " " +
                "WHERE " +
                "  recipe_ingredient.ingredient_id IN :includedIngredients " +
                excludedCondition + " " +
                "GROUP BY " +
                "  recipe.id " +
                "HAVING " +
                "  COUNT(recipe_ingredient.ingredient_id) :goodIngredientsRelation :goodIngredients AND " +
                "  (recipe.numofings - COUNT(recipe_ingredient.ingredient_id)) :unknownIngredientsRelation :unknownIngredients";
    }

    private static String createExcludedJoin() {
        return "" +
                "LEFT JOIN " +
                "  (SELECT " +
                "    re.id, " +
                "    COUNT(ie.ingredient_id) AS badIngs " +
                "  FROM " +
                "    recipe re " +
                "  JOIN " +
                "    recipe_ingredient ie on ie.recipe_id = re.id " +
                "  WHERE " +
                "    ie.ingredient_id in :excludedIngredients " +
                "  GROUP BY " +
                "    re.id " +
                "  HAVING " +
                "    COUNT(ie.ingredient_id) > 0) " +
                "  AS req " +
                "ON recipe.id = req.id";
    }

    private static String createExcludedCondition(boolean putAnd) {
        String result = " (badIngs IS NULL) ";
        if (putAnd) {
            result = " AND " + result;
        }

        return result;
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

    private void setBaseParams(Query<Recipe> query, RecipeQueryParameters.Base params) {
        // TODO
    }
}
