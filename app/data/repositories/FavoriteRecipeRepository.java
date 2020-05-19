package data.repositories;

import lombokized.repositories.Page;
import data.entities.FavoriteRecipe;

import java.util.concurrent.CompletionStage;

public interface FavoriteRecipeRepository {
    CompletionStage<FavoriteRecipe> single(Long id, Long userId);
    CompletionStage<Page<FavoriteRecipe>> allOfUser(Long userId);
    CompletionStage<Long> create(Long userId, Long recipeId);
    CompletionStage<Boolean> delete(Long id, Long userId);
}
