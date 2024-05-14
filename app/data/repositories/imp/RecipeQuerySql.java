package data.repositories.imp;

// Contains code for generating raw SQL strings for queries.
class RecipeQuerySql {
    public static String create(Configuration config) {
        String otherFields = createOtherFieldsSelections(config);
        String includedIngredientsJoin = createIncludedIngredientsJoin(config);
        String excludedJoin = createExcludedJoin(config);
        String useFavoritesJoin = createUseFavoritesJoin(config);
        String userRecipeBooksJoin = createUseRecipeBooksJoin(config);
        String where = createWhereClause(config);
        String includedIngredientsCondition = createIncludedIngredientsCondition(config);
        String excludedCondition = createExcludedCondition(config);
        String groupByCondition = createGroupByCondition(config);
        String havingCondition = createHavingCondition(config);
        String useFavoritesCondition = createUseFavoritesCondition(config);
        String useRecipeBooksCondition = createRecipeBooksCondition(config);
        String useNameLikeCondition = createNameLikeCondition(config);

        return "" +
                "" +
                "SELECT " +
                "  recipe.id " +
                otherFields +
                "FROM " +
                "  recipe " +
                includedIngredientsJoin +
                excludedJoin + " " +
                useFavoritesJoin + " " +
                userRecipeBooksJoin + " " +
                where +
                includedIngredientsCondition +
                excludedCondition + " " +
                useFavoritesCondition + " " +
                useRecipeBooksCondition + " " +
                useNameLikeCondition + " " +
                groupByCondition + " " +
                havingCondition;
    }

    static class Configuration {
        public boolean selectOtherFields;
        public boolean useExclude;
        public QueryType queryType;
        public boolean useAdditionalIngrs;
        public boolean useFavoritesOnly;
        public boolean useRecipeBooks;
        public boolean useNameLike;

        public Configuration(boolean selectOtherFields, boolean useExclude, QueryType queryType) {
            this.selectOtherFields = selectOtherFields;
            this.useExclude = useExclude;
            this.queryType = queryType;
        }
    }

    public enum QueryType {
        RATIO, NUMBER, NONE
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
                    "ON recipe.id = req.id ";
        }

        return excludedJoin;
    }

    private static String createExcludedCondition(Configuration config) {
        if (!config.useExclude) {
            return "";
        }

        String condition = " (badIngs IS NULL) ";
        if (config.queryType == QueryType.NUMBER || config.queryType == QueryType.RATIO) {
            condition = " AND " + condition;
        }

        return condition;
    }

    private static String createGroupByCondition(Configuration config) {
        if (config.useRecipeBooks || !QueryType.NONE.equals(config.queryType)) {
            return "GROUP BY recipe.id ";
        }

        return "";
    }

    private static String createHavingCondition(Configuration config) {
        String prefix = "" +
                "HAVING ";

        if (QueryType.NUMBER.equals(config.queryType)) {
            return prefix + havingNumberCondition();
        } else if (QueryType.RATIO.equals(config.queryType)) {
            return prefix + havingRatioCondition();
        } else if (QueryType.NONE.equals(config.queryType)) {
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
        if (QueryType.RATIO.equals(config.queryType) ||
                QueryType.NUMBER.equals(config.queryType)) {
            return "" +
                    "  recipe_ingredient.ingredient_id IN (:includedIngredients) ";
        } else if (QueryType.NONE.equals(config.queryType)) {
            return "";
        }

        throw new IllegalArgumentException("Query type is invalid!");
    }

    private static String createIncludedIngredientsJoin(Configuration config) {
        String join = "";
        if (QueryType.RATIO.equals(config.queryType) || QueryType.NUMBER.equals(config.queryType)) {
            join = " JOIN recipe_ingredient ON recipe.id = recipe_ingredient.recipe_id ";
        }

        if ((QueryType.RATIO.equals(config.queryType) || QueryType.NUMBER.equals(config.queryType))
                && config.useAdditionalIngrs) {
            join = join + " JOIN (" + createAdditionalIngredientsQuery() + ") AS additionals " +
                    " ON recipe.id = additionals.recipe_id ";
        }

        return join;
    }

    private static String createWhereClause(Configuration config) {
        String whereClause = "";

        if (QueryType.RATIO.equals(config.queryType) ||
                QueryType.NUMBER.equals(config.queryType) ||
                config.useExclude ||
                config.useFavoritesOnly ||
                config.useRecipeBooks ||
                config.useNameLike) {
            whereClause = "WHERE ";
        }

        return whereClause;
    }

    private static String createOtherFieldsSelections(Configuration config) {
        String otherFieldSelections = "";

        if (config.selectOtherFields) {
            otherFieldSelections = ", recipe.name, recipe.url, recipe.date_added, recipe.numofings, recipe.time, recipe.source_page_id ";
        }

        return otherFieldSelections;
    }

    private static String createAdditionalIngredientsQuery() {
        return " SELECT recipe.id AS recipe_id " +
                " FROM recipe " +
                " JOIN recipe_ingredient ON recipe.id = recipe_ingredient.recipe_id " +
                " WHERE recipe_ingredient.ingredient_id IN (:additionalIngredientIds) " +
                " GROUP BY recipe.id " +
                " HAVING " +
                "   COUNT(recipe_ingredient.ingredient_id) :goodAdditionalIngredientRelation :goodAdditionalIngredientIds ";
    }

    private static String createUseFavoritesJoin(Configuration config) {
        if (!config.useFavoritesOnly) {
            return "";
        }

        return " JOIN favorite_recipe ON recipe.id = favorite_recipe.recipe_id ";
    }

    private static String createUseRecipeBooksJoin(Configuration config) {
        if (!config.useRecipeBooks) {
            return "";
        }

        return " JOIN recipe_book_recipe ON recipe.id = recipe_book_recipe.recipe_id ";
    }

    private static String createUseFavoritesCondition(Configuration config) {
        if (!config.useFavoritesOnly) {
            return "";
        }

        String condition = " favorite_recipe.user_id = :userId ";

        if (config.queryType == QueryType.NUMBER ||
                config.queryType == QueryType.RATIO ||
                config.useExclude) {
            condition = " AND " + condition;
        }

        return condition;
    }

    private static String createRecipeBooksCondition(Configuration config) {
        if (!config.useRecipeBooks) {
            return "";
        }

        String condition = " recipe_book_recipe.recipe_book_id IN (:usedRecipeBooks) ";

        if (config.queryType == QueryType.NUMBER ||
                config.queryType == QueryType.RATIO ||
                config.useExclude ||
                config.useFavoritesOnly) {
            condition = " AND " + condition;
        }

        return condition;
    }

    private static String createNameLikeCondition(Configuration config) {
        if(!config.useNameLike) {
            return "";
        }

        String condition = " (recipe.name <-> :nameLike < 0.55 OR recipe.name ILIKE :nameLikeForIlike) ";

        if (config.queryType == QueryType.NUMBER ||
                config.queryType == QueryType.RATIO ||
                config.useExclude ||
                config.useFavoritesOnly ||
                config.useRecipeBooks) {
            condition = " AND " + condition;
        }

        return condition;
    }
}
