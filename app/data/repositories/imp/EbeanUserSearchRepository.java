package data.repositories.imp;

import com.typesafe.config.Config;
import data.DatabaseExecutionContext;
import data.entities.RecipeSearch;
import data.entities.User;
import data.entities.UserSearch;
import data.repositories.RecipeSearchRepository;
import data.repositories.UserSearchRepository;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Query;
import lombokized.repositories.Page;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class EbeanUserSearchRepository implements UserSearchRepository {
    private DatabaseExecutionContext executionContext;
    private EbeanServer ebean;
    private RecipeSearchRepository recipeSearchRepository;
    private int maxPerUser;

    @Inject
    public EbeanUserSearchRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext, Config config,
                                     RecipeSearchRepository recipeSearchRepository) {
        this.executionContext = executionContext;
        ebean = Ebean.getServer(ebeanConfig.defaultServer());
        maxPerUser = config.getInt("receptnekem.usersearches.maxperuser");
        this.recipeSearchRepository = recipeSearchRepository;
    }

    @Override
    public CompletionStage<Long> create(String query, String name, Long userId) {
        return recipeSearchRepository.create(query, true).thenApplyAsync(searchId -> {
            EbeanRepoUtils.assertEntityExists(ebean, User.class, userId);
            if (name == null || name.length() == 0) {
                throw new IllegalArgumentException("name is empty!");
            }

            User user = ebean.find(User.class, userId);
            RecipeSearch recipeSearch = ebean.find(RecipeSearch.class, searchId);

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
}
