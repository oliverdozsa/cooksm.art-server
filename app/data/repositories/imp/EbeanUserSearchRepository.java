package data.repositories.imp;

import data.entities.RecipeSearch;
import data.entities.User;
import data.entities.UserSearch;
import data.repositories.RecipeSearchRepository;
import data.repositories.UserSearchRepository;
import data.repositories.exceptions.NotFoundException;
import io.ebean.EbeanServer;
import io.ebean.Query;
import lombokized.repositories.Page;
import play.Logger;

import javax.inject.Inject;
import java.util.List;

public class EbeanUserSearchRepository implements UserSearchRepository {
    private EbeanServer ebean;
    private RecipeSearchRepository recipeSearchRepository;

    private static final Logger.ALogger logger = Logger.of(EbeanUserSearchRepository.class);

    @Inject
    public EbeanUserSearchRepository(EbeanServer ebean, RecipeSearchRepository recipeSearchRepository) {
        this.ebean = ebean;
        this.recipeSearchRepository = recipeSearchRepository;
    }

    @Override
    public UserSearch create(String name, Long userId, Long recipeSearchId) {
        RecipeSearch recipeSearch = recipeSearchRepository.single(recipeSearchId);
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
    }

    @Override
    public Boolean delete(Long id, Long userId) {
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

        UserSearch userSearch = ebean.find(UserSearch.class, id);
        return ebean.delete(UserSearch.class, userSearch.getId()) == 1;
    }

    @Override
    public Page<UserSearch> page(Long userId, int limit, int offset) {
        logger.info("page(): userId = {}, limit = {}, offset = {}", userId, limit, offset);
        Query<UserSearch> query = ebean.createQuery(UserSearch.class)
                .where()
                .eq("user.id", userId)
                .setFirstRow(offset)
                .setMaxRows(limit);

        return new Page<>(query.findList(), query.findCount());
    }

    @Override
    public UserSearch update(String name, Long userId, Long id) {
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
    }

    @Override
    public List<UserSearch> all(Long userId) {
        logger.info("all()");
        EbeanRepoUtils.assertEntityExists(ebean, User.class, userId);
        return ebean.createQuery(UserSearch.class)
                .where()
                .eq("user.id", userId)
                .findList();
    }

    @Override
    public UserSearch single(Long id, Long userId) {
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
    }

    @Override
    public Integer count(Long userId) {
        logger.info("count(): userId = {}", userId);
        return ebean.createQuery(UserSearch.class)
                .where()
                .eq("user.id", userId)
                .findCount();
    }

    private void throwNotFoundException(Long id, Long userId) {
        String message = String.format("Not found user search with id = %d, userId = %d",
                id, userId);
        throw new NotFoundException(message);
    }
}
