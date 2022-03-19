package data.repositories.imp;

import com.typesafe.config.Config;
import data.entities.ShoppingList;
import data.entities.ShoppingListItem;
import data.entities.User;
import data.repositories.ShoppingListRepository;
import data.repositories.exceptions.BusinessLogicViolationException;
import data.repositories.exceptions.ForbiddenExeption;
import data.repositories.exceptions.NotFoundException;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
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
        perUserLimit = config.getInt("receptnekem.shoppinglist.maxperuser");
        itemsPerShoppingListLimit = config.getInt("receptnekem.shoppinglist.maxitems");
    }

    @Override
    public Long create(Long userId, String name, List<String> items) {
        logger.info("create(): userId = {}, name = {}, items = {}", userId, name, items);

        assertEntityExists(ebean, User.class, userId);

        int numOfShoppingListsOfUser = countAUsersShoppingLists(userId);
        if (numOfShoppingListsOfUser >= perUserLimit) {
            throw new ForbiddenExeption("User already has the allowed number of shopping lists (" + perUserLimit + ")");
        }

        ShoppingList shoppingList = new ShoppingList();
        shoppingList.setName(name);

        User user = ebean.find(User.class, userId);
        shoppingList.setUser(user);

        if (items != null) {
            List<ShoppingListItem> itemEntities = items.stream()
                    .map(i -> fromItemNameInShoppingList(i, shoppingList))
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
    public void addItems(Long userId, Long shoppingListId, List<String> newItems) {
        logger.info("addItems(): userId = {}, shoppingListId = {}, newItems = {}",
                userId, shoppingListId, newItems);

        assertEntityExists(ebean, User.class, userId);
        assertEntityExists(ebean, ShoppingList.class, shoppingListId);

        ShoppingList shoppingList = findShoppingListOfUser(userId, shoppingListId);
        Set<String> alreadyExistingItems = shoppingList.getItems().stream()
                .map(ShoppingListItem::getName)
                .collect(Collectors.toSet());

        List<ShoppingListItem> newAndNotAlreadyExistingItems = newItems.stream()
                .filter(i -> !alreadyExistingItems.contains(i))
                .map(i -> fromItemNameInShoppingList(i, shoppingList))
                .collect(Collectors.toList());

        if (alreadyExistingItems.size() >= itemsPerShoppingListLimit && newAndNotAlreadyExistingItems.size() > 0) {
            throw new ForbiddenExeption("Can't add more items; shopping list (" + shoppingListId + ") " +
                    "already has maximum allowed items (" + itemsPerShoppingListLimit + ")");
        }

        shoppingList.getItems().addAll(newAndNotAlreadyExistingItems);
        ebean.save(shoppingList);
    }

    @Override
    public void removeItems(Long userId, Long shoppingListId, List<String> itemsToRemove) {
        logger.info("removeItems(): userId = {}, shoppingListId = {}, itemsToRemove = {}",
                userId, shoppingListId, itemsToRemove);

        assertEntityExists(ebean, User.class, userId);
        assertEntityExists(ebean, ShoppingList.class, shoppingListId);

        ShoppingList shoppingList = findShoppingListOfUser(userId, shoppingListId);
        Set<String> itemsToKeep = shoppingList.getItems().stream()
                .map(ShoppingListItem::getName)
                .collect(Collectors.toSet());

        itemsToRemove.forEach(itemToRemove -> {
            if (!itemsToKeep.contains(itemToRemove)) {
                throw new NotFoundException("Not found item to remove: " + itemToRemove + " in shopping list: " + shoppingListId);
            }

            itemsToKeep.remove(itemToRemove);
        });

        List<ShoppingListItem> itemsToDelete = shoppingList.getItems().stream()
                .filter(item -> !itemsToKeep.contains(item.getName()))
                .collect(Collectors.toList());
        ebean.deleteAll(itemsToDelete);
    }

    @Override
    public void completeAnItem(Long userId, Long shoppingListId, String item) {
        logger.info("completeAnItem(): userId = {}, shoppingListId = {}, item = {}",
                userId, shoppingListId, item);

        ShoppingListItem itemToComplete = findItemOfShoppingListOfUserWithName(userId, shoppingListId, item);

        itemToComplete.setCompleted(true);
        ebean.save(itemToComplete);
    }

    @Override
    public void undoAnItem(Long userId, Long shoppingListId, String item) {
        logger.info("undoAnItem(): userId = {}, shoppingListId = {}, item = {}",
                userId, shoppingListId, item);

        ShoppingListItem itemToUndo = findItemOfShoppingListOfUserWithName(userId, shoppingListId, item);

        itemToUndo.setCompleted(false);
        ebean.save(itemToUndo);
    }

    private static ShoppingListItem fromItemNameInShoppingList(String name, ShoppingList shoppingList) {
        ShoppingListItem item = new ShoppingListItem();
        item.setName(name);
        item.setShoppingList(shoppingList);

        return item;
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

    private ShoppingListItem findItemOfShoppingListOfUserWithName(Long userId, Long shoppingListId, String item) {
        assertEntityExists(ebean, User.class, userId);
        assertEntityExists(ebean, ShoppingList.class, shoppingListId);

        ShoppingList shoppingList = findShoppingListOfUser(userId, shoppingListId);

        Optional<ShoppingListItem> itemMaybe = shoppingList.getItems().stream()
                .filter(i -> i.getName().equals(item))
                .findFirst();

        if (!itemMaybe.isPresent()) {
            throw new NotFoundException("Not found item: " + item + " in shopping list.");
        }

        return itemMaybe.get();
    }

    private int countAUsersShoppingLists(Long userId) {
        return ebean.createQuery(ShoppingList.class)
                .where()
                .eq("user.id", userId)
                .findCount();
    }
}
