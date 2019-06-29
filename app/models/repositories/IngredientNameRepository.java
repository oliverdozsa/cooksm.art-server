package models.repositories;

import models.entities.IngredientName;

import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

public interface IngredientNameRepository {
    CompletionStage<Stream<IngredientName>> list(String nameLike, Long languageId, int limit, int offset);
}
