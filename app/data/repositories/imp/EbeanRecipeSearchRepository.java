package data.repositories.imp;

import data.entities.RecipeSearch;
import data.repositories.RecipeSearchRepository;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import play.Logger;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class EbeanRecipeSearchRepository implements RecipeSearchRepository {
    private EbeanServer ebean;
    private static AtomicInteger count;

    private static final Logger.ALogger logger = Logger.of(EbeanRecipeSearchRepository.class);

    @Inject
    public EbeanRecipeSearchRepository(EbeanServer ebean) {
        this.ebean = ebean;
        initCount(ebean);
        logger.info("EbeanRecipeSearchRepository(): count = {}", count.get());
    }

    @Override
    public RecipeSearch create(String query, boolean isPermanent) {
        logger.info("create(): isPermanent = {}, query = {}", isPermanent, query);
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
        logger.info("create(): count = {}", count.get());
        return entity;
    }

    @Override
    public Boolean delete(Long id) {
        logger.info("delete(): id = {}", id);
        boolean isDeleted = ebean.delete(RecipeSearch.class, id) == 1;
        if (isDeleted) {
            count.decrementAndGet();
            logger.info("delete(): count = {}", count.get());
        }
        return isDeleted;
    }

    @Override
    public RecipeSearch single(Long id) {
        logger.info("single(): id = {}", id);
        EbeanRepoUtils.assertEntityExists(ebean, RecipeSearch.class, id);
        RecipeSearch entity = ebean.find(RecipeSearch.class, id);
        entity.setLastAccessed(Instant.now());
        ebean.save(entity);
        return entity;
    }

    @Override
    public int countAll() {
        logger.info("countAll()");
        return count.get();
    }

    @Override
    public List<Long> queryNonPermanentOlderThan(Instant instant) {
        logger.info("queryNonPermanentOlderThan(): instant = {}", instant);
        return ebean.createQuery(RecipeSearch.class)
                .where()
                .eq("isPermanent", false)
                .le("lastAccessed", instant)
                .findList()
                .stream()
                .map(RecipeSearch::getId)
                .collect(Collectors.toList());
    }

    @Override
    public Integer deleteAll(List<Long> ids) {
        logger.info("deleteAll(): ids = {}", ids);
        int deleted = 0;
        for (Long id : ids) {
            boolean isDeleted = ebean.delete(RecipeSearch.class, id) == 1;
            if (isDeleted) {
                count.decrementAndGet();
                logger.info("deleteAll(): count = {}", count.get());
                deleted++;
            }
        }

        return deleted;
    }

    @Override
    public RecipeSearch update(String query, boolean isPermanent, Long id) {
        logger.info("update(): query = {}, isPermanent = {}, id = {}", query, isPermanent, id);
        EbeanRepoUtils.assertEntityExists(ebean, RecipeSearch.class, id);
        RecipeSearch entity = ebean.find(RecipeSearch.class, id);

        entity.setQuery(query);
        entity.setPermanent(isPermanent);
        if (!isPermanent) {
            entity.setLastAccessed(Instant.now());
        }
        ebean.save(entity);
        return entity;
    }

    private static synchronized void initCount(EbeanServer ebean) {
        if (count == null) {
            int countEntities = ebean.createQuery(RecipeSearch.class).findCount();
            count = new AtomicInteger(countEntities);
        }
    }
}
