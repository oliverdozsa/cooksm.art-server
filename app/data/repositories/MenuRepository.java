package data.repositories;

import data.entities.Menu;
import dto.MenuCreateUpdateDto;

import java.util.List;

public interface MenuRepository {
    Menu create(MenuCreateUpdateDto request, Long userId);
    void delete(Long menuId, Long userId);
    void update(Long menuId, MenuCreateUpdateDto menu, Long userId);
    Menu single(Long id, Long userId);
    List<Menu> allOf(Long userId);
}
