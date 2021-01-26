package services;

import queryparams.RecipesQueryParams;

import static lombokized.repositories.RecipeRepositoryParams.*;

class RecipesQueryParamsMapping {
    public static QueryTypeNumber toQueryTypeNumber(RecipesQueryParams.Params queryParams) {
        Common commonParams = toCommon(queryParams);
        return toQueryTypeNumber(queryParams, commonParams);
    }

    public static QueryTypeNumber toQueryTypeNumber(RecipesQueryParams.Params queryParams, Long userId){
        Common commonParams = toCommon(queryParams, userId);
        return toQueryTypeNumber(queryParams, commonParams);
    }

    public static QueryTypeRatio toQueryTypeRatio(RecipesQueryParams.Params queryParams) {
        Common commonParams = toCommon(queryParams);
        return toQueryTypeRatio(queryParams, commonParams);
    }

    public static QueryTypeRatio toQueryTypeRatio(RecipesQueryParams.Params queryParams, Long userId){
        Common commonParams = toCommon(queryParams, userId);
        return toQueryTypeRatio(queryParams, commonParams);
    }

    public static Common toCommon(RecipesQueryParams.Params queryParams, Long userId){
        Common params = toCommon(queryParams);
        Common.Builder builder = params.toBuilder();
        builder.userId(userId);
        builder.useFavoritesOnly(queryParams.useFavoritesOnly);
        builder.usedRecipeBooks(queryParams.recipeBooks);
        return builder.build();
    }

    public static Common toCommon(RecipesQueryParams.Params queryParams) {
        Common.Builder builder = Common.builder();

        if (queryParams.exIngs != null && queryParams.exIngs.size() > 0) {
            builder.excludedIngredients(queryParams.exIngs);
        }

        if (queryParams.exIngTags != null && queryParams.exIngTags.size() > 0) {
            builder.excludedIngredientTags(queryParams.exIngTags);
        }

        builder.limit(queryParams.limit);
        builder.offset(queryParams.offset);
        builder.maximumNumberOfIngredients(queryParams.maxIngs);
        builder.minimumNumberOfIngredients(queryParams.minIngs);
        builder.nameLike(queryParams.nameLike);
        builder.orderBy(queryParams.orderBy);
        builder.orderBySort(queryParams.orderBySort);
        builder.sourcePageIds(queryParams.sourcePages);
        builder.times(queryParams.times);

        return builder.build();
    }

    private static IncludedIngredients toIncludedIngredients(RecipesQueryParams.Params queryParams) {
        IncludedIngredients.Builder builder = IncludedIngredients.builder();

        if (queryParams.inIngs != null && queryParams.inIngs.size() > 0) {
            builder.includedIngredients(queryParams.inIngs);
        }

        if (queryParams.inIngTags != null && queryParams.inIngTags.size() > 0) {
            builder.includedIngredientTags(queryParams.inIngTags);
        }

        return builder.build();
    }

    private static AdditionalIngredients toAdditionalIngredients(RecipesQueryParams.Params queryParams) {
        if (queryParams.goodAdditionalIngs == null) {
            return null;
        }

        AdditionalIngredients.Builder builder = AdditionalIngredients.builder();

        builder.goodAdditionalIngredients(queryParams.goodAdditionalIngs);
        builder.goodAdditionalIngredientsRelation(Relation.fromString(queryParams.goodAdditionalIngsRel));
        if (queryParams.addIngs != null) {
            builder.additionalIngredients(queryParams.addIngs);
        }
        builder.additionalIngredientTags(queryParams.addIngTags);

        return builder.build();
    }

    private static QueryTypeRatio toQueryTypeRatio(RecipesQueryParams.Params queryParams, Common commonParams){
        QueryTypeRatio.Builder builder = QueryTypeRatio.builder();

        builder.common(commonParams);
        builder.goodIngredientsRatio(queryParams.goodIngsRatio);
        builder.includedIngredients(toIncludedIngredients(queryParams));
        builder.additionalIngredients(toAdditionalIngredients(queryParams));

        return builder.build();
    }

    private static QueryTypeNumber toQueryTypeNumber(RecipesQueryParams.Params queryParams, Common commonParams) {
        QueryTypeNumber.Builder builder = QueryTypeNumber.builder();

        builder.common(commonParams);
        builder.includedIngredients(toIncludedIngredients(queryParams));
        builder.goodIngredients(queryParams.goodIngs);
        builder.goodIngredientsRelation(Relation.fromString(queryParams.goodIngsRel));
        builder.unknownIngredients(queryParams.unknownIngs);
        builder.unknownIngredientsRelation(Relation.fromString(queryParams.unknownIngsRel));
        builder.additionalIngredients(toAdditionalIngredients(queryParams));

        return builder.build();
    }
}
