package services;

import com.typesafe.config.Config;
import data.entities.RecipeBook;
import data.repositories.RecipeBookRepository;
import data.repositories.exceptions.ForbiddenExeption;
import dto.RecipeBookCreateUpdateDto;
import lombokized.dto.RecipeBookDto;
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

    private static final Logger.ALogger logger = Logger.of(RecipeBooksService.class);

    @Inject
    public RecipeBooksService(Config config) {
        maxPerUser = config.getInt("receptnekem.recipebooks.maxperuser");
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
}
