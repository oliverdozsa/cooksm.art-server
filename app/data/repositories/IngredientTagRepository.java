package data.repositories;

import data.entities.IngredientTag;
import lombokized.repositories.Page;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface IngredientTagRepository {
    CompletionStage<Page<IngredientTag>> page(String nameLike, Long languageId, int limit, int offset);
    CompletionStage<List<IngredientTag>> byIds(List<Long> ids);
}
