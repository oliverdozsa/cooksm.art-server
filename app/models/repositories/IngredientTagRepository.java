package models.repositories;

import models.entities.IngredientTag;

import java.util.concurrent.CompletionStage;

public interface IngredientTagRepository {
    CompletionStage<Page<IngredientTag>> page(String nameLike, Long languageId, int limit, int offset);
}
