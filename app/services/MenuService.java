package services;

import com.typesafe.config.Config;
import data.DatabaseExecutionContext;
import data.repositories.MenuRepository;
import data.repositories.exceptions.BusinessLogicViolationException;
import dto.MenuCreateUpdateDto;
import lombokized.dto.MenuDto;
import lombokized.dto.MenuTitleDto;
import play.Logger;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public class MenuService {
    @Inject
    private MenuRepository repository;

    @Inject
    private DatabaseExecutionContext dbExecContext;

    @Inject
    private LanguageService languageService;

    private final int maxItemsPerMenu;

    private static final Logger.ALogger logger = Logger.of(MenuService.class);

    @Inject
    public MenuService(Config config) {
        maxItemsPerMenu = config.getInt("cooksm.art.menu.maxitems");
    }

    public CompletionStage<Long> create(MenuCreateUpdateDto menu, Long userId) {
        logger.info("create(): menu = {}, userId = {}", menu);
        return supplyAsync(() -> {
            if(menu.items.size() > maxItemsPerMenu) {
                String logMessage = "Too many items in menu. Max. allowed: " + maxItemsPerMenu;
                logger.warn(logMessage);
                throw new BusinessLogicViolationException(logMessage);
            }
            return repository.create(menu, userId).getId();
        }, dbExecContext);
    }

    public CompletionStage<Void> delete(Long menuId, Long userId) {
        logger.info("delete(): menuId = {}, userId = {}", menuId, userId);
        return runAsync(() -> repository.delete(menuId, userId), dbExecContext);
    }

    public CompletionStage<Void> update(Long menuId, MenuCreateUpdateDto menu, Long userId) {
        logger.info("update(): menuId = {}, menu = {}, userId = {}", menuId, menu, userId);
        return runAsync(() -> {
            if(menu.items.size() > maxItemsPerMenu) {
                String logMessage = "Too many items in menu. Max. allowed: " + maxItemsPerMenu;
                logger.warn(logMessage);
                throw new BusinessLogicViolationException(logMessage);
            }
            repository.update(menuId, menu, userId);
        }, dbExecContext);
    }

    public CompletionStage<MenuDto> single(Long menuId, Long languageId, Long userId) {
        logger.info("single(): menuId = {}, languageId = {}, userId = {}", menuId, userId);
        Long usedLanguageId = languageService.getLanguageIdOrDefault(languageId);
        return supplyAsync(() -> repository.single(menuId, userId), dbExecContext)
                .thenApplyAsync(m -> DtoMapper.toDto(m, usedLanguageId));
    }

    public CompletionStage<List<MenuTitleDto>> all(Long userId) {
        logger.info("all(): userId = {}", userId);
        return supplyAsync(() -> repository.allOf(userId), dbExecContext)
                .thenApplyAsync(DtoMapper::toMenuTitleDtos);
    }
}
