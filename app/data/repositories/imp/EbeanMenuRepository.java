package data.repositories.imp;

import data.entities.Menu;
import data.entities.MenuItem;
import data.entities.Recipe;
import data.entities.User;
import data.repositories.MenuRepository;
import data.repositories.exceptions.NotFoundException;
import dto.MenuCreateUpdateDto;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import play.Logger;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static data.repositories.imp.EbeanRepoUtils.assertEntitiesExist;
import static data.repositories.imp.EbeanRepoUtils.assertEntityExists;

public class EbeanMenuRepository implements MenuRepository {
    private EbeanServer ebean;

    private static final Logger.ALogger logger = Logger.of(EbeanMenuRepository.class);

    @Inject
    public EbeanMenuRepository(EbeanServer ebean) {
        this.ebean = ebean;
    }

    @Override
    public Menu create(MenuCreateUpdateDto request, Long userId) {
        logger.info("create(): request = {}, userId = {}", request, userId);

        Set<Long> recipeIds = request.items.stream()
                .map(i -> i.recipeId)
                .collect(Collectors.toSet());
        assertEntityExists(ebean, User.class, userId);
        assertEntitiesExist(ebean, Recipe.class, "id", recipeIds);

        Menu menu = new Menu();
        menu.setUser(ebean.find(User.class, userId));
        populateMenuFrom(request, menu);

        ebean.save(menu);
        return menu;
    }

    @Override
    public void delete(Long menuId, Long userId) {
        logger.info("delete(): menuId = {}, userId = {}", menuId, userId);
        Menu menu = getOrNotFoundMenuFor(menuId, userId);
        ebean.delete(menu);
    }

    @Override
    public void update(Long menuId, MenuCreateUpdateDto request, Long userId) {
        logger.info("update(): menuId = {}, request = {}, userId = {}", menuId, request, userId);
        Menu menu = getOrNotFoundMenuFor(menuId, userId);
        populateMenuFrom(request, menu);
        ebean.save(menu);
    }

    @Override
    public Menu single(Long id, Long userId) {
        logger.info("single(): id = {}, userId = {}", id, userId);
        return getOrNotFoundMenuFor(id, userId);
    }

    @Override
    public List<Menu> allOf(Long userId) {
        logger.info("allOf(): userId = {}", userId);

        return ebean.createQuery(Menu.class).where()
                .eq("user.id", userId)
                .findList();
    }

    private Menu getOrNotFoundMenuFor(Long menuId, Long userId) {
        Optional<Menu> menuOptional = ebean.createQuery(Menu.class)
                .where()
                .eq("id", menuId)
                .eq("user.id", userId)
                .findOneOrEmpty();

        if (menuOptional.isEmpty()) {
            throw new NotFoundException("Not found menu with id = " + menuId + " for user = " + userId);
        }

        return menuOptional.get();
    }

    private void populateMenuFrom(MenuCreateUpdateDto request, Menu menu) {
        menu.setName(request.name);

        request.items.forEach(requestItem -> {
            MenuItem menuItem = new MenuItem();
            menuItem.setGroup(requestItem.group);
            menuItem.setOrder(requestItem.order);
            menuItem.setRecipe(Ebean.find(Recipe.class, requestItem.recipeId));

            menu.getMenuItems().add(menuItem);
        });
    }
}
