package data.repositories;

import data.entities.RecipeSearch;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletionStage;

public interface RecipeSearchRepository {
    CompletionStage<RecipeSearch> create(String query, boolean isPermanent);
    CompletionStage<Boolean> delete(Long id);
    Integer deleteAll(List<Long> ids);
    CompletionStage<RecipeSearch> single(Long id);
    List<Long> queryNonPermanentOlderThan(Instant instant);
    int countAll();
    CompletionStage<RecipeSearch> update(String query, boolean isPermanent, Long id);
}
