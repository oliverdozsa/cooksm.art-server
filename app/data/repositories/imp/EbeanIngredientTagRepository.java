package data.repositories.imp;

import com.fasterxml.jackson.databind.JsonNode;
import data.entities.*;
import data.repositories.IngredientTagRepository;
import data.repositories.exceptions.NotFoundException;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Query;
import lombokized.repositories.IngredientTagRepositoryParams;
import lombokized.repositories.Page;
import play.Logger;
import play.libs.Json;
import queryparams.RecipesQueryParams;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EbeanIngredientTagRepository implements IngredientTagRepository {
    private EbeanServer ebean;

    private static final Logger.ALogger logger = Logger.of(EbeanIngredientTagRepository.class);

    @Inject
    public EbeanIngredientTagRepository(EbeanServer ebean) {
        this.ebean = ebean;
    }

    @Override
    public Page<IngredientTag> page(IngredientTagRepositoryParams.Page params) {
        logger.info("page(): params = {}", params);
        Query<IngredientTag> query = ebean.createQuery(IngredientTag.class);
        query.where().ilike("names.name", "%" + params.getNameLike() + "%");
        query.where().eq("names.language.id", params.getLanguageId());
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
    }

    @Override
    public List<IngredientTag> byIds(List<Long> ids) {
        logger.info("byIds(): ids = {}", ids);
        return ebean.createQuery(IngredientTag.class)
                .where()
                .in("id", ids)
                .findList();
    }

    @Override
    public IngredientTag byNameOfUser(Long userId, String name, Long languageId) {
        logger.info("byNameOfUser(): userId = {}, name = {}", userId, name);
        return ebean.createQuery(IngredientTag.class)
                .where()
                .eq("names.name", name)
                .eq("user.id", userId)
                .eq("names.language.id", userId)
                .findOne();
    }

    @Override
    public Integer count(Long userId) {
        logger.info("count(): userId = {}", userId);
        return ebean.createQuery(IngredientTag.class)
                .where()
                .eq("user.id", userId)
                .findCount();
    }

    @Override
    public IngredientTag create(Long userId, String name, List<Long> ingredientIds, Long languageId) {
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

        IngredientTagName tagName = new IngredientTagName();
        tagName.setLanguage(language);
        tagName.setName(name);
        entity.setNames(Arrays.asList(tagName));

        ebean.save(entity);
        return entity;
    }

    @Override
    public IngredientTag byId(Long id, Long userId) {
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
    }

    @Override
    public void update(Long id, Long userId, String name, List<Long> ingredientIds, Long languageId) {
        logger.info("update(): id = {}, userId = {}, name = {}, language = {}, ingredientIds = {}",
                id, userId, name, ingredientIds, languageId);

        EbeanRepoUtils.assertEntityExists(ebean, User.class, userId);
        EbeanRepoUtils.assertEntityExists(ebean, Language.class, languageId);

        IngredientTag entity = Ebean.createQuery(IngredientTag.class)
                .where()
                .eq("id", id)
                .eq("user.id", userId)
                .eq("names.language.id", languageId)
                .findOne();

        if (entity == null) {
            logger.warn("update(): not found user defined tag with id = {}, userId = {}, language = {}", id, userId, languageId);
            throwNotFoundException(id, userId);
        }

        List<Ingredient> ingredients = ingredientByIds(ingredientIds);
        entity.setIngredients(ingredients);

        Language language = ebean.find(Language.class, languageId);

        IngredientTagName tagName = new IngredientTagName();
        tagName.setLanguage(language);
        tagName.setName(name);
        entity.setNames(Arrays.asList(tagName));

        ebean.update(entity);
    }

    @Override
    public void delete(Long id, Long userId) {
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
    }

    @Override
    public List<UserSearch> userSearchesOf(Long id, Long userId) {
        logger.info("userSearchesOf(): id = {}, userId = {}", id, userId);
        byId(id, userId);

        List<UserSearch> userSearches = ebean.createQuery(UserSearch.class)
                .where()
                .eq("user.id", userId)
                .findList();

        List<UserSearch> userSearchesContainingTag = userSearches.stream()
                .filter(userSearch -> containsTag(userSearch, id))
                .collect(Collectors.toList());

        logger.info("userSearchesOf(): found {} user searches containing tag {}",
                userSearchesContainingTag.size(), id);

        return userSearchesContainingTag;
    }

    @Override
    public Boolean containsUserDefined(List<Long> ids) {
        logger.info("containsUserDefined(): tags = {}", ids);

        return ebean.createQuery(IngredientTag.class)
                .where()
                .in("id", ids)
                .isNotNull("user.id")
                .exists();
    }

    @Override
    public List<IngredientTag> userDefinedOnly(Long userId) {
        logger.info("userDefinedOnly(): userId = {}", userId);

        return ebean.createQuery(IngredientTag.class)
                .where()
                .eq("user.id", userId)
                .findList();
    }

    @Override
    public List<IngredientTagName> byIds(Long languageId, List<Long> ids) {
        logger.info("byIds(): languageId = {}, ids = {}", languageId, ids);

        return ebean.createQuery(IngredientTagName.class)
                .where()
                .in("tag.id", ids)
                .eq("language.id", languageId)
                .findList();
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

    private boolean containsTag(UserSearch entity, Long tagId) {
        String jsonQueryStr = entity.getSearch().getQuery();
        JsonNode queryJson = Json.parse(jsonQueryStr);
        RecipesQueryParams.Params queryParams = Json.fromJson(queryJson, RecipesQueryParams.Params.class);

        List<Long> allTagIds = new ArrayList<>();
        addAllOf(queryParams.inIngTags, allTagIds);
        addAllOf(queryParams.exIngTags, allTagIds);
        addAllOf(queryParams.addIngTags, allTagIds);

        return allTagIds.contains(tagId);
    }

    private void addAllOf(List<Long> source, List<Long> target) {
        if (source != null) {
            target.addAll(source);
        }
    }
}
