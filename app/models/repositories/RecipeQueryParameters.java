package models.repositories;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class RecipeQueryParameters {
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
        private String orderBy;
        private String orderBySort;
        private Integer unknownIngredients;
        private Relation unknownIngredientRelation;
        private Relation goodIngredientsRelation;
        private Integer offset;
        private Integer limit;

        public Base(Integer minimumNumberOfIngredients, Integer maximumNumberOfIngredients, List<Long> includedIngredients, List<Long> excludedIngredients, String orderBy, String orderBySort, Integer unknownIngredients, Relation unknownIngredientRelation, Relation goodIngredientsRelation, Integer offset, Integer limit) {
            this.minimumNumberOfIngredients = minimumNumberOfIngredients;
            this.maximumNumberOfIngredients = maximumNumberOfIngredients;
            this.includedIngredients = includedIngredients;
            this.excludedIngredients = excludedIngredients;
            this.orderBy = orderBy;
            this.orderBySort = orderBySort;
            this.unknownIngredients = unknownIngredients;
            this.unknownIngredientRelation = unknownIngredientRelation;
            this.goodIngredientsRelation = goodIngredientsRelation;
            this.offset = offset == null ? 0 : offset;
            this.limit = limit == null ? 25 : limit;
        }
    }

    @Getter
    public static class ByGoodIngredientsNumber extends Base {
        private Integer goodIngredients;

        @Builder
        public ByGoodIngredientsNumber(Integer minimumNumberOfIngredients, Integer maximumNumberOfIngredients, List<Long> includedIngredients, List<Long> excludedIngredients, String orderBy, String orderBySort, Integer unknownIngredients, Relation unknownIngredientRelation, Relation goodIngredientsRelation, Integer offset, Integer limit, Integer goodIngredients) {
            super(minimumNumberOfIngredients, maximumNumberOfIngredients, includedIngredients, excludedIngredients, orderBy, orderBySort, unknownIngredients, unknownIngredientRelation, goodIngredientsRelation, offset, limit);
            this.goodIngredients = goodIngredients;
        }
    }

    @Getter
    public static class ByGoodIngredientsRatio extends Base {
        private Float goodIngredientsRatio;

        @Builder
        public ByGoodIngredientsRatio(Integer minimumNumberOfIngredients, Integer maximumNumberOfIngredients, List<Long> includedIngredients, List<Long> excludedIngredients, String orderBy, String orderBySort, Integer unknownIngredients, Relation unknownIngredientRelation, Relation goodIngredientsRelation, Integer offset, Integer limit, Float goodIngredientsRatio) {
            super(minimumNumberOfIngredients, maximumNumberOfIngredients, includedIngredients, excludedIngredients, orderBy, orderBySort, unknownIngredients, unknownIngredientRelation, goodIngredientsRelation, offset, limit);
            this.goodIngredientsRatio = goodIngredientsRatio;
        }
    }
}
