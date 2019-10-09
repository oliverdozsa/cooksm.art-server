package models.repositories;

import models.entities.RecipeSearch;

import java.util.concurrent.CompletionStage;

public interface RecipeSearchRepository {
    CompletionStage<Page<RecipeSearch>> globals();
}
