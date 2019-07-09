package models.repositories.imp;

// Contains code for generating raw SQL strings for queries.
class RecipeQuerySql {
    public static String create(Configuration config) {
        String otherFields = createOtherFieldsSelections(config);
        String includedIngredientsJoin = createIncludedIngredientsJoin(config);
        String excludedJoin = createExcludedJoin(config);
        String where = createWhereClause(config);
        String includedIngredientsCondition = createIncludedIngredientsCondition(config);
        String excludedCondition = createExcludedCondition(config);
        String havingConditon = createHavingCondition(config);

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
            this(selectOtherFields, useExclude, queryType, true);
        }

        public Configuration(boolean selectOtherFields, boolean useExclude, QueryType queryType, boolean putExcludedAnd) {
            this.selectOtherFields = selectOtherFields;
            this.useExclude = useExclude;
            this.queryType = queryType;
            this.putExcludedAnd = putExcludedAnd;
        }
    }

    public enum QueryType {
        RATIO, NUMBER, ALL
    }

    private static String createExcludedJoin(Configuration config) {
        String excludedJoin = "";
        if (config.useExclude) {
            excludedJoin = "" +
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

        return excludedJoin;
    }

    private static String createExcludedCondition(Configuration config) {
        String excludedCondition = "";
        if (config.useExclude) {
            excludedCondition = " (badIngs IS NULL) ";
            if (config.putExcludedAnd) {
                excludedCondition = " AND " + excludedCondition;
            }
        }

        return excludedCondition;
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

    private static String createIncludedIngredientsCondition(Configuration config) {
        if (QueryType.RATIO.equals(config.queryType) || QueryType.NUMBER.equals(config.queryType)) {
            return "" +
                    "  recipe_ingredient.ingredient_id IN (:includedIngredients) ";
        } else if (QueryType.ALL.equals(config.queryType)) {
            return "";
        }

        throw new IllegalArgumentException("Query type is invalid!");
    }

    private static String createIncludedIngredientsJoin(Configuration config) {
        if (QueryType.RATIO.equals(config.queryType) || QueryType.NUMBER.equals(config.queryType)) {
            return "  JOIN recipe_ingredient ON recipe.id = recipe_ingredient.recipe_id ";
        } else if (QueryType.ALL.equals(config.queryType)) {
            return "";
        }

        throw new IllegalArgumentException("Query type is invalid!");
    }

    private static String createWhereClause(Configuration config) {
        String whereClause = "";

        if (QueryType.RATIO.equals(config.queryType) ||
                QueryType.NUMBER.equals(config.queryType) ||
                config.useExclude) {
            whereClause = "WHERE ";
        }

        return whereClause;
    }

    private static String createOtherFieldsSelections(Configuration config) {
        String otherFieldSelections = "";

        if(config.selectOtherFields){
            otherFieldSelections = ", recipe.name, recipe.url, recipe.date_added, recipe.numofings, recipe.time, recipe.source_page_id ";
        }

        return  otherFieldSelections;
    }
}
