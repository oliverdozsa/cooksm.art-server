package data.repositories;

import data.entities.IngredientTag;
import lombokized.repositories.IngredientTagRepositoryParams;
import lombokized.repositories.Page;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface IngredientTagRepository {
    CompletionStage<Page<IngredientTag>> page(IngredientTagRepositoryParams.Page params);
    CompletionStage<List<IngredientTag>> byIds(List<Long> ids);
    CompletionStage<IngredientTag> byNameOfUser(Long userId, String name);
    CompletionStage<Integer> count(Long userId);
    CompletionStage<IngredientTag> create(Long userId, String name, List<Long> ingredientIds, Long languageId);
    CompletionStage<IngredientTag> byId(Long id, Long userId);
    CompletionStage<Void> update(Long id, Long userId, String name, List<Long> ingredientIds, Long languageId);
    CompletionStage<Void> delete(Long id, Long userId);
}
