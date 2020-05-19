package data.repositories;

import java.util.concurrent.CompletionStage;

public interface RecipeSearchRepository {
    CompletionStage<Long> create(String query);
    CompletionStage<Boolean> delete(Long id);
    CompletionStage<String> read(Long id);
}
