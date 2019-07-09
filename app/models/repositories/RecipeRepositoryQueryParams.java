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
    public static class Base {
        private Integer minimumNumberOfIngredients;
        private Integer maximumNumberOfIngredients;
        private List<Long> includedIngredients;
        private List<Long> excludedIngredients;
        private List<Long> includedIngredientTags;
        private List<Long> excludedIngredientTags;
        private String orderBy;
        private String orderBySort;
        private Integer offset;
        private Integer limit;
        private String nameLike;
        private List<Long> sourcePageIds;

        @Builder
        public Base(Integer minimumNumberOfIngredients, Integer maximumNumberOfIngredients, List<Long> includedIngredients, List<Long> excludedIngredients, List<Long> includedIngredientTags, List<Long> excludedIngredientTags, String orderBy, String orderBySort, Integer offset, Integer limit, String nameLike, List<Long> sourcePageIds) {
            this.minimumNumberOfIngredients = minimumNumberOfIngredients;
            this.maximumNumberOfIngredients = maximumNumberOfIngredients;

            // Make sure these two have non-null values as it is important when merging tags.
            this.includedIngredients = includedIngredients == null ? new ArrayList<>() : includedIngredients;
            this.excludedIngredients = excludedIngredients == null ? new ArrayList<>() : excludedIngredients;

            this.includedIngredientTags = includedIngredientTags;
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
    @ToString
    public static class OfGoodIngredientsNumber {
        private Integer goodIngredients;
        private Relation goodIngredientsRelation;
        private Integer unknownIngredients;
        private Relation unknownIngredientsRelation;
        private Base base;

        @Builder
        public OfGoodIngredientsNumber(Integer goodIngredients, Relation goodIngredientsRelation, Integer unknownIngredients, Relation unknownIngredientsRelation, Base base) {
            this.goodIngredients = goodIngredients;
            this.goodIngredientsRelation = goodIngredientsRelation;
            this.unknownIngredients = unknownIngredients;
            this.unknownIngredientsRelation = unknownIngredientsRelation;
            this.base = base;
        }
    }

    @Getter
    @ToString
    public static class OfGoodIngredientsRatio{
        private Float goodIngredientsRatio;
        private Base base;

        @Builder
        public OfGoodIngredientsRatio(Float goodIngredientsRatio, Base base) {
            this.goodIngredientsRatio = goodIngredientsRatio;
            this.base = base;
        }
    }
}
