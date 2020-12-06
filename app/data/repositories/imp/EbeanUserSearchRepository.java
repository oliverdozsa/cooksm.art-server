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
import play.Logger;
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

    private static final Logger.ALogger logger = Logger.of(EbeanUserSearchRepository.class);

    @Inject
    public EbeanUserSearchRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext, Config config,
                                     RecipeSearchRepository recipeSearchRepository) {
        this.executionContext = executionContext;
        ebean = Ebean.getServer(ebeanConfig.defaultServer());
        this.recipeSearchRepository = recipeSearchRepository;
    }

    @Override
    public CompletionStage<UserSearch> create(String name, Long userId, Long recipeSearchId) {
        return recipeSearchRepository.single(recipeSearchId).thenApplyAsync(recipeSearch -> {
            logger.info("create(): name = {}, userId = {}, recipeSearchId = {}", name, userId, recipeSearchId);
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

            return userSearch;
        }, executionContext);
    }

    @Override
    public CompletionStage<Boolean> delete(Long id, Long userId) {
        return supplyAsync(() -> {
            logger.info("delete(): id = {}, userId = {}", id, userId);
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
                .thenApplyAsync(e -> ebean.delete(UserSearch.class, id) == 1);
    }

    @Override
    public CompletionStage<Page<UserSearch>> page(Long userId, int limit, int offset) {
        return supplyAsync(() -> {
            logger.info("page(): userId = {}, limit = {}, offset = {}", userId, limit, offset);
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
            logger.info("update(): name = {}, userId = {}, id = {}", name, userId, id);
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
            logger.info("all()");
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
            logger.info("single(): id = {}, userId = {}", id, userId);
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
        return supplyAsync(() -> {
            logger.info("count(): userId = {}", userId);
            return ebean.createQuery(UserSearch.class)
                    .where()
                    .eq("user.id", userId)
                    .findCount();
        }, executionContext);
    }

    private void throwNotFoundException(Long id, Long userId) {
        String message = String.format("Not found user search with id = %d, userId = %d",
                id, userId);
        throw new NotFoundException(message);
    }
}
