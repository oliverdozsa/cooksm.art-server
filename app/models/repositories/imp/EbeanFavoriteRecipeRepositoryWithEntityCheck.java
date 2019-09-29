package models.repositories.imp;

import models.DatabaseExecutionContext;
import models.entities.FavoriteRecipe;
import models.entities.User;
import models.repositories.FavoriteRecipeRepository;
import models.repositories.Page;

import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.runAsync;

// TODO

public class EbeanFavoriteRecipeRepositoryWithEntityCheck implements FavoriteRecipeRepository {
    private EbeanFavoriteRecipeRepository delegate;
    private DatabaseExecutionContext executionContext;

    public EbeanFavoriteRecipeRepositoryWithEntityCheck(EbeanFavoriteRecipeRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public CompletionStage<FavoriteRecipe> single(Long id, Long userId) {
        return runAsync(() -> EbeanRepoUtils.checkEntity(delegate.getEbean(), User.class, userId),
                delegate.getExecutionContext())
                .thenCompose(v -> delegate.single(id, userId));
    }

    @Override
    public CompletionStage<Page<FavoriteRecipe>> allOfUser(Long userId) {
        return null;
    }

    @Override
    public CompletionStage<Long> create(Long userId, Long recipeId) {
        return null;
    }

    @Override
    public CompletionStage<Boolean> delete(Long id, Long userId) {
        return null;
    }
}
