package controllers.v1;

import models.repositories.RecipeRepositoryQuery;

// Maps controller query to repository queries
class RecipeControllerQueryMapping {
    static RecipeRepositoryQuery.WithGoodIngredientsNumberParams toGoodIngredientsNumberParams(RecipesControllerQuery.Params params) {
        RecipeRepositoryQuery.CommonParams.CommonParamsBuilder commonBuilder =
                toCommonBuilder(params);

        RecipeRepositoryQuery.WithIncludedIngredientsParams.WithIncludedIngredientsParamsBuilder
                withIncludedIngredientsBuilder = toWithIncludedIngredientsBuilder(params);

        RecipeRepositoryQuery.WithGoodIngredientsNumberParams.WithGoodIngredientsNumberParamsBuilder withGoodIngredientsNumberParamsBuilder =
                RecipeRepositoryQuery.WithGoodIngredientsNumberParams.builder();

        withGoodIngredientsNumberParamsBuilder.commonParams(commonBuilder.build());
        withGoodIngredientsNumberParamsBuilder.recipesWithIncludedIngredientsParams(withIncludedIngredientsBuilder.build());
        withGoodIngredientsNumberParamsBuilder.goodIngredients(params.goodIngs);
        withGoodIngredientsNumberParamsBuilder.goodIngredientsRelation(RecipeRepositoryQuery.Relation.fromString(params.goodIngsRel));
        withGoodIngredientsNumberParamsBuilder.unknownIngredients(params.unknownIngs);
        withGoodIngredientsNumberParamsBuilder.unknownIngredientsRelation(RecipeRepositoryQuery.Relation.fromString(params.unknownIngsRel));

        return withGoodIngredientsNumberParamsBuilder.build();
    }

    static RecipeRepositoryQuery.WithGoodIngredientsRatioParams toGoodIngredientsRatioParams(RecipesControllerQuery.Params params) {
        RecipeRepositoryQuery.CommonParams.CommonParamsBuilder commonParamsBuilder =
                toCommonBuilder(params);

        RecipeRepositoryQuery.WithIncludedIngredientsParams.WithIncludedIngredientsParamsBuilder
                withIncludedIngredientsBuilder = toWithIncludedIngredientsBuilder(params);

        RecipeRepositoryQuery.WithGoodIngredientsRatioParams.WithGoodIngredientsRatioParamsBuilder withGoodIngredientsRatioParamsBuilder =
                RecipeRepositoryQuery.WithGoodIngredientsRatioParams.builder();

        withGoodIngredientsRatioParamsBuilder.commonParams(commonParamsBuilder.build());
        withGoodIngredientsRatioParamsBuilder.goodIngredientsRatio(params.goodIngsRatio);
        withGoodIngredientsRatioParamsBuilder.recipesWithIncludedIngredientsParams(withIncludedIngredientsBuilder.build());

        return withGoodIngredientsRatioParamsBuilder.build();
    }

    static RecipeRepositoryQuery.CommonParams toCommonParams(RecipesControllerQuery.Params params){
        return toCommonBuilder(params).build();
    }

    // Converts controller query params to repository query params builder.
    private static RecipeRepositoryQuery.CommonParams.CommonParamsBuilder toCommonBuilder(RecipesControllerQuery.Params params) {
        RecipeRepositoryQuery.CommonParams.CommonParamsBuilder builder = RecipeRepositoryQuery.CommonParams.builder();

        if (params.exIngs != null && params.exIngs.size() > 0) {
            builder.excludedIngredients(params.exIngs);
        }

        if (params.exIngTags != null && params.exIngTags.size() > 0) {
            builder.excludedIngredientTags(params.exIngTags);
        }

        builder.limit(params.limit);
        builder.offset(params.offset);
        builder.maximumNumberOfIngredients(params.maxIngs);
        builder.minimumNumberOfIngredients(params.minIngs);
        builder.nameLike(params.nameLike);
        builder.orderBy(params.orderBy);
        builder.orderBySort(params.orderBySort);
        builder.sourcePageIds(params.sourcePages);

        return builder;
    }

    private static RecipeRepositoryQuery.WithIncludedIngredientsParams.WithIncludedIngredientsParamsBuilder
    toWithIncludedIngredientsBuilder(RecipesControllerQuery.Params params) {
        RecipeRepositoryQuery.WithIncludedIngredientsParams.WithIncludedIngredientsParamsBuilder
                builder = RecipeRepositoryQuery.WithIncludedIngredientsParams.builder();

        if (params.inIngs != null && params.inIngs.size() > 0) {
            builder.includedIngredients(params.inIngs);
        }

        if (params.inIngTags != null && params.inIngTags.size() > 0) {
            builder.includedIngredientTags(params.inIngTags);
        }

        return builder;
    }
}
