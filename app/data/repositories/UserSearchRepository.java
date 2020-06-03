package data.repositories;

import lombokized.repositories.Page;
import data.entities.UserSearch;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface UserSearchRepository {
    CompletionStage<Long> create(String name, Long userId, Long recipeSearchId);
    CompletionStage<Boolean> delete(Long id);
    CompletionStage<Page<UserSearch>> page(Long userId, int limit, int offset);
    CompletionStage<Void> update(String query, String name, Long userId, Long searchId);
    CompletionStage<List<UserSearch>> all(Long userId);
}
