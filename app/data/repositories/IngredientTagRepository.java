package data.repositories;

import data.entities.IngredientTag;
import lombokized.repositories.IngredientTagRepositoryParams;
import lombokized.repositories.Page;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface IngredientTagRepository {
    CompletionStage<Page<IngredientTag>> page(IngredientTagRepositoryParams.Page params);
    CompletionStage<List<IngredientTag>> byIds(List<Long> ids);
}
