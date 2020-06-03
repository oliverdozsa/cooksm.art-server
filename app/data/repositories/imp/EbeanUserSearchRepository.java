package data.repositories.imp;

import com.typesafe.config.Config;
import data.DatabaseExecutionContext;
import data.entities.RecipeSearch;
import data.entities.User;
import data.entities.UserSearch;
import data.repositories.RecipeSearchRepository;
import data.repositories.UserSearchRepository;
import data.repositories.exceptions.BusinessLogicViolationException;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Query;
import lombokized.repositories.Page;
import play.db.ebean.EbeanConfig;
import play.db.ebean.Transactional;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class EbeanUserSearchRepository implements UserSearchRepository {
    private DatabaseExecutionContext executionContext;
    private EbeanServer ebean;
    private RecipeSearchRepository recipeSearchRepository;

    @Inject
    public EbeanUserSearchRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext, Config config,
                                     RecipeSearchRepository recipeSearchRepository) {
        this.executionContext = executionContext;
        ebean = Ebean.getServer(ebeanConfig.defaultServer());
        this.recipeSearchRepository = recipeSearchRepository;
    }

    @Override
    @Transactional
    public CompletionStage<Long> create(String name, Long userId, Long recipeSearchId) {
        return recipeSearchRepository.read(recipeSearchId).thenApplyAsync(searchId -> {
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
        return supplyAsync(() -> {
            EbeanRepoUtils.assertEntityExists(ebean, UserSearch.class, id);
            return ebean.find(UserSearch.class, id);
        }, executionContext)
                .thenComposeAsync(e -> recipeSearchRepository.delete(e.getSearch().getId()))
                .thenApplyAsync(deleteResult -> {
                    if (deleteResult) {
                        throw new BusinessLogicViolationException("User search doesn't have recipe search!");
                    }

                    return ebean.delete(UserSearch.class, id) == 1;
                });
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
    public CompletionStage<Void> update(String query, String name, Long userId, Long userSearchId) {
        return CompletableFuture.runAsync(() -> {
            EbeanRepoUtils.assertEntityExists(ebean, User.class, userId);
            EbeanRepoUtils.assertEntityExists(ebean, RecipeSearch.class, userSearchId);
            if (query == null || query.length() == 0) {
                throw new IllegalArgumentException("query is empty");
            }
            if (name == null || name.length() == 0) {
                throw new IllegalArgumentException("name is null!");
            }

            UserSearch userSearch = ebean.createQuery(UserSearch.class)
                    .where()
                    .eq("user.id", userId)
                    .eq("id", userSearchId)
                    .findOne();

            userSearch.setName(name);
            userSearch.getSearch().setQuery(query);
            ebean.save(userSearch.getSearch());
            ebean.save(userSearch);
        }, executionContext);
    }

    @Override
    public CompletionStage<List<UserSearch>> all(Long userId) {
        return supplyAsync(() -> {
            EbeanRepoUtils.assertEntityExists(ebean, User.class, userId);
            return ebean.createQuery(UserSearch.class)
                    .where()
                    .eq("user.id", userId)
                    .findList();
        }, executionContext);
    }
}
