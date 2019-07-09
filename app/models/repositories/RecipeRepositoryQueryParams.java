package models.repositories;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

public class RecipeRepositoryQueryParams {
    public enum Relation {
        LT("<"),
        LE("<="),
        EQ("="),
        GT(">"),
        GE(">=");

        /**
         * Init with string rep.
         *
         * @param stringRep The string representation.
         */
        Relation(String stringRep) {
            this.stringRep = stringRep;
        }

        /**
         * The string representation.
         */
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
    public static class Common {
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
        public Common(Integer minimumNumberOfIngredients, Integer maximumNumberOfIngredients, List<Long> excludedIngredients, List<Long> excludedIngredientTags, String orderBy, String orderBySort, Integer offset, Integer limit, String nameLike, List<Long> sourcePageIds) {
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
    public static class WithIncludedIngredients {
        private List<Long> includedIngredients;
        private List<Long> includedIngredientTags;

        @Builder
        public WithIncludedIngredients(List<Long> includedIngredients, List<Long> includedIngredientTags) {
            // Make sure this has non-null value as it is important when merging tags.
            this.includedIngredients = includedIngredients == null ? new ArrayList<>() : includedIngredients;
            this.includedIngredientTags = includedIngredientTags;
        }
    }

    @Getter
    @ToString
    public static class OfGoodIngredientsNumber {
        private Integer goodIngredients;
        private Relation goodIngredientsRelation;
        private Integer unknownIngredients;
        private Relation unknownIngredientsRelation;
        private Common common;
        private WithIncludedIngredients recipesWithIncludedIngredients;

        @Builder
        public OfGoodIngredientsNumber(Integer goodIngredients, Relation goodIngredientsRelation, Integer unknownIngredients, Relation unknownIngredientsRelation, Common common, WithIncludedIngredients recipesWithIncludedIngredients) {
            this.goodIngredients = goodIngredients;
            this.goodIngredientsRelation = goodIngredientsRelation;
            this.unknownIngredients = unknownIngredients;
            this.unknownIngredientsRelation = unknownIngredientsRelation;
            this.common = common;
            this.recipesWithIncludedIngredients = recipesWithIncludedIngredients;
        }
    }

    @Getter
    @ToString
    public static class OfGoodIngredientsRatio {
        private Float goodIngredientsRatio;
        private Common common;
        private WithIncludedIngredients recipesWithIncludedIngredients;

        @Builder
        public OfGoodIngredientsRatio(Float goodIngredientsRatio, Common common, WithIncludedIngredients recipesWithIncludedIngredients) {
            this.goodIngredientsRatio = goodIngredientsRatio;
            this.common = common;
            this.recipesWithIncludedIngredients = recipesWithIncludedIngredients;
        }
    }
}
