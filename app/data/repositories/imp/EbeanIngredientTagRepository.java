package data.repositories.imp;

import data.DatabaseExecutionContext;
import data.entities.Ingredient;
import data.entities.IngredientTag;
import data.entities.Language;
import data.entities.User;
import data.repositories.IngredientTagRepository;
import data.repositories.exceptions.NotFoundException;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Query;
import lombokized.repositories.IngredientTagRepositoryParams;
import lombokized.repositories.Page;
import play.Logger;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public class EbeanIngredientTagRepository implements IngredientTagRepository {
    private EbeanServer ebean;
    private DatabaseExecutionContext executionContext;

    private static final Logger.ALogger logger = Logger.of(EbeanIngredientTagRepository.class);

    @Inject
    public EbeanIngredientTagRepository(EbeanConfig config, DatabaseExecutionContext executionContext) {
        this.ebean = Ebean.getServer(config.defaultServer());
        this.executionContext = executionContext;
    }

    @Override
    public CompletionStage<Page<IngredientTag>> page(IngredientTagRepositoryParams.Page params) {
        return supplyAsync(() -> {
            logger.info("page(): params = {}", params);
            Query<IngredientTag> query = ebean.createQuery(IngredientTag.class);
            query.where().ilike("name", "%" + params.getNameLike() + "%");
            query.where().eq("language.id", params.getLanguageId());
            query.setFirstRow(params.getOffset());
            query.setMaxRows(params.getLimit());

            if (params.getUserId() != null) {
                query.where().or()
                        .isNull("user.id")
                        .eq("user.id", params.getUserId());
            } else {
                query.where()
                        .isNull("user.id");
            }

            return new Page<>(query.findList(), query.findCount());
        }, executionContext);
    }

    @Override
    public CompletionStage<List<IngredientTag>> byIds(List<Long> ids) {
        return supplyAsync(() -> {
            logger.info("byIds(): ids = {}", ids);
            return ebean.createQuery(IngredientTag.class)
                    .where()
                    .in("id", ids)
                    .findList();
        }, executionContext);
    }

    @Override
    public CompletionStage<IngredientTag> byNameOfUser(Long userId, String name) {
        return supplyAsync(() -> {
            logger.info("byNameOfUser(): userId = {}, name = {}", userId, name);
            return ebean.createQuery(IngredientTag.class)
                    .where()
                    .eq("name", name)
                    .eq("user.id", userId)
                    .findOne();
        }, executionContext);
    }

    @Override
    public CompletionStage<Integer> count(Long userId) {
        return supplyAsync(() -> {
            logger.info("count(): userId = {}", userId);
            return ebean.createQuery(IngredientTag.class)
                    .where()
                    .eq("user.id", userId)
                    .findCount();
        }, executionContext);
    }

    @Override
    public CompletionStage<IngredientTag> create(Long userId, String name, List<Long> ingredientIds, Long languageId) {
        return supplyAsync(() -> {
            logger.info("create(): userId = {}, name = {}, languageId = {}, ingredientIds = {}",
                    userId, name, languageId, ingredientIds);
            EbeanRepoUtils.assertEntityExists(ebean, User.class, userId);
            EbeanRepoUtils.assertEntityExists(ebean, Language.class, languageId);

            User user = Ebean.find(User.class, userId);
            Language language = Ebean.find(Language.class, languageId);
            List<Ingredient> ingredients = ingredientByIds(ingredientIds);

            IngredientTag entity = new IngredientTag();
            entity.setUser(user);
            entity.setIngredients(ingredients);
            entity.setLanguage(language);
            entity.setName(name);

            ebean.save(entity);
            return entity;
        }, executionContext);
    }

    @Override
    public CompletionStage<IngredientTag> byId(Long id, Long userId) {
        return supplyAsync(() -> {
            logger.info("byId(): id = {}, userId = {}", id, userId);
            IngredientTag entity = ebean.createQuery(IngredientTag.class)
                    .where()
                    .eq("id", id)
                    .eq("user.id", userId)
                    .findOne();

            if (entity == null) {
                throwNotFoundException(id, userId);
            }

            return entity;
        }, executionContext);
    }

    @Override
    public CompletionStage<Void> update(Long id, Long userId, String name, List<Long> ingredientIds, Long languageId) {
        return runAsync(() -> {
            logger.info("update(): id = {}, userId = {}, name = {}, language = {}, ingredientIds = {}",
                    id, userId, name, ingredientIds, languageId);

            EbeanRepoUtils.assertEntityExists(ebean, User.class, userId);
            EbeanRepoUtils.assertEntityExists(ebean, Language.class, languageId);

            IngredientTag entity = Ebean.createQuery(IngredientTag.class)
                    .where()
                    .eq("id", id)
                    .eq("user.id", userId)
                    .findOne();

            if (entity == null) {
                logger.warn("update(): not found user defined tag with id = {}, userId = {}", id, userId);
                throwNotFoundException(id, userId);
            }

            List<Ingredient> ingredients = ingredientByIds(ingredientIds);
            Language language = ebean.find(Language.class, languageId);

            entity.setName(name);
            entity.setIngredients(ingredients);
            entity.setLanguage(language);

            ebean.update(entity);
        }, executionContext);
    }

    @Override
    public CompletionStage<Void> delete(Long id, Long userId) {
        return runAsync(() -> {
            logger.info("delete(): id = {}, userId = {}", id, userId);
            EbeanRepoUtils.assertEntityExists(ebean, User.class, userId);

            IngredientTag entity = ebean.createQuery(IngredientTag.class)
                    .where()
                    .eq("id", id)
                    .eq("user.id", userId)
                    .findOne();

            if (entity == null) {
                throwNotFoundException(id, userId);
            }

            ebean.delete(entity);

        }, executionContext);
    }

    private List<Ingredient> ingredientByIds(List<Long> ingredientIds) {
        return ebean.createQuery(Ingredient.class)
                .where()
                .in("id", ingredientIds)
                .findList();
    }

    private void throwNotFoundException(Long id, Long userId) {
        String message = String.format("Not found user defined tag with id = %d, userId = %d",
                id, userId);
        throw new NotFoundException(message);
    }
}
