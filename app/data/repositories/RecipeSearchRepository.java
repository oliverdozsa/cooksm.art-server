package data.repositories;

import data.entities.RecipeSearch;

import java.util.concurrent.CompletionStage;

public interface RecipeSearchRepository {
    CompletionStage<RecipeSearch> create(String query, boolean isPermanent);
    CompletionStage<Boolean> delete(Long id);
    CompletionStage<String> read(Long id);
}
