package data.repositories;

import lombokized.repositories.Page;
import data.entities.IngredientName;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface IngredientNameRepository {
    CompletionStage<Page<IngredientName>> page(String nameLike, Long languageId, int limit, int offset);
    CompletionStage<List<IngredientName>> byIngredientIds(List<Long> ids, Long languageId);
    CompletionStage<IngredientName> singleByIngredientId(Long id, Long languageId);
}
