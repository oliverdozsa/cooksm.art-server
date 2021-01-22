package services;

import com.typesafe.config.Config;
import data.entities.RecipeBook;
import data.repositories.RecipeBookRepository;
import data.repositories.exceptions.BusinessLogicViolationException;
import data.repositories.exceptions.ForbiddenExeption;
import dto.RecipeBookCreateUpdateDto;
import dto.RecipeBookRecipesCreateUpdateDto;
import lombokized.dto.RecipeBookDto;
import lombokized.dto.RecipeBookWithRecipesDto;
import play.Logger;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class RecipeBooksService {
    @Inject
    private RecipeBookRepository repository;

    private int maxPerUser;
    private int maxRecipesPerBook;

    private static final Logger.ALogger logger = Logger.of(RecipeBooksService.class);

    @Inject
    public RecipeBooksService(Config config) {
        maxPerUser = config.getInt("receptnekem.recipebooks.maxperuser");
        maxRecipesPerBook = config.getInt("receptnekem.recipebooks.maxrecipesperbook");
    }

    public CompletionStage<Long> create(RecipeBookCreateUpdateDto dto, Long userId) {
        logger.info("create(): user = {}, dto = {}", userId, dto);
        return repository
                .byNameOfUser(userId, dto.name)
                .thenAcceptAsync(o -> checkNameExists(o.isPresent(), dto.name, userId))
                .thenComposeAsync(v -> repository.countOf(userId))
                .thenAcceptAsync(c -> checkCount(c, userId))
                .thenComposeAsync(v -> repository.create(dto.name, userId))
                .thenApplyAsync(RecipeBook::getId);
    }

    public CompletionStage<List<RecipeBookDto>> all(Long user) {
        logger.info("all(): user = {}", user);

        return repository.allOf(user)
                .thenApplyAsync(this::toDtoList);
    }

    public CompletionStage<RecipeBookDto> single(Long user, Long id) {
        logger.info("single(): user = {}, id = {}", user, id);
        return repository.single(id, user)
                .thenApplyAsync(DtoMapper::toDto);
    }

    public CompletionStage<Void> update(Long userId, Long id, RecipeBookCreateUpdateDto dto) {
        logger.info("update(): userId = {}, id = {}, dto = {}", userId, id, dto);
        return repository
                .byNameOfUser(userId, dto.name)
                .thenAcceptAsync(o -> checkNameToUpdate(o, dto.name, userId, id))
                .thenComposeAsync(v -> repository.update(id, dto.name, userId));
    }

    public CompletionStage<Void> delete(Long userId, Long id) {
        logger.info("delete(): userId = {}, id = {}", userId, id);
        return repository.delete(id, userId);
    }

    public CompletionStage<Void> addRecipes(Long userId, Long id, RecipeBookRecipesCreateUpdateDto dto) {
        logger.info("addRecipes(): userId = {}, id = {}, dto = {}", userId, id, dto);
        return repository
                .futureCountOf(id, userId, dto.recipeIds)
                .thenAcceptAsync(this::checkFutureRecipeCount)
                .thenComposeAsync(v -> repository.addRecipes(id, userId, dto.recipeIds));
    }

    public CompletionStage<RecipeBookWithRecipesDto> recipesOf(Long userId, Long id) {
        logger.info("recipesOf(): userId = {}, id = {}", userId, id);
        return repository.single(id, userId)
                .thenApplyAsync(DtoMapper::toRecipeBookWithRecipesDto);
    }

    private void checkCount(int count, Long user) {
        if (count >= maxPerUser) {
            throw new ForbiddenExeption("User reached max limit! user = " + user);
        }
    }

    private void checkNameExists(boolean doesExist, String name, Long userId) {
        if (doesExist) {
            throw new ForbiddenExeption("Recipe book with name already exists! name = " + name + ", userId = " + userId);
        }
    }

    private void checkNameToUpdate(Optional<RecipeBook> recipeBookOptional, String name, Long userId, Long updateId) {
        boolean doesNameExistForOtherEntity = recipeBookOptional.isPresent() &&
                !updateId.equals(recipeBookOptional.get().getId());
        if (doesNameExistForOtherEntity) {
            throw new ForbiddenExeption("Other recipe book with name already exists! name = " + name + ", userId = " + userId);
        }
    }

    private List<RecipeBookDto> toDtoList(List<RecipeBook> entities) {
        return entities.stream()
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());
    }

    private void checkFutureRecipeCount(int count) {
        logger.info("checkFutureRecipeCount(): count = {}", count);
        if(count > maxRecipesPerBook) {
            throw new ForbiddenExeption("Max recipes per book would be violated! count = " + count);
        }
    }
}
