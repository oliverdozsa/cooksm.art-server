package data.repositories;

import data.entities.IngredientName;
import lombokized.repositories.Page;

import java.util.List;

public interface IngredientNameRepository {
    Page<IngredientName> page(String nameLike, Long languageId, int limit, int offset);
    List<IngredientName> byIngredientIds(List<Long> ids, Long languageId);
}
