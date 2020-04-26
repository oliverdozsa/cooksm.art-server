package models.repositories;

import lombokized.repositories.Page;
import lombokized.repositories.RecipeRepositoryParams.*;
import models.entities.Recipe;

import java.util.concurrent.CompletionStage;

public interface RecipeRepository {
    CompletionStage<Page<Recipe>> pageOfQueryTypeNumber(QueryTypeNumber params);
    CompletionStage<Page<Recipe>> pageOfQueryTypeRatio(QueryTypeRatio params);
    CompletionStage<Page<Recipe>> pageOfQueryTypeNone(Common params);
    CompletionStage<Recipe> single(Long id);
}
