package models.repositories.imp;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import models.DatabaseExecutionContext;
import models.entities.FavoriteRecipe;
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
    public CompletionStage<FavoriteRecipe> single(Long id) {
        return supplyAsync(() -> ebean.find(FavoriteRecipe.class, id), executionContext);
    }

    @Override
    public CompletionStage<Page<FavoriteRecipe>> allOfUser(Long userId) {
        return supplyAsync(() ->{
            List<FavoriteRecipe> result = ebean.createQuery(FavoriteRecipe.class)
                    .where()
                    .eq("user.id", userId)
                    .findList();

            return new Page<>(result, result.size());
        }, executionContext);
    }
}
