package lombokized.repositories;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

public class RecipeRepositoryQuery {
    public enum Relation {
        LT("<"),
        LE("<="),
        EQ("="),
        GT(">"),
        GE(">=");

        Relation(String stringRep) {
            this.stringRep = stringRep;
        }

        private String stringRep;

        public String getStringRep() {
            return stringRep;
        }

        public static Relation fromString(String value) {
            return Relation.valueOf(value.toUpperCase());
        }
    }

    @Getter
    @ToString
    public static class CommonParams {
        private Integer minimumNumberOfIngredients;
        private Integer maximumNumberOfIngredients;
        private List<Long> excludedIngredients;
        private List<Long> excludedIngredientTags;
        private String orderBy;
        private String orderBySort;
        private Integer offset;
        private Integer limit;
        private String nameLike;
        private List<Long> sourcePageIds;

        @Builder
        public CommonParams(Integer minimumNumberOfIngredients, Integer maximumNumberOfIngredients, List<Long> excludedIngredients, List<Long> excludedIngredientTags, String orderBy, String orderBySort, Integer offset, Integer limit, String nameLike, List<Long> sourcePageIds) {
            this.minimumNumberOfIngredients = minimumNumberOfIngredients;
            this.maximumNumberOfIngredients = maximumNumberOfIngredients;

            // Make sure this has non-null value as it is important when merging tags.
            this.excludedIngredients = excludedIngredients == null ? new ArrayList<>() : excludedIngredients;
            this.excludedIngredientTags = excludedIngredientTags;
            this.orderBy = orderBy;
            this.orderBySort = orderBySort;
            this.offset = offset;
            this.limit = limit;
            this.nameLike = nameLike;
            this.sourcePageIds = sourcePageIds;
        }
    }

    @Getter
    public static class WithIncludedIngredientsParams {
        private List<Long> includedIngredients;
        private List<Long> includedIngredientTags;

        @Builder
        public WithIncludedIngredientsParams(List<Long> includedIngredients, List<Long> includedIngredientTags) {
            // Make sure this has non-null value as it is important when merging tags.
            this.includedIngredients = includedIngredients == null ? new ArrayList<>() : includedIngredients;
            this.includedIngredientTags = includedIngredientTags;
        }
    }

    @Getter
    @ToString
    public static class WithGoodIngredientsNumberParams {
        private Integer goodIngredients;
        private Relation goodIngredientsRelation;
        private Integer unknownIngredients;
        private Relation unknownIngredientsRelation;
        private CommonParams commonParams;
        private WithIncludedIngredientsParams recipesWithIncludedIngredientsParams;

        @Builder
        public WithGoodIngredientsNumberParams(Integer goodIngredients, Relation goodIngredientsRelation, Integer unknownIngredients, Relation unknownIngredientsRelation, CommonParams commonParams, WithIncludedIngredientsParams recipesWithIncludedIngredientsParams) {
            this.goodIngredients = goodIngredients;
            this.goodIngredientsRelation = goodIngredientsRelation;
            this.unknownIngredients = unknownIngredients;
            this.unknownIngredientsRelation = unknownIngredientsRelation;
            this.commonParams = commonParams;
            this.recipesWithIncludedIngredientsParams = recipesWithIncludedIngredientsParams;
        }
    }

    @Getter
    @ToString
    public static class WithGoodIngredientsRatioParams {
        private Float goodIngredientsRatio;
        private CommonParams commonParams;
        private WithIncludedIngredientsParams recipesWithIncludedIngredientsParams;

        @Builder
        public WithGoodIngredientsRatioParams(Float goodIngredientsRatio, CommonParams commonParams, WithIncludedIngredientsParams recipesWithIncludedIngredientsParams) {
            this.goodIngredientsRatio = goodIngredientsRatio;
            this.commonParams = commonParams;
            this.recipesWithIncludedIngredientsParams = recipesWithIncludedIngredientsParams;
        }
    }
}
