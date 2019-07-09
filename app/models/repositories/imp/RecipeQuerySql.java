package models.repositories.imp;

// Contains code for generating raw SQL strings for queries.
class RecipeQuerySql {
    public static String create(Configuration config) {
        String otherFields = ", recipe.name, recipe.url, recipe.date_added, recipe.numofings, recipe.time, recipe.source_page_id ";
        otherFields = config.selectOtherFields ? otherFields : "";

        String excludedJoin = config.useExclude ? createExcludedJoin() : "";
        String excludedCondition = config.useExclude ? createExcludedCondition(config.putExcludedAnd) : "";

        String havingConditon = createHavingCondition(config);
        String includedIngredientsCondition = includedIngredientsCondition(config);
        String includedIngredientsJoin = includedIngredientsJoin(config);
        String where = "";
        if(!"".equals(includedIngredientsCondition) || !"".equals(excludedCondition)){
            where = "WHERE ";
        }

        return "" +
                "" +
                "SELECT " +
                "  recipe.id " +
                otherFields +
                "FROM " +
                "  recipe " +
                includedIngredientsJoin +
                excludedJoin + " " +
                where +
                includedIngredientsCondition +
                excludedCondition + " " +
                havingConditon;
    }

    static class Configuration {
        public boolean selectOtherFields;
        public boolean useExclude;
        public QueryType queryType;
        public boolean putExcludedAnd;

        public Configuration(boolean selectOtherFields, boolean useExclude, QueryType queryType) {
            this.selectOtherFields = selectOtherFields;
            this.useExclude = useExclude;
            this.queryType = queryType;
            this.putExcludedAnd = true;
        }
    }

    public enum QueryType {
        RATIO, NUMBER, ALL
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
                "    ie.ingredient_id in (:excludedIngredients) " +
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

    private static String createHavingCondition(Configuration config) {
        String prefix = "" +
                "GROUP BY " +
                "  recipe.id " +
                "HAVING ";

        if (QueryType.NUMBER.equals(config.queryType)) {
            return prefix + havingNumberCondition();
        } else if (QueryType.RATIO.equals(config.queryType)) {
            return prefix + havingRatioCondition();
        } else if (QueryType.ALL.equals(config.queryType)) {
            return "";
        }

        throw new IllegalArgumentException("Query type is invalid!");
    }

    private static String havingRatioCondition() {
        return " (COUNT(recipe_ingredient.ingredient_id) * 1.0) / (recipe.numofings * 1.0) >= :ratio";
    }

    private static String havingNumberCondition() {
        return "  COUNT(recipe_ingredient.ingredient_id) :goodIngredientsRelation :goodIngredients AND " +
                "  (recipe.numofings - COUNT(recipe_ingredient.ingredient_id)) :unknownIngredientsRelation :unknownIngredients";
    }

    private static String includedIngredientsCondition(Configuration config) {
        if (QueryType.RATIO.equals(config.queryType) || QueryType.NUMBER.equals(config.queryType)) {
            return "" +
                    "  recipe_ingredient.ingredient_id IN (:includedIngredients) ";
        } else if (QueryType.ALL.equals(config.queryType)) {
            return "";
        }

        throw new IllegalArgumentException("Query type is invalid!");
    }

    private static String includedIngredientsJoin(Configuration config) {
        if (QueryType.RATIO.equals(config.queryType) || QueryType.NUMBER.equals(config.queryType)) {
            return "  JOIN recipe_ingredient ON recipe.id = recipe_ingredient.recipe_id ";
        } else if (QueryType.ALL.equals(config.queryType)) {
            return "";
        }

        throw new IllegalArgumentException("Query type is invalid!");
    }
}
