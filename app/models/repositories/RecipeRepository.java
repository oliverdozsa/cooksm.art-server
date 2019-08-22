package models.repositories;

import models.entities.Recipe;

import java.util.concurrent.CompletionStage;

public interface RecipeRepository {
    CompletionStage<Page<Recipe>> pageOfByGoodIngredientsNumber(RecipeRepositoryQuery.WithGoodIngredientsNumberParams params);
    CompletionStage<Page<Recipe>> pageOfByGoodIngredientsRatio(RecipeRepositoryQuery.WithGoodIngredientsRatioParams params);
    CompletionStage<Page<Recipe>> pageOfAll(RecipeRepositoryQuery.CommonParams params);
    CompletionStage<Recipe> single(Long id);
}
