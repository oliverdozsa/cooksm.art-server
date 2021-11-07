package data.repositories;

import data.entities.RecipeSearch;

import java.time.Instant;
import java.util.List;

public interface RecipeSearchRepository {
    RecipeSearch create(String query, boolean isPermanent);
    Boolean delete(Long id);
    Integer deleteAll(List<Long> ids);
    RecipeSearch single(Long id);
    List<Long> queryNonPermanentOlderThan(Instant instant);
    int countAll();
    RecipeSearch update(String query, boolean isPermanent, Long id);
}
