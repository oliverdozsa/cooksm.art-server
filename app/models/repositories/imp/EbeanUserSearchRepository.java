package models.repositories.imp;

import com.typesafe.config.Config;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Query;
import lombokized.repositories.Page;
import models.DatabaseExecutionContext;
import models.entities.RecipeSearch;
import models.entities.User;
import models.entities.UserSearch;
import models.repositories.UserSearchRepository;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class EbeanUserSearchRepository implements UserSearchRepository {
    private DatabaseExecutionContext executionContext;
    private EbeanServer ebean;
    private int maxPerUser;

    @Inject
    public EbeanUserSearchRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext, Config config) {
        this.executionContext = executionContext;
        ebean = Ebean.getServer(ebeanConfig.defaultServer());
        maxPerUser = config.getInt("receptnekem.usersearches.maxperuser");
    }

    @Override
    public CompletionStage<Long> create(String query, String name, Long userId) {
        return supplyAsync(() -> {
            EbeanRepoUtils.assertEntityExists(ebean, User.class, userId);
            if (name == null || name.length() == 0) {
                throw new IllegalArgumentException("name is empty!");
            }

            RecipeSearch recipeSearch = createRecipeSearch(query);
            User user = ebean.find(User.class, userId);

            UserSearch userSearch = new UserSearch();
            userSearch.setSearch(recipeSearch);
            userSearch.setUser(user);
            userSearch.setName(name);
            ebean.save(userSearch);

            return userSearch.getId();
        }, executionContext);
    }

    @Override
    public CompletionStage<Boolean> delete(Long id) {
        return supplyAsync(() -> ebean.delete(UserSearch.class, id) == 1, executionContext);
    }

    @Override
    public CompletionStage<Page<UserSearch>> page(Long userId, int limit, int offset) {
        return supplyAsync(() -> {
            Query<UserSearch> query = ebean.createQuery(UserSearch.class)
                    .where()
                    .eq("user.id", userId)
                    .setFirstRow(offset)
                    .setMaxRows(limit);

            return new Page<>(query.findList(), query.findCount());
        }, executionContext);
    }

    @Override
    public CompletionStage<Void> update(String query, String name, Long userId, Long searchId) {
        return CompletableFuture.runAsync(() -> {
            EbeanRepoUtils.assertEntityExists(ebean, User.class, userId);
            EbeanRepoUtils.assertEntityExists(ebean, RecipeSearch.class, searchId);
            if (query == null || query.length() == 0) {
                throw new IllegalArgumentException("query is empty");
            }
            if (name == null || name.length() == 0) {
                throw new IllegalArgumentException("name is null!");
            }

            UserSearch userSearch = ebean.find(UserSearch.class, searchId);
            userSearch.setName(name);
            userSearch.getSearch().setQuery(query);
            ebean.save(userSearch);
        }, executionContext);
    }

    private RecipeSearch createRecipeSearch(String query) {
        if (query == null || query.length() == 0) {
            throw new IllegalArgumentException("query is empty!");
        }

        RecipeSearch entity = new RecipeSearch();

        entity.setQuery(query);
        entity.setPermanent(true);

        return entity;
    }
}
