package services;

import com.typesafe.config.Config;
import data.DatabaseExecutionContext;
import data.entities.RecipeSearch;
import data.entities.UserSearch;
import data.repositories.RecipeBookRepository;
import data.repositories.UserSearchRepository;
import data.repositories.exceptions.BusinessLogicViolationException;
import data.repositories.exceptions.ForbiddenExeption;
import data.repositories.exceptions.NotFoundException;
import dto.UserSearchCreateUpdateDto;
import lombokized.dto.UserSearchDto;
import play.Logger;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public class UserSearchService {
    private UserSearchRepository userSearchRepository;
    private RecipeSearchService recipeSearchService;
    private Integer maxPerUser;
    private RecipeBookRepository recipeBookRepository;
    private DatabaseExecutionContext dbExecContext;

    private static final Logger.ALogger logger = Logger.of(UserSearchService.class);

    @Inject
    public UserSearchService(UserSearchRepository userSearchRepository, RecipeSearchService recipeSearchService, Config config,
                             RecipeBookRepository recipeBookRepository, DatabaseExecutionContext dbExecContext) {
        this.userSearchRepository = userSearchRepository;
        this.recipeSearchService = recipeSearchService;
        this.recipeBookRepository = recipeBookRepository;
        this.dbExecContext = dbExecContext;
        maxPerUser = config.getInt("receptnekem.usersearches.maxperuser");
    }

    public CompletionStage<List<UserSearchDto>> all(Long userId) {
        logger.info("all(): userId = {}", userId);
        return supplyAsync(() -> {
            List<UserSearch> userSearches = userSearchRepository.all(userId);
            return userSearches.stream()
                    .map(DtoMapper::toDto)
                    .collect(Collectors.toList());
        }, dbExecContext);
    }

    public CompletionStage<Long> create(UserSearchCreateUpdateDto dto, Long userId) {
        logger.info("create(): userId = {}, dto = {}", userId, dto);
        return runAsync(() -> {
            Integer userSearchCountOfUser = userSearchRepository.count(userId);
            if (userSearchCountOfUser >= maxPerUser) {
                throw new ForbiddenExeption("User reached max limit! userId = " + userId);
            }

            checkRecipeBooks(dto, userId);
        }, dbExecContext)
                .thenCompose(v -> recipeSearchService.createWithLongId(dto.query, true))
                .thenApplyAsync(searchId -> {
                    UserSearch userSearch = userSearchRepository.create(dto.name, userId, searchId);
                    return userSearch.getId();
                }, dbExecContext);

    }

    public CompletionStage<UserSearchDto> single(Long id, Long userId) {
        logger.info("single(): id = {}, userId = {}", id, userId);
        return supplyAsync(() -> {
            UserSearch userSearch = userSearchRepository.single(id, userId);
            return DtoMapper.toDto(userSearch);
        }, dbExecContext);
    }

    public CompletionStage<Boolean> delete(Long id, Long userId) {
        logger.info("delete(): id = {}, userId = {}", id, userId);
        return supplyAsync(() -> {
            return userSearchRepository.delete(id, userId);
        }, dbExecContext);
    }

    public CompletionStage<Void> patch(Long id, Long userId, UserSearchCreateUpdateDto dto) {
        logger.info("patch(): id = {}, userId = {}, dto = {}", id, userId, dto);
        return runAsync(() -> {
            UserSearch userSearch;
            if (dto.name != null) {
                userSearch = userSearchRepository.update(dto.name, userId, id);
            } else {
                userSearch = userSearchRepository.single(id, userId);
            }

            Long searchId = userSearch.getSearch().getId();
            if (dto.query != null) {
                recipeSearchService.update(dto.query, true, searchId);
            }
        }, dbExecContext);
    }

    private void checkRecipeBooks(UserSearchCreateUpdateDto dto, Long userId) {
        if (dto.query.recipeBooks != null && dto.query.recipeBooks.size() > 0) {
            try {
                recipeBookRepository.checkRecipeBooksOfUser(dto.query.recipeBooks, userId);
            } catch (NotFoundException e) {
                handleRecipeBookNotFoundException(e);
            }
        }
    }

    private void handleRecipeBookNotFoundException(NotFoundException e) {
        throw new BusinessLogicViolationException(e.getMessage());
    }
}
