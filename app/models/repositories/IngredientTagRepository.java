package models.repositories;

import lombokized.repositories.Page;
import models.entities.IngredientTag;

import java.util.concurrent.CompletionStage;

public interface IngredientTagRepository {
    CompletionStage<Page<IngredientTag>> page(String nameLike, Long languageId, int limit, int offset);
}
