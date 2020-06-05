package data.repositories.imp;

import com.typesafe.config.Config;
import data.DatabaseExecutionContext;
import data.entities.RecipeSearch;
import data.entities.User;
import data.entities.UserSearch;
import data.repositories.RecipeSearchRepository;
import data.repositories.UserSearchRepository;
import data.repositories.exceptions.BusinessLogicViolationException;
import data.repositories.exceptions.NotFoundException;
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
    public CompletionStage<Long> create(String name, Long userId, Long recipeSearchId) {
        return recipeSearchRepository.single(recipeSearchId).thenApplyAsync(recipeSearch -> {
            EbeanRepoUtils.assertEntityExists(ebean, User.class, userId);
            if (name == null || name.length() == 0) {
                throw new IllegalArgumentException("name is empty!");
            }

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
    public CompletionStage<Boolean> delete(Long id, Long userId) {
        return supplyAsync(() -> {
            EbeanRepoUtils.assertEntityExists(ebean, UserSearch.class, id);
            EbeanRepoUtils.assertEntityExists(ebean, User.class, userId);
            UserSearch entity = ebean.createQuery(UserSearch.class)
                    .where()
                    .eq("user.id", userId)
                    .eq("id", id)
                    .findOne();
            if (entity == null) {
                throwNotFoundException(id, userId);
            }
            return ebean.find(UserSearch.class, id);
        }, executionContext)
                .thenComposeAsync(e -> recipeSearchRepository.delete(e.getSearch().getId()))
                .thenApplyAsync(deleteSuccess -> {
                    if (!deleteSuccess) {
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
    public CompletionStage<UserSearch> update(String name, Long userId, Long id) {
        return supplyAsync(() -> {
            EbeanRepoUtils.assertEntityExists(ebean, User.class, userId);
            EbeanRepoUtils.assertEntityExists(ebean, UserSearch.class, id);
            UserSearch entity = ebean.createQuery(UserSearch.class)
                    .where()
                    .eq("user.id", userId)
                    .eq("id", id)
                    .findOne();
            if (entity == null) {
                throwNotFoundException(id, userId);
            }

            entity.setName(name);
            ebean.save(entity);
            return entity;
        });
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

    @Override
    public CompletionStage<UserSearch> single(Long id, Long userId) {
        return supplyAsync(() -> {
            EbeanRepoUtils.assertEntityExists(ebean, User.class, userId);
            EbeanRepoUtils.assertEntityExists(ebean, UserSearch.class, id);
            UserSearch entity = ebean.createQuery(UserSearch.class)
                    .where()
                    .eq("user.id", userId)
                    .eq("id", id)
                    .findOne();
            if (entity == null) {
                throwNotFoundException(id, userId);
            }

            return entity;
        }, executionContext);
    }

    @Override
    public CompletionStage<Integer> count(Long userId) {
        return supplyAsync(() -> ebean.createQuery(UserSearch.class)
                .where()
                .eq("user.id", userId)
                .findCount(), executionContext);
    }

    private void throwNotFoundException(Long id, Long userId) {
        String message = String.format("Not found user search with id = %d, userId = %d",
                id, userId);
        throw new NotFoundException(message);
    }
}
