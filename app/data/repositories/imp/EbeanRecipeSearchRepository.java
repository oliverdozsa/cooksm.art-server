package data.repositories.imp;

import data.DatabaseExecutionContext;
import data.entities.RecipeSearch;
import data.repositories.RecipeSearchRepository;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.time.Instant;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class EbeanRecipeSearchRepository implements RecipeSearchRepository {
    private EbeanServer ebean;
    private DatabaseExecutionContext executionContext;

    @Inject
    public EbeanRecipeSearchRepository(EbeanConfig dbConfig, DatabaseExecutionContext executionContext) {
        this.executionContext = executionContext;
        this.ebean = Ebean.getServer(dbConfig.defaultServer());
    }

    @Override
    public CompletionStage<Long> create(String query, boolean isPermanent) {
        return supplyAsync(() -> {
            if (query == null || query.length() == 0) {
                throw new IllegalArgumentException("query is empty!");
            }

            RecipeSearch entity = new RecipeSearch();
            if (!isPermanent) {
                entity.setLastAccessed(Instant.now());
            }
            entity.setPermanent(isPermanent);
            entity.setQuery(query);
            ebean.save(entity);
            return entity.getId();
        }, executionContext);
    }

    @Override
    public CompletionStage<Boolean> delete(Long id) {
        return supplyAsync(() -> ebean.delete(RecipeSearch.class, id) == 1, executionContext);
    }

    @Override
    public CompletionStage<RecipeSearch> read(Long id) {
        return supplyAsync(() -> {
            EbeanRepoUtils.assertEntityExists(ebean, RecipeSearch.class, id);
            RecipeSearch entity = ebean.find(RecipeSearch.class, id);
            entity.setLastAccessed(Instant.now());
            ebean.save(entity);
            return entity;
        }, executionContext);
    }
}
