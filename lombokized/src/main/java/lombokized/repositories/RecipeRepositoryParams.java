package lombokized.repositories;

import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecipeRepositoryParams {

    @Value
    @Builder(builderClassName = "Builder")
    public static class QueryTypeNumber {
        Integer goodIngredients;
        Relation goodIngredientsRelation;
        Integer unknownIngredients;
        Relation unknownIngredientsRelation;
        Common common;
        IncludedIngredients includedIngredients;
        AdditionalIngredients additionalIngredients;

        public Optional<AdditionalIngredients> getAdditionalIngredients() {
            return Optional.ofNullable(additionalIngredients);
        }
    }

    @Value
    @Builder(builderClassName = "Builder")
    public static class QueryTypeRatio {
        Float goodIngredientsRatio;
        Common common;
        IncludedIngredients includedIngredients;
        AdditionalIngredients additionalIngredients;

        public Optional<AdditionalIngredients> getAdditionalIngredients() {
            return Optional.ofNullable(additionalIngredients);
        }
    }

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

    @Value
    @Builder(builderClassName = "Builder", toBuilder = true)
    public static class Common {
        Integer minimumNumberOfIngredients;
        Integer maximumNumberOfIngredients;
        @lombok.Builder.Default List<Long> excludedIngredients = new ArrayList<>();
        List<Long> excludedIngredientTags;
        String orderBy;
        String orderBySort;
        Integer offset;
        Integer limit;
        String nameLike;
        List<Long> sourcePageIds;
        List<Long> times;
        Long userId;
        Boolean useFavoritesOnly;
    }

    @Value
    @Builder(builderClassName = "Builder")
    public static class IncludedIngredients {
        @lombok.Builder.Default List<Long> includedIngredients = new ArrayList<>();
        List<Long> includedIngredientTags;
    }

    @Value
    @Builder(builderClassName = "Builder")
    public static class AdditionalIngredients {
        Integer goodAdditionalIngredients;
        Relation goodAdditionalIngredientsRelation;
        @lombok.Builder.Default List<Long> additionalIngredients = new ArrayList<>();
        List<Long> additionalIngredientTags;
    }
}
