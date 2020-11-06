package data.repositories.imp;

import data.entities.Recipe;
import io.ebean.Query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class IngredientsConditionSetter {
    private List<Long> ingredientIds;
    private List<Long> ingredientTagIds;
    private EbeanIngredientTagsResolver tagsResolver;

    public IngredientsConditionSetter(EbeanIngredientTagsResolver tagsResolver, List<Long> ingredientIds, List<Long> ingredientTagIds) {
        this.ingredientIds = new ArrayList<>(ingredientIds);
        this.ingredientTagIds = ingredientTagIds;
        this.tagsResolver = tagsResolver;
    }

    public void set(Query<Recipe> query, String paramName) {
        if(ingredientTagIds != null && ingredientTagIds.size() > 0) {
            List<Long> ingredientIdsOfTags = tagsResolver.resolve(ingredientTagIds);
            mergeToIngredientIds(ingredientIdsOfTags);
        }

        query.setParameter(paramName, ingredientIds);
    }

    private void mergeToIngredientIds(List<Long> ingredientIdsToAdd) {
        Set<Long> ids = new HashSet<>(ingredientIds);
        ids.addAll(ingredientIdsToAdd);
        ingredientIds.clear();
        ingredientIds.addAll(ids);
    }
}
