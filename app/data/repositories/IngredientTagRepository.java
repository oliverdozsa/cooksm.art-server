package data.repositories;

import data.entities.IngredientTag;
import data.entities.IngredientTagName;
import data.entities.UserSearch;
import lombokized.repositories.IngredientTagRepositoryParams;
import lombokized.repositories.Page;

import java.util.List;

public interface IngredientTagRepository {
    Page<IngredientTag> page(IngredientTagRepositoryParams.Page params);
    List<IngredientTag> byIds(List<Long> ids);
    IngredientTag byNameOfUser(Long userId, String name, Long languageId);
    Integer count(Long userId);
    IngredientTag create(Long userId, String name, List<Long> ingredientIds, Long languageId);
    IngredientTag byId(Long id, Long userId);
    void update(Long id, Long userId, String name, List<Long> ingredientIds, Long languageId);
    void delete(Long id, Long userId);
    List<UserSearch> userSearchesOf(Long id, Long userId);
    Boolean containsUserDefined(List<Long> tags);
    List<IngredientTag> userDefinedOnly(Long userId);

    List<IngredientTagName> byIds(Long languageId, List<Long> ids);
}
