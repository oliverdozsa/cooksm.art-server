package models.repositories;

import lombokized.repositories.Page;
import models.entities.UserSearch;

import java.util.concurrent.CompletionStage;

public interface UserSearchRepository {
    CompletionStage<Long> create(String query, String name, Long userId);
    CompletionStage<Boolean> delete(Long id);
    CompletionStage<Page<UserSearch>> page(Long userId, int limit, int offset);
    CompletionStage<Void> update(String query, String name, Long userId, Long searchId);
}
