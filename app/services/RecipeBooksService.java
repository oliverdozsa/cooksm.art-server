package services;

import com.typesafe.config.Config;
import data.DatabaseExecutionContext;
import data.entities.RecipeBook;
import data.repositories.RecipeBookRepository;
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

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public class RecipeBooksService {
    @Inject
    private RecipeBookRepository repository;

    @Inject
    private DatabaseExecutionContext dbExecContext;

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
        return supplyAsync(() -> {
            Optional<RecipeBook> recipeBookOptional = repository.byNameOfUser(userId, dto.name);
            checkNameExists(recipeBookOptional.isPresent(), dto.name, userId);

            Integer numOfRecipeBooksOfUser = repository.countOf(userId);
            checkCount(numOfRecipeBooksOfUser, userId);

            RecipeBook createdRecipeBook = repository.create(dto.name, userId);
            return createdRecipeBook.getId();
        }, dbExecContext);
    }

    public CompletionStage<List<RecipeBookDto>> all(Long user) {
        logger.info("all(): user = {}", user);
        return supplyAsync(() -> {
            List<RecipeBook> recipeBooks = repository.allOf(user);
            return toDtoList(recipeBooks);
        }, dbExecContext);
    }

    public CompletionStage<RecipeBookDto> single(Long user, Long id) {
        logger.info("single(): user = {}, id = {}", user, id);
        return supplyAsync(() -> {
            RecipeBook recipeBook = repository.single(id, user);
            return DtoMapper.toDto(recipeBook);
        }, dbExecContext);
    }

    public CompletionStage<Void> update(Long userId, Long id, RecipeBookCreateUpdateDto dto) {
        logger.info("update(): userId = {}, id = {}, dto = {}", userId, id, dto);
        return runAsync(() -> {
            Optional<RecipeBook> recipeBookOptional = repository.byNameOfUser(userId, dto.name);
            checkNameToUpdate(recipeBookOptional, dto.name, userId, id);

            repository.update(id, dto.name, userId);
        }, dbExecContext);
    }

    public CompletionStage<Void> delete(Long userId, Long id) {
        logger.info("delete(): userId = {}, id = {}", userId, id);
        return runAsync(() -> {
            repository.delete(id, userId);
        }, dbExecContext);
    }

    public CompletionStage<Void> addRecipes(Long userId, Long id, RecipeBookRecipesCreateUpdateDto dto) {
        logger.info("addRecipes(): userId = {}, id = {}, dto = {}", userId, id, dto);
        return runAsync(() -> {
            Integer futureCountOfRecipeBooks = repository.futureCountOf(id, userId, dto.recipeIds);
            checkFutureRecipeCount(futureCountOfRecipeBooks);

            repository.addRecipes(id, userId, dto.recipeIds);
        }, dbExecContext);
    }

    public CompletionStage<RecipeBookWithRecipesDto> recipesOf(Long userId, Long id) {
        logger.info("recipesOf(): userId = {}, id = {}", userId, id);
        return supplyAsync(() -> {
            RecipeBook recipeBook = repository.single(id, userId);
            return DtoMapper.toRecipeBookWithRecipesDto(recipeBook);
        }, dbExecContext);
    }

    public CompletionStage<Void> updateRecipes(Long userId, Long id, RecipeBookRecipesCreateUpdateDto dto) {
        logger.info("updateRecipes(): userId = {}, id = {}, dto = {}", userId, id, dto);
        return runAsync(() -> {
            repository.updateRecipes(id, userId, dto.recipeIds);
        }, dbExecContext);
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
