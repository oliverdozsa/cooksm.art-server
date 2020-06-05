package data.repositories;

import lombokized.repositories.Page;
import data.entities.UserSearch;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface UserSearchRepository {
    CompletionStage<Long> create(String name, Long userId, Long recipeSearchId);
    CompletionStage<Boolean> delete(Long id, Long userId);
    CompletionStage<Page<UserSearch>> page(Long userId, int limit, int offset);
    CompletionStage<UserSearch> update(String name, Long userId, Long id);
    CompletionStage<List<UserSearch>> all(Long userId);
    CompletionStage<UserSearch> single(Long id, Long userId);
    CompletionStage<Integer> count(Long userId);
}
