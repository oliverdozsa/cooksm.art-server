package data.repositories;

import data.entities.RecipeBook;
import lombokized.repositories.Page;
import lombokized.repositories.RecipeRepositoryParams.*;
import data.entities.Recipe;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface RecipeRepository {
    Page<Recipe> pageOfQueryTypeNumber(QueryTypeNumber params);
    Page<Recipe> pageOfQueryTypeRatio(QueryTypeRatio params);
    Page<Recipe> pageOfQueryTypeNone(Common params);
    Recipe single(Long id);
    List<RecipeBook> recipeBooksOf(Long id, Long userId);
}
