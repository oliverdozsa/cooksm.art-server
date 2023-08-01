package data.repositories.imp;

import com.typesafe.config.Config;
import data.entities.ShoppingList;
import data.entities.ShoppingListItem;
import data.entities.ShoppingListItemCategory;
import data.entities.User;
import data.repositories.ShoppingListRepository;
import data.repositories.exceptions.ForbiddenExeption;
import data.repositories.exceptions.NotFoundException;
import dto.ShoppingListItemRequestDto;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.SqlUpdate;
import play.Logger;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static data.repositories.imp.EbeanRepoUtils.assertEntityExists;

public class EbeanShoppingListRepository implements ShoppingListRepository {
    private EbeanServer ebean;
    private final int perUserLimit;
    private final int itemsPerShoppingListLimit;

    private static final Logger.ALogger logger = Logger.of(EbeanShoppingListRepository.class);

    @Inject
    public EbeanShoppingListRepository(EbeanConfig dbConfig, Config config) {
        ebean = Ebean.getServer(dbConfig.defaultServer());
        perUserLimit = config.getInt("cooksm.art.shoppinglist.maxperuser");
        itemsPerShoppingListLimit = config.getInt("cooksm.art.shoppinglist.maxitems");
    }

    @Override
    public Long create(Long userId, String name, List<ShoppingListItemRequestDto> itemDtos) {
        logger.info("create(): userId = {}, name = {}, items = {}", userId, name, itemDtos);

        assertEntityExists(ebean, User.class, userId);

        int numOfShoppingListsOfUser = countAUsersShoppingLists(userId);
        if (numOfShoppingListsOfUser >= perUserLimit) {
            throw new ForbiddenExeption("User already has the allowed number of shopping lists (" + perUserLimit + ")");
        }

        ShoppingList shoppingList = new ShoppingList();
        shoppingList.setName(name);

        User user = ebean.find(User.class, userId);
        shoppingList.setUser(user);

        if (itemDtos != null) {
            List<ShoppingListItem> itemEntities = itemDtos.stream()
                    .map(i -> fromItemDtoInShoppingList(i, shoppingList))
                    .collect(Collectors.toList());
            shoppingList.setItems(itemEntities);
        }

        ebean.save(shoppingList);

        return shoppingList.getId();
    }

    @Override
    public List<ShoppingList> allOfUser(Long userId) {
        logger.info("allOfUser(): userId = {}", userId);

        assertEntityExists(ebean, User.class, userId);
        return ebean.createQuery(ShoppingList.class).where()
                .eq("user.id", userId)
                .findList();
    }

    @Override
    public ShoppingList single(Long userId, Long shoppingListId) {
        logger.info("single(): userId = {}, shoppingListId = {}", userId, shoppingListId);

        return findShoppingListOfUser(userId, shoppingListId);
    }

    @Override
    public void delete(Long userId, Long shoppingListId) {
        logger.info("userId = {}, shoppingListId = {}", userId, shoppingListId);

        assertEntityExists(ebean, User.class, userId);
        assertEntityExists(ebean, ShoppingList.class, shoppingListId);

        ShoppingList shoppingList = findShoppingListOfUser(userId, shoppingListId);
        ebean.delete(shoppingList);
    }

    @Override
    public void rename(Long userId, Long shoppingListId, String newName) {
        logger.info("rename(): userId = {}, shoppingListId = {}, newName = {}", userId, shoppingListId, newName);

        assertEntityExists(ebean, User.class, userId);
        assertEntityExists(ebean, ShoppingList.class, shoppingListId);

        ShoppingList shoppingList = findShoppingListOfUser(userId, shoppingListId);
        shoppingList.setName(newName);

        ebean.save(shoppingList);
    }

    @Override
    public void addItems(Long userId, Long shoppingListId, List<ShoppingListItemRequestDto> newItems) {
        logger.info("addItems(): userId = {}, shoppingListId = {}, newItems = {}",
                userId, shoppingListId, newItems);

        ShoppingList shoppingList = findShoppingListOfUser(userId, shoppingListId);

        Set<Long> uniqueCategoryIdsFromRequest = newItems.stream()
                .map(i -> i.categoryId)
                .collect(Collectors.toSet());
        checkIfCategoriesExist(uniqueCategoryIdsFromRequest);

        Set<String> alreadyExistingItemNames = shoppingList.getItems().stream()
                .map(ShoppingListItem::getName)
                .collect(Collectors.toSet());

        List<ShoppingListItem> newAndNotAlreadyExistingItems = newItems.stream()
                .filter(i -> !alreadyExistingItemNames.contains(i.getName()))
                .map(i -> fromItemDtoInShoppingList(i, shoppingList))
                .collect(Collectors.toList());

        if (alreadyExistingItemNames.size() >= itemsPerShoppingListLimit && newAndNotAlreadyExistingItems.size() > 0) {
            throw new ForbiddenExeption("Can't add more items; shopping list (" + shoppingListId + ") " +
                    "already has maximum allowed items (" + itemsPerShoppingListLimit + ")");
        }

        shoppingList.getItems().addAll(newAndNotAlreadyExistingItems);
        ebean.save(shoppingList);
    }

    @Override
    public void removeItems(Long userId, Long shoppingListId, List<Long> itemsToRemove) {
        logger.info("removeItems(): userId = {}, shoppingListId = {}, itemsToRemove = {}",
                userId, shoppingListId, itemsToRemove);

        findShoppingListOfUser(userId, shoppingListId);

        SqlUpdate sqlUpdate = ebean.createSqlUpdate("delete from shopping_list_item where shopping_list_id = :shopping_list_id and " +
                " id in (:item_ids)");

        sqlUpdate.setParameter("shopping_list_id", shoppingListId);
        sqlUpdate.setParameter("item_ids", itemsToRemove);

        sqlUpdate.execute();
    }

    @Override
    public void completeAnItem(Long userId, Long shoppingListId, Long itemId) {
        logger.info("completeAnItem(): userId = {}, shoppingListId = {}, itemId = {}",
                userId, shoppingListId, itemId);

        ShoppingListItem itemToComplete = findItemOfShoppingListOfUserWithId(userId, shoppingListId, itemId);

        itemToComplete.setCompleted(true);
        ebean.save(itemToComplete);
    }

    @Override
    public void undoAnItem(Long userId, Long shoppingListId, Long itemId) {
        logger.info("undoAnItem(): userId = {}, shoppingListId = {}, itemId = {}",
                userId, shoppingListId, itemId);

        ShoppingListItem itemToUndo = findItemOfShoppingListOfUserWithId(userId, shoppingListId, itemId);

        itemToUndo.setCompleted(false);
        ebean.save(itemToUndo);
    }

    private ShoppingListItem fromItemDtoInShoppingList(ShoppingListItemRequestDto itemDto, ShoppingList shoppingListEntity) {
        ShoppingListItemCategory categoryEntity = ebean.find(ShoppingListItemCategory.class, itemDto.getCategoryId());

        ShoppingListItem itemEntity = new ShoppingListItem();
        itemEntity.setName(itemDto.getName());
        itemEntity.setCategory(categoryEntity);
        itemEntity.setShoppingList(shoppingListEntity);

        return itemEntity;
    }

    private ShoppingList findShoppingListOfUser(Long userId, Long shoppingListId) {
        ShoppingList shoppingList = ebean.createQuery(ShoppingList.class)
                .where()
                .eq("user.id", userId)
                .eq("id", shoppingListId)
                .findOne();

        if (shoppingList == null) {
            throw new NotFoundException("Not found shopping list with userId = " + userId + " and shoppingListId = " + shoppingListId);
        }

        return shoppingList;
    }

    private ShoppingListItem findItemOfShoppingListOfUserWithId(Long userId, Long shoppingListId, Long itemId) {
        Optional<ShoppingListItem> shoppingListItemOptional = ebean.createQuery(ShoppingListItem.class)
                .where()
                .eq("shoppingList.user.id", userId)
                .eq("shoppingList.id", shoppingListId)
                .eq("id", itemId)
                .findOneOrEmpty();

        if (!shoppingListItemOptional.isPresent()) {
            throw new NotFoundException("Not found item with ID: " + itemId + " in shopping list.");
        }

        return shoppingListItemOptional.get();
    }

    private int countAUsersShoppingLists(Long userId) {
        return ebean.createQuery(ShoppingList.class)
                .where()
                .eq("user.id", userId)
                .findCount();
    }

    private void checkIfCategoriesExist(Set<Long> categoryIdsToCheck) {
        Set<Long> categoryIdsFromDb = ebean.createQuery(ShoppingListItemCategory.class)
                .where()
                .in("id", categoryIdsToCheck)
                .findList()
                .stream()
                .map(ShoppingListItemCategory::getId)
                .collect(Collectors.toSet());

        if(!categoryIdsFromDb.equals(categoryIdsToCheck)) {
            throw new NotFoundException("Not found every category from these: " + categoryIdsToCheck);
        }
    }
}
