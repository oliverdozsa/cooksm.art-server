package models.repositories.imp;

// Contains code for generating raw SQL strings for queries.
class RecipeQuerySql {
    public static String createRecipesByGoodIngredientsNumberSql(boolean selectOtherFields, boolean useExclude) {
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
                "  recipe_ingredient.ingredient_id IN (:includedIngredients) " +
                excludedCondition + " " +
                "GROUP BY " +
                "  recipe.id " +
                "HAVING " +
                "  COUNT(recipe_ingredient.ingredient_id) :goodIngredientsRelation :goodIngredients AND " +
                "  (recipe.numofings - COUNT(recipe_ingredient.ingredient_id)) :unknownIngredientsRelation :unknownIngredients";
    }

    public static String createExcludedJoin() {
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
                "    ie.ingredient_id in (:excludedIngredients) " +
                "  GROUP BY " +
                "    re.id " +
                "  HAVING " +
                "    COUNT(ie.ingredient_id) > 0) " +
                "  AS req " +
                "ON recipe.id = req.id";
    }

    public static String createExcludedCondition(boolean putAnd) {
        String result = " (badIngs IS NULL) ";
        if (putAnd) {
            result = " AND " + result;
        }

        return result;
    }
}
