package data.repositories.imp;

import com.typesafe.config.Config;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import data.DatabaseExecutionContext;
import data.entities.FavoriteRecipe;
import data.entities.Recipe;
import data.entities.User;
import data.repositories.FavoriteRecipeRepository;
import data.repositories.exceptions.BusinessLogicViolationException;
import play.Logger;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class EbeanFavoriteRecipeRepository implements FavoriteRecipeRepository {
    private EbeanServer ebean;
    private int maxPerUser;

    private static final Logger.ALogger logger = Logger.of(EbeanFavoriteRecipeRepository.class);

    @Inject
    public EbeanFavoriteRecipeRepository(EbeanConfig dbConfig, Config config) {
        ebean = Ebean.getServer(dbConfig.defaultServer());
        maxPerUser = config.getInt("cooksm.art.favoriterecipes.maxperuser");
    }

    @Override
    public FavoriteRecipe single(Long id, Long userId) {
        logger.info("single(): id = {}, userId = {}", id, userId);
        EbeanRepoUtils.assertEntityExists(ebean, FavoriteRecipe.class, id);
        EbeanRepoUtils.assertEntityExists(ebean, User.class, userId);

        return ebean.createQuery(FavoriteRecipe.class)
                .where()
                .eq("user.id", userId)
                .eq("id", id)
                .findOne();
    }

    @Override
    public List<FavoriteRecipe> all(Long userId) {
        logger.info("all(): userId = {}", userId);
        EbeanRepoUtils.assertEntityExists(ebean, User.class, userId);

        return ebean.createQuery(FavoriteRecipe.class)
                .where()
                .eq("user.id", userId)
                .findList();
    }

    @Override
    public FavoriteRecipe create(Long userId, Long recipeId) {
        logger.info("create(): userId = {}, recipeId = {}", userId, recipeId);
        EbeanRepoUtils.assertEntityExists(ebean, User.class, userId);
        EbeanRepoUtils.assertEntityExists(ebean, Recipe.class, recipeId);
        assertNotExists(userId, recipeId);
        assertCount(userId);

        FavoriteRecipe fr = new FavoriteRecipe();
        fr.setUser(ebean.find(User.class, userId));
        fr.setRecipe(ebean.find(Recipe.class, recipeId));
        ebean.save(fr);

        return fr;
    }

    @Override
    public Boolean delete(Long id, Long userId) {
        logger.info("delete(): id = {}, userId = {}", id, userId);
        EbeanRepoUtils.assertEntityExists(ebean, FavoriteRecipe.class, id);
        EbeanRepoUtils.assertEntityExists(ebean, User.class, userId);

        // To prevent deleting other users' entities, userId is needed.
        int deleteCount = ebean.createQuery(FavoriteRecipe.class)
                .where()
                .eq("user.id", userId)
                .eq("id", id)
                .delete();

        return deleteCount == 1;
    }

    private void assertCount(Long userId) {
        int count = count(userId);
        if (count >= maxPerUser) {
            String msg = String.format("User (%d) has too many favorites!", userId);
            throw new BusinessLogicViolationException(msg);
        }
    }

    private int count(Long userId) {
        return ebean.createQuery(FavoriteRecipe.class)
                .where()
                .eq("user.id", userId)
                .findCount();
    }

    private void assertNotExists(Long userId, Long recipeId) {
        if (exist(userId, recipeId)) {
            String msg = String.format("Favorite recipe with user id = %d, and recipe id = %d already exists!",
                    userId, recipeId);
            throw new BusinessLogicViolationException(msg);
        }
    }

    private boolean exist(Long userId, Long recipeId) {
        return ebean.createQuery(FavoriteRecipe.class)
                .where()
                .eq("user.id", userId)
                .eq("recipe.id", recipeId)
                .findOneOrEmpty().isPresent();
    }
}
