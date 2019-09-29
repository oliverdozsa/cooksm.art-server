package models.repositories.imp;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.ExpressionFactory;
import models.DatabaseExecutionContext;
import models.entities.FavoriteRecipe;
import models.entities.Recipe;
import models.entities.User;
import models.repositories.FavoriteRecipeRepository;
import models.repositories.Page;
import play.Logger;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class EbeanFavoriteRecipeRepository implements FavoriteRecipeRepository {
    private EbeanServer ebean;
    private DatabaseExecutionContext executionContext;

    private static final Logger.ALogger logger = Logger.of(EbeanRecipeRepository.class);

    @Inject
    public EbeanFavoriteRecipeRepository(EbeanConfig dbConfig, DatabaseExecutionContext executionContext) {
        ebean = Ebean.getServer(dbConfig.defaultServer());
        this.executionContext = executionContext;
    }

    @Override
    public CompletionStage<FavoriteRecipe> single(Long id, Long userId) {
        return supplyAsync(() -> ebean.createQuery(FavoriteRecipe.class)
                        .where()
                        .eq("user.id", userId)
                        .eq("id", id)
                        .findOne(),
                executionContext);
    }

    @Override
    public CompletionStage<Page<FavoriteRecipe>> allOfUser(Long userId) {
        return supplyAsync(() -> {
            List<FavoriteRecipe> result = ebean.createQuery(FavoriteRecipe.class)
                    .where()
                    .eq("user.id", userId)
                    .findList();

            return new Page<>(result, result.size());
        }, executionContext);
    }

    @Override
    public CompletionStage<Long> create(Long userId, Long recipeId) {
        return supplyAsync(() -> {
            FavoriteRecipe fr = new FavoriteRecipe();
            fr.setUser(ebean.find(User.class, userId));
            fr.setRecipe(ebean.find(Recipe.class, recipeId));
            ebean.save(fr);

            return fr.getId();
        }, executionContext);
    }

    @Override
    public CompletionStage<Boolean> delete(Long id, Long userId) {
        return supplyAsync(() -> {
            // To prevent deleting other users' entities, userId is needed.
            int deletecCount = ebean.createQuery(FavoriteRecipe.class)
                    .where()
                    .eq("user.id", userId)
                    .eq("id", id)
                    .delete();

            return deletecCount == 1;
        }, executionContext);
    }

    public EbeanServer getEbean() {
        return ebean;
    }

    public DatabaseExecutionContext getExecutionContext() {
        return executionContext;
    }
}
