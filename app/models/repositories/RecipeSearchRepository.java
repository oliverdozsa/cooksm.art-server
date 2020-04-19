package models.repositories;

import lombokized.dto.RecipeSearchCreateUpdateDto;
import lombokized.repositories.Page;
import models.entities.RecipeSearch;

import java.util.concurrent.CompletionStage;

public interface RecipeSearchRepository {
    CompletionStage<Page<RecipeSearch>> globals();
    CompletionStage<Page<RecipeSearch>> userSearches(Long userId);
    CompletionStage<RecipeSearch> userSearch(Long userId, Long entityId);
    CompletionStage<Long> create(Long userId, RecipeSearchCreateUpdateDto dto);
    CompletionStage<Void> update(Long userId, Long entityId, RecipeSearchCreateUpdateDto dto);
    CompletionStage<Boolean> delete(Long userId, Long entityId);
}
