package models.repositories.imp;

import com.typesafe.config.Config;
import dto.RecipeSearchCreateUpdateDto;
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
import javax.validation.ValidatorFactory;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class EbeanRecipeSearchRepository implements RecipeSearchRepository {
    private EbeanServer ebean;
    private DatabaseExecutionContext executionContext;
    private int maxPerUser;
    private RecipeSearchCreateUpdateDtoValidator createUpdateDtoValidator;

    @Inject
    public EbeanRecipeSearchRepository(EbeanConfig dbConfig, DatabaseExecutionContext executionContext, Config config, ValidatorFactory validatorFactory) {
        ebean = Ebean.getServer(dbConfig.defaultServer());
        this.executionContext = executionContext;
        maxPerUser = config.getInt("receptnekem.usersearches.maxperuser");
        createUpdateDtoValidator = new RecipeSearchCreateUpdateDtoValidator(validatorFactory.getValidator());
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
            EbeanRepoUtils.assertEntityExists(ebean, User.class, userId);

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
            EbeanRepoUtils.assertEntityExists(ebean, User.class, userId);
            EbeanRepoUtils.assertEntityExists(ebean, RecipeSearch.class, entityId);

            return ebean.createQuery(RecipeSearch.class)
                    .where()
                    .eq("user.id", userId)
                    .eq("id", entityId)
                    .findOne();
        }, executionContext);
    }

    @Override
    public CompletionStage<Long> create(Long userId, RecipeSearchCreateUpdateDto dto) {
        return supplyAsync(() -> {
            EbeanRepoUtils.assertEntityExists(ebean, User.class, userId);
            assertCount(userId);
            createUpdateDtoValidator.validate(dto);

            RecipeSearch entity = new RecipeSearch();
            entity.setUser(ebean.find(User.class, userId));
            entity.setName(dto.getName());
            entity.setQuery(dto.getQuery());
            ebean.save(entity);

            return entity.getId();
        }, executionContext);
    }

    @Override
    public CompletionStage<Void> update(Long userId, Long entityId, RecipeSearchCreateUpdateDto dto) {
        return supplyAsync(() -> {
            EbeanRepoUtils.assertEntityExists(ebean, User.class, userId);
            EbeanRepoUtils.assertEntityExists(ebean, RecipeSearch.class, entityId);
            createUpdateDtoValidator.validate(dto);

            RecipeSearch entity = ebean.createQuery(RecipeSearch.class)
                    .where()
                    .eq("id", entityId)
                    .eq("user.id", userId)
                    .findOne();

            if (entity == null) {
                String msg = String.format("User (%d) has no such recipesearch (%d)!", userId, entityId);
                throw new BusinessLogicViolationException(msg);
            }

            entity.setName(dto.getName());
            entity.setQuery(dto.getQuery());

            ebean.update(entity);

            return null;
        }, executionContext);
    }

    @Override
    public CompletionStage<Boolean> delete(Long userId, Long entityId) {
        return supplyAsync(() -> {
            EbeanRepoUtils.assertEntityExists(ebean, User.class, userId);
            EbeanRepoUtils.assertEntityExists(ebean, RecipeSearch.class, entityId);

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
