package models.repositories;

import lombok.Builder;
import lombok.Getter;

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
    public static class Base {
        private Integer minimumNumberOfIngredients;
        private Integer maximumNumberOfIngredients;
        private List<Long> includedIngredients;
        private List<Long> excludedIngredients;
        private List<Long> includedIngredientTags;
        private List<Long> excludedIngredientTags;
        private String orderBy;
        private String orderBySort;
        private Integer unknownIngredients;
        private Relation unknownIngredientsRelation;
        private Relation goodIngredientsRelation;
        private Integer offset;
        private Integer limit;
        private String nameLike;
        private List<Long> sourcePageIds;

        public Base(Integer minimumNumberOfIngredients, Integer maximumNumberOfIngredients, List<Long> includedIngredients, List<Long> excludedIngredients, List<Long> includedIngredientTags, List<Long> excludedIngredientTags, String orderBy, String orderBySort, Integer unknownIngredients, Relation unknownIngredientsRelation, Relation goodIngredientsRelation, Integer offset, Integer limit, String nameLike, List<Long> sourcePageIds) {
            this.minimumNumberOfIngredients = minimumNumberOfIngredients;
            this.maximumNumberOfIngredients = maximumNumberOfIngredients;
            this.includedIngredients = includedIngredients;
            this.excludedIngredients = excludedIngredients;
            this.includedIngredientTags = includedIngredientTags;
            this.excludedIngredientTags = excludedIngredientTags;
            this.orderBy = orderBy;
            this.orderBySort = orderBySort;
            this.unknownIngredients = unknownIngredients;
            this.unknownIngredientsRelation = unknownIngredientsRelation;
            this.goodIngredientsRelation = goodIngredientsRelation;
            this.offset = offset;
            this.limit = limit;
            this.nameLike = nameLike;
            this.sourcePageIds = sourcePageIds;
        }
    }

    @Getter
    public static class OfGoodIngredientsNumber extends Base {
        private Integer goodIngredients;

        @Builder
        public OfGoodIngredientsNumber(Integer minimumNumberOfIngredients, Integer maximumNumberOfIngredients, List<Long> includedIngredients, List<Long> excludedIngredients, List<Long> includedIngredientTags, List<Long> excludedIngredientTags, String orderBy, String orderBySort, Integer unknownIngredients, Relation unknownIngredientRelation, Relation goodIngredientsRelation, Integer offset, Integer limit, String nameLike, List<Long> sourcePageIds, Integer goodIngredients) {
            super(minimumNumberOfIngredients, maximumNumberOfIngredients, includedIngredients, excludedIngredients, includedIngredientTags, excludedIngredientTags, orderBy, orderBySort, unknownIngredients, unknownIngredientRelation, goodIngredientsRelation, offset, limit, nameLike, sourcePageIds);
            this.goodIngredients = goodIngredients;
        }
    }

    @Getter
    public static class OfGoodIngredientsRatio extends Base {
        private Float goodIngredientsRatio;

        @Builder
        public OfGoodIngredientsRatio(Integer minimumNumberOfIngredients, Integer maximumNumberOfIngredients, List<Long> includedIngredients, List<Long> excludedIngredients, List<Long> includedIngredientTags, List<Long> excludedIngredientTags, String orderBy, String orderBySort, Integer unknownIngredients, Relation unknownIngredientRelation, Relation goodIngredientsRelation, Integer offset, Integer limit, String nameLike, List<Long> sourcePageIds, Float goodIngredientsRatio) {
            super(minimumNumberOfIngredients, maximumNumberOfIngredients, includedIngredients, excludedIngredients, includedIngredientTags, excludedIngredientTags, orderBy, orderBySort, unknownIngredients, unknownIngredientRelation, goodIngredientsRelation, offset, limit, nameLike, sourcePageIds);
            this.goodIngredientsRatio = goodIngredientsRatio;
        }
    }
}
