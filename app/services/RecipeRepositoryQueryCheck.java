package services;

import com.typesafe.config.Config;

import javax.inject.Inject;

import static lombokized.repositories.RecipeRepositoryParams.*;

import java.util.List;
import java.util.Optional;

public class RecipeRepositoryQueryCheck {
    private static boolean SHOULD_DISABLE_CHECK = true;

    @Inject
    public RecipeRepositoryQueryCheck(Config config) {
        SHOULD_DISABLE_CHECK = config.getBoolean("cooksm.art.disable.mutual.exclusion.check");
    }

    public void check(QueryTypeNumber query) {
        checkMutuallyExclusive(query.getIncludedIngredients(), query.getCommon().getExcludedIngredients());
        checkMutuallyExclusive(query.getAdditionalIngredients(), query.getCommon().getExcludedIngredients());
        checkMutuallyExclusive(query.getAdditionalIngredients(), query.getIncludedIngredients());
    }

    public void check(QueryTypeRatio query) {
        checkMutuallyExclusive(query.getIncludedIngredients(), query.getCommon().getExcludedIngredients());
    }

    private static void checkMutuallyExclusive(Optional<AdditionalIngredients> additional, List<Long> excluded) {
        if (additional.isPresent() && !areMutuallyExclusive(additional.get().getAdditionalIngredients(), excluded)) {
            throw new IllegalArgumentException("Additional and excluded ingredients are not mutually exclusive!");
        }
    }

    private static void checkMutuallyExclusive(Optional<AdditionalIngredients> additional, IncludedIngredients included) {
        if (additional.isPresent() && !areMutuallyExclusive(additional.get().getAdditionalIngredients(), included.getIncludedIngredients())) {
            throw new IllegalArgumentException("Additional and included ingredients are not mutually exclusive!");
        }
    }

    private static void checkMutuallyExclusive(IncludedIngredients included, List<Long> excluded) {
        if (!areMutuallyExclusive(included.getIncludedIngredients(), excluded)) {
            throw new IllegalArgumentException("Included and excluded ingredients are not mutually exclusive!");
        }
    }

    private static boolean areMutuallyExclusive(List<Long> included, List<Long> excluded) {
        if (included == null || excluded == null || SHOULD_DISABLE_CHECK) {
            return true;
        }

        for (Long id : included) {
            if (excluded.contains(id)) {
                return false;
            }
        }

        return true;
    }
}
