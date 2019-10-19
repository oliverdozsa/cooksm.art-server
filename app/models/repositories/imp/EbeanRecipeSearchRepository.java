package models.repositories.imp;

import com.typesafe.config.Config;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import models.DatabaseExecutionContext;
import models.entities.RecipeSearch;
import models.entities.User;
import models.repositories.Page;
import models.repositories.RecipeSearchRepository;
import models.repositories.exceptions.BusinessLogicViolationException;
import play.Logger;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class EbeanRecipeSearchRepository implements RecipeSearchRepository {
    private EbeanServer ebean;
    private DatabaseExecutionContext executionContext;
    private int maxPerUser;
    private static final Logger.ALogger logger = Logger.of(EbeanRecipeRepository.class);

    @Inject
    public EbeanRecipeSearchRepository(EbeanConfig dbConfig, DatabaseExecutionContext executionContext, Config config) {
        this.ebean = Ebean.getServer(dbConfig.defaultServer());
        this.executionContext = executionContext;
        this.maxPerUser = config.getInt("receptnekem.usersearches.maxperuser");
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

    @Override
    public CompletionStage<Page<RecipeSearch>> userSearches(Long userId) {
        return supplyAsync(() -> {
            EbeanRepoUtils.assertEntity(ebean, User.class, userId);

            List<RecipeSearch> searches = ebean.createQuery(RecipeSearch.class)
                    .where()
                    .eq("user.id", userId)
                    .findList();
            return new Page<>(searches, searches.size());
        }, executionContext);
    }

    @Override
    public CompletionStage<RecipeSearch> userSearch(Long userId, Long entityId) {
        return supplyAsync(() -> {
            EbeanRepoUtils.assertEntity(ebean, User.class, userId);
            EbeanRepoUtils.assertEntity(ebean, RecipeSearch.class, entityId);

            return ebean.createQuery(RecipeSearch.class)
                    .where()
                    .eq("user.id", userId)
                    .eq("id", entityId)
                    .findOne();
        }, executionContext);
    }

    @Override
    public CompletionStage<Long> create(Long userId, String name, String query) {
        return supplyAsync(() -> {
            EbeanRepoUtils.assertEntity(ebean, User.class, userId);
            assertCount(userId);

            RecipeSearch entity = new RecipeSearch();
            entity.setUser(ebean.find(User.class, userId));
            entity.setName(name);
            entity.setQuery(query);
            ebean.save(entity);

            return entity.getId();
        }, executionContext);
    }

    @Override
    public CompletionStage<Void> update(Long userId, Long entityId, String name, String query) {
        return supplyAsync(() -> {
            EbeanRepoUtils.assertEntity(ebean, User.class, userId);
            EbeanRepoUtils.assertEntity(ebean, RecipeSearch.class, entityId);

            RecipeSearch entity = ebean.createQuery(RecipeSearch.class)
                    .where()
                    .eq("id", entityId)
                    .eq("user.id", userId)
                    .findOne();

            if (entity == null) {
                String msg = String.format("User (%d) has no such recipesearch (%d)!", userId, entityId);
                throw new BusinessLogicViolationException(msg);
            }

            entity.setName(name);
            entity.setQuery(query);

            ebean.update(entity);

            return null;
        }, executionContext);
    }

    @Override
    public CompletionStage<Boolean> delete(Long userId, Long entityId) {
        return supplyAsync(() -> {
            EbeanRepoUtils.assertEntity(ebean, User.class, userId);
            EbeanRepoUtils.assertEntity(ebean, RecipeSearch.class, entityId);

            int deleteCount = ebean.createQuery(RecipeSearch.class)
                    .where()
                    .eq("user.id", userId)
                    .eq("id", entityId)
                    .delete();

            return deleteCount == 1;
        }, executionContext);
    }

    private void assertCount(Long userId) {
        int count = count(userId);
        if (count >= maxPerUser) {
            String msg = String.format("User (%d) has too many searches!", userId);
            throw new BusinessLogicViolationException(msg);
        }
    }

    private int count(Long userId) {
        return ebean.createQuery(RecipeSearch.class)
                .where()
                .eq("user.id", userId)
                .findCount();
    }
}
