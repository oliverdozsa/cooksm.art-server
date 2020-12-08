package services;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import data.entities.Ingredient;
import data.entities.IngredientName;
import data.entities.IngredientTag;
import data.entities.UserSearch;
import data.repositories.IngredientNameRepository;
import data.repositories.IngredientTagRepository;
import data.repositories.exceptions.BusinessLogicViolationException;
import data.repositories.exceptions.ForbiddenExeption;
import data.repositories.exceptions.NotFoundException;
import dto.IngredientTagCreateUpdateDto;
import lombokized.dto.IngredientNameDto;
import lombokized.dto.IngredientTagDto;
import lombokized.dto.IngredientTagResolvedDto;
import lombokized.dto.UserSearchDto;
import lombokized.queryparams.IngredientTagQueryParams;
import lombokized.repositories.IngredientTagRepositoryParams;
import lombokized.repositories.Page;
import play.Logger;
import play.libs.Json;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class IngredientTagsService {
    private IngredientTagRepository repository;
    private IngredientNameRepository ingredientNameRepository;
    private LanguageService languageService;
    private int maxPerUser;

    private static final Logger.ALogger logger = Logger.of(IngredientTagsService.class);

    @Inject
    public IngredientTagsService(IngredientTagRepository repository, IngredientNameRepository ingredientNameRepository, LanguageService languageService, Config config) {
        this.repository = repository;
        this.ingredientNameRepository = ingredientNameRepository;
        this.languageService = languageService;
        this.maxPerUser = config.getInt("receptnekem.userdefinedtags.maxperuser");
    }

    public CompletionStage<Page<IngredientTagDto>> page(IngredientTagQueryParams queryParams) {
        logger.info("page(): queryParams = {}", queryParams);
        IngredientTagRepositoryParams.Page repositoryParams = toPageParams(queryParams);
        return repository.page(repositoryParams)
                .thenApplyAsync(this::toPageDto);
    }

    public CompletionStage<Page<IngredientTagDto>> page(IngredientTagQueryParams queryParams, Long userId) {
        logger.info("page(): queryParams = {}, userId = {}", queryParams, userId);
        IngredientTagRepositoryParams.Page repositoryParams = toPageParams(queryParams, userId);
        return repository.page(repositoryParams)
                .thenApplyAsync(this::toPageDto);
    }

    public CompletionStage<Long> create(IngredientTagCreateUpdateDto dto, Long userId) {
        logger.info("create(): userId = {}, dto = {}", userId, dto);
        List<Long> uniqueIngredientIds = new ArrayList<>(new HashSet<>(dto.ingredientIds));

        return repository.count(userId)
                .thenAcceptAsync(this::checkTagCountLimitReached)
                .thenComposeAsync(v -> checkTagWithNameExists(userId, dto.name))
                .thenComposeAsync(v -> checkAllIngredientIdsExist(uniqueIngredientIds))
                .thenComposeAsync(v -> repository.create(userId, dto.name, uniqueIngredientIds, languageService.getDefault()))
                .thenApplyAsync(IngredientTag::getId);
    }

    public CompletionStage<IngredientTagResolvedDto> single(Long id, Long languageId, Long userId) {
        logger.info("single(): id = {}, languageId = {}, userId = {}", id, languageId, userId);
        CompletionStage<IngredientTag> tagCompletionStage = repository.byId(id, userId);
        CompletionStage<List<IngredientName>> ingredientNamesDtoCompletionStage =
                tagCompletionStage.thenComposeAsync(tag -> namesOfTag(tag, languageId));

        return tagCompletionStage
                .thenCombineAsync(ingredientNamesDtoCompletionStage, this::createResolvedDto);
    }

    public CompletionStage<Void> update(Long id, IngredientTagCreateUpdateDto dto, Long userId) {
        logger.info("update(): id = {}, userId = {}, dto = {}", id, userId, dto);
        List<Long> uniqueIngredientIds = new ArrayList<>(new HashSet<>(dto.ingredientIds));

        return checkAllIngredientIdsExist(dto.ingredientIds)
                .thenComposeAsync(v -> repository.update(id, userId, dto.name, uniqueIngredientIds, languageService.getDefault()));
    }

    public CompletionStage<Void> delete(Long id, Long userId) {
        logger.info("delete(): id = {}, userId = {}", id, userId);
        return repository.userSearchesOf(id, userId)
                .thenAcceptAsync(list -> {
                    if (list != null && list.size() > 0) {
                        logger.warn("delete(): user defined tag is referenced by user search(es)!");
                        throw createConlictingUserSearchesException(id, list);
                    }
                })
                .thenComposeAsync(v -> repository.delete(id, userId));
    }

    private IngredientTagRepositoryParams.Page toPageParams(IngredientTagQueryParams queryParams) {
        IngredientTagRepositoryParams.Page.Builder builder = IngredientTagRepositoryParams.Page.builder();
        builder.nameLike(queryParams.getNameLike());
        builder.languageId(queryParams.getLanguageId());
        builder.limit(queryParams.getLimit());
        builder.offset(queryParams.getOffset());

        return builder.build();
    }

    private IngredientTagRepositoryParams.Page toPageParams(IngredientTagQueryParams queryParams, Long userId) {
        IngredientTagRepositoryParams.Page.Builder builder = toPageParams(queryParams).toBuilder();
        builder.userId(userId);
        return builder.build();
    }

    private Page<IngredientTagDto> toPageDto(Page<IngredientTag> page) {
        List<IngredientTagDto> items = page.getItems().stream()
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());

        return new Page<>(items, page.getTotalCount());
    }

    private void checkTagCountLimitReached(int currentValue) {
        if (currentValue >= maxPerUser) {
            throw new ForbiddenExeption("User has reached the maximum tag number!");
        }
    }

    private CompletionStage<Void> checkTagWithNameExists(Long userId, String name) {
        return repository.byNameOfUser(userId, name)
                .thenAcceptAsync(t -> {
                    if (t != null) {
                        throw new BusinessLogicViolationException("User has a tag with the same name! name = " + name);
                    }
                });
    }

    private CompletionStage<Void> checkAllIngredientIdsExist(List<Long> ingredientIds) {
        return ingredientNameRepository.byIngredientIds(ingredientIds, languageService.getDefault())
                .thenRunAsync(() -> {
                });
    }

    private List<Long> toIds(List<Ingredient> ingredients) {
        return ingredients.stream()
                .map(Ingredient::getId)
                .collect(Collectors.toList());
    }

    private CompletionStage<List<IngredientName>> namesOfTag(IngredientTag tag, Long languageId) {
        if (tag != null) {
            List<Long> ingredientIds = toIds(tag.getIngredients());
            return ingredientNameRepository
                    .byIngredientIds(ingredientIds, languageService.getLanguageIdOrDefault(languageId));
        }

        return null;
    }

    private IngredientTagResolvedDto createResolvedDto(IngredientTag tag, List<IngredientName> names) {
        if (tag == null) {
            throw new NotFoundException("Not found tag!");
        }

        List<IngredientNameDto> namesDto = names.stream()
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());

        return DtoMapper.toDto(tag, namesDto);
    }

    private BusinessLogicViolationException createConlictingUserSearchesException(Long id, List<UserSearch> searches) {
        ObjectNode json = Json.newObject();
        List<UserSearchDto> searchDtos = searches.stream()
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());
        json.set("usersearches", Json.toJson(searchDtos));

        return new BusinessLogicViolationException(json,
                "There are user searches containing the user defined tag: " + id + "!");
    }
}
