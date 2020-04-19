package models.repositories;

import lombokized.repositories.Page;
import models.entities.IngredientName;

import java.util.concurrent.CompletionStage;

public interface IngredientNameRepository {
    CompletionStage<Page<IngredientName>> page(String nameLike, Long languageId, int limit, int offset);
}
