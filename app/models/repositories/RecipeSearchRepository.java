package models.repositories;

import java.util.concurrent.CompletionStage;

public interface RecipeSearchRepository {
    CompletionStage<Long> create(String query);
    CompletionStage<Integer> delete(Long id);
    CompletionStage<String> read(Long id);
}
