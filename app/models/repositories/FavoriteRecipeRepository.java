package models.repositories;

import models.entities.FavoriteRecipe;

import java.util.concurrent.CompletionStage;

public interface FavoriteRecipeRepository {
    CompletionStage<FavoriteRecipe> single(Long id);
    CompletionStage<Page<FavoriteRecipe>> allOfUser(Long userId);
}
