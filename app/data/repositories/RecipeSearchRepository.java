package data.repositories;

import data.entities.RecipeSearch;

import java.util.concurrent.CompletionStage;

public interface RecipeSearchRepository {
    CompletionStage<Long> create(String query, boolean isPermanent);
    CompletionStage<Boolean> delete(Long id);
    CompletionStage<RecipeSearch> read(Long id);
    int getCount();
}
