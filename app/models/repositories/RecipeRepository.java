package models.repositories;

import models.entities.Recipe;

import java.util.concurrent.CompletionStage;

public interface RecipeRepository {
    CompletionStage<Page<Recipe>> pageOfByGoodIngredientsRatio(RecipeRepositoryQueryParams.OfGoodIngredientsNumber params);
    CompletionStage<Page<Recipe>> pageOfByGoodIngredientsRatio(RecipeRepositoryQueryParams.OfGoodIngredientsRatio params);
}
