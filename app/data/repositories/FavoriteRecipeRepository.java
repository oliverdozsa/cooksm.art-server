package data.repositories;

import data.entities.FavoriteRecipe;

import java.util.List;

public interface FavoriteRecipeRepository {
    FavoriteRecipe single(Long id, Long userId);
    List<FavoriteRecipe> all(Long userId);
    FavoriteRecipe create(Long userId, Long recipeId);
    Boolean delete(Long id, Long userId);
}
