package models.repositories;

import models.entities.Recipe;

import java.util.concurrent.CompletionStage;

public interface RecipeRepository {
    CompletionStage<Page<Recipe>> pageOfComposedOfIngredients(RecipeQueryParameters.ByGoodIngredientsNumber params);
    CompletionStage<Page<Recipe>> pageOfComposedOfIngredients(RecipeQueryParameters.ByGoodIngredientsRatio params);
}
