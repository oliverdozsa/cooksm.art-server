package data.repositories.imp;

import data.DatabaseExecutionContext;
import data.entities.RecipeBook;
import data.entities.User;
import data.repositories.RecipeBookRepository;
import data.repositories.exceptions.NotFoundException;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import play.Logger;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static data.repositories.imp.EbeanRepoUtils.assertEntityExists;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public class EbeanRecipeBookRepository implements RecipeBookRepository {
    private EbeanServer ebean;
    private DatabaseExecutionContext executionContext;

    private static final Logger.ALogger logger = Logger.of(EbeanRecipeBookRepository.class);

    @Inject
    public EbeanRecipeBookRepository(EbeanConfig dbConfig, DatabaseExecutionContext executionContext) {
        ebean = Ebean.getServer(dbConfig.defaultServer());
        this.executionContext = executionContext;
    }

    @Override
    public CompletionStage<RecipeBook> create(String name, Long userId) {
        return supplyAsync(() -> {
            logger.info("create(): name = {}, userId = {}", name, userId);

            assertEntityExists(ebean, User.class, userId);

            User user = ebean.find(User.class, userId);

            RecipeBook entity = new RecipeBook();
            entity.setName(name);
            entity.setUser(user);
            entity.setLastAccessed(Instant.now());

            ebean.save(entity);

            return entity;
        }, executionContext);
    }

    @Override
    public CompletionStage<RecipeBook> single(Long id, Long userId) {
        return supplyAsync(() -> {
            logger.info("single(): id = {}, userId = {}", id, userId);

            assertEntityExists(ebean, User.class, userId);
            assertEntityExists(ebean, RecipeBook.class, id);

            RecipeBook entity = ebean.createQuery(RecipeBook.class)
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
    public CompletionStage<Integer> countOf(Long userId) {
        return supplyAsync(() -> ebean.createQuery(RecipeBook.class)
                        .where()
                        .eq("user.id", userId)
                        .findCount()
                , executionContext);
    }

    @Override
    public CompletionStage<Optional<RecipeBook>> byNameOfUser(Long userId, String name) {
        return supplyAsync(() -> ebean.createQuery(RecipeBook.class)
                        .where()
                        .eq("user.id", userId)
                        .eq("name", name)
                        .findOneOrEmpty()
                , executionContext);
    }

    @Override
    public CompletionStage<List<RecipeBook>> allOf(Long userId) {
        return supplyAsync(() -> {
            assertEntityExists(ebean, User.class, userId);

            return ebean.createQuery(RecipeBook.class)
                    .where()
                    .eq("user.id", userId)
                    .findList();
        });
    }

    @Override
    public CompletionStage<Void> update(Long id, String name, Long userId) {
        return runAsync(() -> {
            logger.info("update(): id = {}, name = {}, userId = {}", id, name, userId);

            assertEntityExists(ebean, RecipeBook.class, id);
            assertEntityExists(ebean, User.class, userId);

            RecipeBook entity = ebean.createQuery(RecipeBook.class)
                    .where()
                    .eq("user.id", userId)
                    .eq("id", id)
                    .findOne();

            if (entity == null) {
                throwNotFoundException(id, userId);
            }

            entity.setName(name);
            entity.setLastAccessed(Instant.now());
            ebean.save(entity);

        }, executionContext);
    }

    private void throwNotFoundException(Long id, Long userId) {
        String message = String.format("Not found recipe book with id = %d, userId = %d",
                id, userId);
        throw new NotFoundException(message);
    }
}
