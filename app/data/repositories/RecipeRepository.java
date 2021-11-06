package data.repositories;

import lombokized.repositories.Page;
import lombokized.repositories.RecipeRepositoryParams.*;
import data.entities.Recipe;

import java.util.concurrent.CompletionStage;

public interface RecipeRepository {
    Page<Recipe> pageOfQueryTypeNumber(QueryTypeNumber params);
    Page<Recipe> pageOfQueryTypeRatio(QueryTypeRatio params);
    Page<Recipe> pageOfQueryTypeNone(Common params);
    Recipe single(Long id);
}
