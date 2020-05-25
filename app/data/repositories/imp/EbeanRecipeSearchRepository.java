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
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class EbeanRecipeSearchRepository implements RecipeSearchRepository {
    private EbeanServer ebean;
    private DatabaseExecutionContext executionContext;
    private static AtomicInteger count;

    @Inject
    public EbeanRecipeSearchRepository(EbeanConfig dbConfig, DatabaseExecutionContext executionContext) {
        this.executionContext = executionContext;
        this.ebean = Ebean.getServer(dbConfig.defaultServer());
        initCount(ebean);
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
            count.incrementAndGet();
            return entity.getId();
        }, executionContext);
    }

    @Override
    public CompletionStage<Boolean> delete(Long id) {
        return supplyAsync(() -> {
            boolean isDeleted = ebean.delete(RecipeSearch.class, id) == 1;
            if (isDeleted) {
                count.decrementAndGet();
            }
            return isDeleted;
        }, executionContext);
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

    @Override
    public int getCount() {
        return count.get();
    }

    private static synchronized void initCount(EbeanServer ebean) {
        if (count == null) {
            int countEntities = ebean.createQuery(RecipeSearch.class).findCount();
            count = new AtomicInteger(countEntities);
        }
    }
}
