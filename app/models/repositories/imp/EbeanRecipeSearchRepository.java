package models.repositories.imp;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import models.DatabaseExecutionContext;
import models.entities.RecipeSearch;
import models.repositories.RecipeSearchRepository;
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
    public CompletionStage<Long> create(String query) {
        return supplyAsync(() -> {
            RecipeSearch entity = new RecipeSearch();
            entity.setLastAccessed(Instant.now());
            entity.setPermanent(false);
            entity.setQuery(query);
            Ebean.save(entity);
            return entity.getId();
        }, executionContext);
    }

    @Override
    public CompletionStage<Integer> delete(Long id) {
        return supplyAsync(() -> Ebean.delete(RecipeSearch.class, id), executionContext);
    }

    @Override
    public CompletionStage<String> read(Long id) {
        return supplyAsync(() -> {
            EbeanRepoUtils.assertEntityExists(ebean, RecipeSearch.class, id);
            RecipeSearch entity = Ebean.find(RecipeSearch.class, id);
            return entity.getQuery();
        }, executionContext);
    }
}
