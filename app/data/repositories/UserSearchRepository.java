package data.repositories;

import data.entities.UserSearch;
import lombokized.repositories.Page;

import java.util.List;

public interface UserSearchRepository {
    UserSearch create(String name, Long userId, Long recipeSearchId);
    Boolean delete(Long id, Long userId);
    Page<UserSearch> page(Long userId, int limit, int offset);
    UserSearch update(String name, Long userId, Long id);
    List<UserSearch> all(Long userId);
    UserSearch single(Long id, Long userId);
    Integer count(Long userId);
}
