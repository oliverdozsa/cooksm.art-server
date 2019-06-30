package models.repositories;

import models.entities.IngredientName;

import java.util.concurrent.CompletionStage;

public interface IngredientNameRepository {
    CompletionStage<Page<IngredientName>> page(String nameLike, Long languageId, int limit, int offset);
}
