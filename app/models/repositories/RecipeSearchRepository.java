package models.repositories;

import models.entities.RecipeSearch;

import java.util.concurrent.CompletionStage;

public interface RecipeSearchRepository {
    CompletionStage<Page<RecipeSearch>> globals();
    CompletionStage<Page<RecipeSearch>> userSearches(Long userId);
    CompletionStage<RecipeSearch> userSearch(Long userId, Long entityId);
}
