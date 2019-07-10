package controllers.v1;

import models.repositories.RecipeRepositoryQueryParams;

// Maps controller query to repository queries
class RecipeControllerQueryMapping {
    static RecipeRepositoryQueryParams.OfGoodIngredientsNumber toGoodIngredientsNumberQueryParams(RecipesControllerQuery.Params params) {
        RecipeRepositoryQueryParams.Common.CommonBuilder commonBuilder =
                toCommonBuilder(params);

        RecipeRepositoryQueryParams.WithIncludedIngredients.WithIncludedIngredientsBuilder
                withIncludedIngredientsBuilder = toWithIncludedIngredientsBuilder(params);

        RecipeRepositoryQueryParams.OfGoodIngredientsNumber.OfGoodIngredientsNumberBuilder ofGoodIngredientsNumberBuilder =
                RecipeRepositoryQueryParams.OfGoodIngredientsNumber.builder();

        ofGoodIngredientsNumberBuilder.common(commonBuilder.build());
        ofGoodIngredientsNumberBuilder.recipesWithIncludedIngredients(withIncludedIngredientsBuilder.build());
        ofGoodIngredientsNumberBuilder.goodIngredients(params.goodIngs);
        ofGoodIngredientsNumberBuilder.goodIngredientsRelation(RecipeRepositoryQueryParams.Relation.fromString(params.goodIngsRel));
        ofGoodIngredientsNumberBuilder.unknownIngredients(params.unknownIngs);
        ofGoodIngredientsNumberBuilder.unknownIngredientsRelation(RecipeRepositoryQueryParams.Relation.fromString(params.unknownIngsRel));

        return ofGoodIngredientsNumberBuilder.build();
    }

    static RecipeRepositoryQueryParams.OfGoodIngredientsRatio toGoodIngredientsRatioQueryParams(RecipesControllerQuery.Params params) {
        RecipeRepositoryQueryParams.Common.CommonBuilder commonBuilder =
                toCommonBuilder(params);

        RecipeRepositoryQueryParams.WithIncludedIngredients.WithIncludedIngredientsBuilder
                withIncludedIngredientsBuilder = toWithIncludedIngredientsBuilder(params);

        RecipeRepositoryQueryParams.OfGoodIngredientsRatio.OfGoodIngredientsRatioBuilder ofGoodIngredientsRatioBuilder =
                RecipeRepositoryQueryParams.OfGoodIngredientsRatio.builder();

        ofGoodIngredientsRatioBuilder.common(commonBuilder.build());
        ofGoodIngredientsRatioBuilder.goodIngredientsRatio(params.goodIngsRatio);
        ofGoodIngredientsRatioBuilder.recipesWithIncludedIngredients(withIncludedIngredientsBuilder.build());

        return ofGoodIngredientsRatioBuilder.build();
    }

    static RecipeRepositoryQueryParams.Common toCommonQueryParams(RecipesControllerQuery.Params params){
        return toCommonBuilder(params).build();
    }

    // Converts controller query params to repository query params builder.
    private static RecipeRepositoryQueryParams.Common.CommonBuilder toCommonBuilder(RecipesControllerQuery.Params params) {
        RecipeRepositoryQueryParams.Common.CommonBuilder builder = RecipeRepositoryQueryParams.Common.builder();

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

    private static RecipeRepositoryQueryParams.WithIncludedIngredients.WithIncludedIngredientsBuilder
    toWithIncludedIngredientsBuilder(RecipesControllerQuery.Params params) {
        RecipeRepositoryQueryParams.WithIncludedIngredients.WithIncludedIngredientsBuilder
                builder = RecipeRepositoryQueryParams.WithIncludedIngredients.builder();

        if (params.inIngs != null && params.inIngs.size() > 0) {
            builder.includedIngredients(params.inIngs);
        }

        if (params.inIngTags != null && params.inIngTags.size() > 0) {
            builder.includedIngredientTags(params.inIngTags);
        }

        return builder;
    }
}
