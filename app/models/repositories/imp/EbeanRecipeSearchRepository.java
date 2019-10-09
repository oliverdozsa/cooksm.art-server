package models.repositories.imp;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import models.DatabaseExecutionContext;
import models.entities.RecipeSearch;
import models.repositories.Page;
import models.repositories.RecipeSearchRepository;
import play.Logger;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class EbeanRecipeSearchRepository implements RecipeSearchRepository {
    private EbeanServer ebean;
    private DatabaseExecutionContext executionContext;
    private static final Logger.ALogger logger = Logger.of(EbeanRecipeRepository.class);

    @Inject
    public EbeanRecipeSearchRepository(EbeanConfig dbConfig, DatabaseExecutionContext executionContext) {
        this.ebean = Ebean.getServer(dbConfig.defaultServer());
        this.executionContext = executionContext;
    }

    @Override
    public CompletionStage<Page<RecipeSearch>> globals() {
        return supplyAsync(() -> {
            List<RecipeSearch> searches = ebean.createQuery(RecipeSearch.class)
                    .where()
                    .isNull("user.id")
                    .findList();
            return new Page<>(searches, searches.size());
        }, executionContext);
    }
}
