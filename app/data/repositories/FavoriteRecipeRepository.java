package data.repositories;

import lombokized.repositories.Page;
import data.entities.FavoriteRecipe;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface FavoriteRecipeRepository {
    CompletionStage<FavoriteRecipe> single(Long id, Long userId);
    CompletionStage<List<FavoriteRecipe>> all(Long userId);
    CompletionStage<FavoriteRecipe> create(Long userId, Long recipeId);
    CompletionStage<Boolean> delete(Long id, Long userId);
}
