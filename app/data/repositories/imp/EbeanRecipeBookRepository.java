package data.repositories.imp;

import data.entities.Recipe;
import data.entities.RecipeBook;
import data.entities.User;
import data.repositories.RecipeBookRepository;
import data.repositories.exceptions.NotFoundException;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.SqlUpdate;
import play.Logger;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static data.repositories.imp.EbeanRepoUtils.assertEntitiesExist;
import static data.repositories.imp.EbeanRepoUtils.assertEntityExists;

public class EbeanRecipeBookRepository implements RecipeBookRepository {
    private EbeanServer ebean;

    private static final Logger.ALogger logger = Logger.of(EbeanRecipeBookRepository.class);

    @Inject
    public EbeanRecipeBookRepository(EbeanConfig dbConfig) {
        ebean = Ebean.getServer(dbConfig.defaultServer());
    }

    @Override
    public RecipeBook create(String name, Long userId) {
        logger.info("create(): name = {}, userId = {}", name, userId);

        assertEntityExists(ebean, User.class, userId);

        User user = ebean.find(User.class, userId);

        RecipeBook entity = new RecipeBook();
        entity.setName(name);
        entity.setUser(user);
        entity.setLastAccessed(Instant.now());

        ebean.save(entity);

        return entity;
    }

    @Override
    public RecipeBook single(Long id, Long userId) {
        logger.info("single(): id = {}, userId = {}", id, userId);

        assertEntityExists(ebean, User.class, userId);
        assertEntityExists(ebean, RecipeBook.class, id);

        return findBookOfUser(userId, id);
    }

    @Override
    public Integer countOf(Long userId) {
        return ebean.createQuery(RecipeBook.class)
                .where()
                .eq("user.id", userId)
                .findCount();
    }

    @Override
    public Optional<RecipeBook> byNameOfUser(Long userId, String name) {
        return ebean.createQuery(RecipeBook.class)
                .where()
                .eq("user.id", userId)
                .eq("name", name)
                .findOneOrEmpty();
    }

    @Override
    public List<RecipeBook> allOf(Long userId) {
        assertEntityExists(ebean, User.class, userId);

        return ebean.createQuery(RecipeBook.class)
                .where()
                .eq("user.id", userId)
                .orderBy("lastAccessed desc")
                .findList();
    }

    @Override
    public void update(Long id, String name, Long userId) {
        logger.info("update(): id = {}, name = {}, userId = {}", id, name, userId);

        assertEntityExists(ebean, RecipeBook.class, id);
        assertEntityExists(ebean, User.class, userId);

        RecipeBook entity = findBookOfUser(userId, id);

        entity.setName(name);
        entity.setLastAccessed(Instant.now());
        ebean.save(entity);
    }

    @Override
    public void delete(Long id, Long userId) {
        logger.info("delete(): id = {}, userId = {}", id, userId);
        assertEntityExists(ebean, RecipeBook.class, id);
        assertEntityExists(ebean, User.class, userId);

        RecipeBook entity = findBookOfUser(userId, id);

        SqlUpdate sql = ebean.createSqlUpdate("delete from recipe_book_recipe where recipe_book_id = :book_id");
        sql.setParameter("book_id", id);
        sql.execute();

        ebean.delete(entity);
    }

    @Override
    public void addRecipes(Long id, Long userId, List<Long> recipeIds) {
        logger.info("addRecipes(): id = {}, userId = {}, recipeIds = {}", id, userId, recipeIds);

        assertEntityExists(ebean, RecipeBook.class, id);
        assertEntityExists(ebean, User.class, userId);
        assertEntitiesExist(ebean, Recipe.class, "id", recipeIds);

        RecipeBook entity = findBookOfUser(userId, id);

        Set<Long> futureRecipeIds = queryFutureRecipeIdsOf(entity, recipeIds);
        List<Recipe> recipes = findRecipes(futureRecipeIds);

        entity.setRecipes(recipes);
        entity.setLastAccessed(Instant.now());
        ebean.update(entity);
    }

    @Override
    public void removeRecipes(Long id, Long userId, List<Long> recipeIds) {
        logger.info("removeRecipes(): id = {}, userId = {}, recipeIds = {}", id, userId, recipeIds);

        Optional<RecipeBook> optionalRecipeBookOfUser = ebean.createQuery(RecipeBook.class)
                .where()
                .eq("id", id)
                .eq("user.id", userId)
                .findOneOrEmpty();

        if (!optionalRecipeBookOfUser.isPresent()) {
            throw new NotFoundException("Not found recipe books with id " + id + " for user " + userId);
        }

        ebean.createSqlUpdate("delete from recipe_book_recipe where recipe_book_id = :recipe_book_id and " +
                        "recipe_id in (:recipe_ids)")
                .setParameter("recipe_book_id", id)
                .setParameter("recipe_ids", recipeIds)
                .execute();
    }

    @Override
    public Integer futureCountOf(Long id, Long userId, List<Long> recipeIdsToAdd) {
        RecipeBook recipeBook = single(id, userId);
        return countFutureRecipeIds(recipeBook, recipeIdsToAdd);
    }

    @Override
    public void updateRecipes(Long id, Long userId, List<Long> recipeIds) {
        logger.info("updateRecipes(): id = {}, userId = {}, recipeIds = {}", id, userId, recipeIds);

        assertEntityExists(ebean, RecipeBook.class, id);
        assertEntityExists(ebean, User.class, userId);
        assertEntitiesExist(ebean, Recipe.class, "id", recipeIds);

        RecipeBook entity = findBookOfUser(userId, id);

        List<Recipe> recipes = findRecipes(recipeIds);
        entity.setRecipes(recipes);
        entity.setLastAccessed(Instant.now());
        ebean.update(entity);
    }

    @Override
    public void checkRecipeBooksOfUser(List<Long> recipeBookIds, Long userId) {
        logger.info("checkRecipeBooksOfUser(): userId = {}, recipeBookIds = {}", userId, recipeBookIds);

        List<RecipeBook> selectedBooksOfUser = ebean.createQuery(RecipeBook.class)
                .where()
                .eq("user.id", userId)
                .in("id", recipeBookIds)
                .findList();

        if (selectedBooksOfUser.size() != recipeBookIds.size()) {
            throw new NotFoundException("Not found found recipe book among user's recipe book!");
        }
    }

    @Override
    public List<RecipeBook> byIds(List<Long> ids) {
        logger.info("byIds(): ids = {}", ids);

        return ebean.createQuery(RecipeBook.class).where()
                .in("id", ids)
                .findList();
    }

    private RecipeBook findBookOfUser(Long userId, Long bookId) {
        RecipeBook entity = ebean.createQuery(RecipeBook.class)
                .where()
                .eq("user.id", userId)
                .eq("id", bookId)
                .findOne();

        if (entity == null) {
            throwNotFoundException(bookId, userId);
        }

        return entity;
    }

    private void throwNotFoundException(Long id, Long userId) {
        String message = String.format("Not found recipe book with id = %d, userId = %d",
                id, userId);
        throw new NotFoundException(message);
    }

    private List<Recipe> findRecipes(Collection<Long> recipeIds) {
        if (recipeIds == null || recipeIds.size() <= 0) {
            return new ArrayList<>();
        }

        return ebean.createQuery(Recipe.class)
                .where()
                .in("id", recipeIds)
                .findList();
    }

    private Integer countFutureRecipeIds(RecipeBook entity, List<Long> candidates) {
        Set<Long> futureRecipeIds = new HashSet<>(candidates);

        List<Long> recipeIdsOfBook = entity.getRecipes().stream()
                .map(Recipe::getId)
                .collect(Collectors.toList());
        futureRecipeIds.addAll(recipeIdsOfBook);

        return futureRecipeIds.size();
    }

    private Set<Long> queryFutureRecipeIdsOf(RecipeBook entity, List<Long> byCandidatesToAdd) {
        Set<Long> futureIds = new HashSet<>(byCandidatesToAdd);

        List<Long> recipeIdsOfBook = entity.getRecipes().stream()
                .map(Recipe::getId)
                .collect(Collectors.toList());
        futureIds.addAll(recipeIdsOfBook);

        return futureIds;
    }
}
