package services;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import data.DatabaseExecutionContext;
import data.entities.*;
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

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public class IngredientTagsService {
    private IngredientTagRepository repository;
    private IngredientNameRepository ingredientNameRepository;
    private LanguageService languageService;
    private DatabaseExecutionContext dbExecContext;
    private int maxPerUser;

    private static final Logger.ALogger logger = Logger.of(IngredientTagsService.class);

    @Inject
    public IngredientTagsService(IngredientTagRepository repository, IngredientNameRepository ingredientNameRepository, LanguageService languageService, Config config, DatabaseExecutionContext dbExecContext) {
        this.repository = repository;
        this.ingredientNameRepository = ingredientNameRepository;
        this.dbExecContext = dbExecContext;
        this.languageService = languageService;
        this.maxPerUser = config.getInt("cooksm.art.userdefinedtags.maxperuser");
    }

    public CompletionStage<Page<IngredientTagDto>> page(IngredientTagQueryParams queryParams) {
        logger.info("page(): queryParams = {}", queryParams);
        IngredientTagRepositoryParams.Page repositoryParams = toPageParams(queryParams);

        return supplyAsync(() -> {
            Page<IngredientTag> ingredientTagPage = repository.page(repositoryParams);
            return toPageDto(ingredientTagPage, queryParams.getLanguageId());
        }, dbExecContext);
    }

    public CompletionStage<Page<IngredientTagDto>> page(IngredientTagQueryParams queryParams, Long userId) {
        logger.info("page(): queryParams = {}, userId = {}", queryParams, userId);
        return supplyAsync(() -> {
            IngredientTagRepositoryParams.Page repositoryParams = toPageParams(queryParams, userId);
            Page<IngredientTag> ingredientTagPage = repository.page(repositoryParams);
            return toPageDto(ingredientTagPage, queryParams.getLanguageId());
        }, dbExecContext);
    }

    public CompletionStage<Long> create(IngredientTagCreateUpdateDto dto, Long userId) {
        logger.info("create(): userId = {}, dto = {}", userId, dto);
        return supplyAsync(() -> {
            Integer numOfTagsOfUser = repository.count(userId);

            checkTagCountLimitReached(numOfTagsOfUser);
            checkTagWithNameExists(userId, dto.name, dto.languageId);

            List<Long> uniqueIngredientIds = new ArrayList<>(new HashSet<>(dto.ingredientIds));
            checkAllIngredientIdsExist(uniqueIngredientIds);

            IngredientTag createdIngredientTag = repository.create(userId, dto.name, uniqueIngredientIds, dto.languageId);
            return createdIngredientTag.getId();

        }, dbExecContext);
    }

    public CompletionStage<IngredientTagResolvedDto> single(Long id, Long languageId, Long userId) {
        logger.info("single(): id = {}, languageId = {}, userId = {}", id, languageId, userId);
        return supplyAsync(() -> {
            IngredientTag ingredientTag = repository.byId(id, userId);
            List<IngredientName> ingredientNames = namesOfTag(ingredientTag, languageId);
            return createResolvedDto(ingredientTag, ingredientNames, languageId);
        }, dbExecContext);
    }

    public CompletionStage<Void> update(Long id, IngredientTagCreateUpdateDto dto, Long userId) {
        logger.info("update(): id = {}, userId = {}, dto = {}", id, userId, dto);
        return runAsync(() -> {
            List<Long> uniqueIngredientIds = new ArrayList<>(new HashSet<>(dto.ingredientIds));
            checkAllIngredientIdsExist(dto.ingredientIds);

            repository.update(id, userId, dto.name, uniqueIngredientIds, dto.languageId);
        }, dbExecContext);
    }

    public CompletionStage<Void> delete(Long id, Long userId) {
        logger.info("delete(): id = {}, userId = {}", id, userId);
        return runAsync(() -> {
            List<UserSearch> userSearches = repository.userSearchesOf(id, userId);
            if (userSearches != null && userSearches.size() > 0) {
                logger.warn("delete(): user defined tag is referenced by user search(es)!");
                throw createConlictingUserSearchesException(id, userSearches);
            }

            repository.delete(id, userId);
        }, dbExecContext);
    }

    public CompletionStage<List<IngredientTagDto>> userDefined(Long userId, Long languageId) {
        logger.info("userDefined(): userId = {}, languageId = {}", userId, languageId);
        return supplyAsync(() -> {
            List<IngredientTag> userDefinedIngredientTags = repository.userDefinedOnly(userId);
            return toDtoList(userDefinedIngredientTags, languageId);
        }, dbExecContext);
    }

    public CompletionStage<List<IngredientTagDto>> byIds(Long languageId, List<Long> ids) {
        logger.info("byIds(): langaugeId = {}, ids = {}", languageId, ids);
        return supplyAsync(() -> {
            List<IngredientTagName> tagNames = repository.byIds(languageId, ids);
            return tagNamesToDtoList(tagNames, languageId);
        }, dbExecContext);
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

    private Page<IngredientTagDto> toPageDto(Page<IngredientTag> page, Long languageId) {
        List<IngredientTagDto> items = toDtoList(page.getItems(), languageId);
        return new Page<>(items, page.getTotalCount());
    }

    private List<IngredientTagDto> toDtoList(List<IngredientTag> entities, Long languageId) {
        return entities.stream()
                .map(t -> DtoMapper.toDto(t, languageId))
                .collect(Collectors.toList());
    }

    private void checkTagCountLimitReached(int currentValue) {
        if (currentValue >= maxPerUser) {
            throw new ForbiddenExeption("User has reached the maximum tag number!");
        }
    }

    private void checkTagWithNameExists(Long userId, String name, Long languageId) {
        IngredientTag ingredientTag = repository.byNameOfUser(userId, name, languageId);
        if (ingredientTag != null) {
            throw new BusinessLogicViolationException("User has a tag with the same name with same language! name = " + name +
                    ", languageId = " + languageId);
        }
    }

    private void checkAllIngredientIdsExist(List<Long> ingredientIds) {
        ingredientNameRepository.byIngredientIds(ingredientIds, languageService.getDefault());
    }

    private List<Long> toIds(List<Ingredient> ingredients) {
        return ingredients.stream()
                .map(Ingredient::getId)
                .collect(Collectors.toList());
    }

    private List<IngredientName> namesOfTag(IngredientTag tag, Long languageId) {
        if (tag != null) {
            List<Long> ingredientIds = toIds(tag.getIngredients());
            return ingredientNameRepository
                    .byIngredientIds(ingredientIds, languageService.getLanguageIdOrDefault(languageId));
        }

        return null;
    }

    private IngredientTagResolvedDto createResolvedDto(IngredientTag tag, List<IngredientName> names, Long languageId) {
        if (tag == null) {
            throw new NotFoundException("Not found tag!");
        }

        List<IngredientNameDto> namesDto = names.stream()
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());

        Long usedLanguage = languageService.getLanguageIdOrDefault(languageId);
        return DtoMapper.toDto(tag, namesDto, usedLanguage);
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

    private List<IngredientTagDto> tagNamesToDtoList(List<IngredientTagName> tagNames, Long languageId) {
        List<IngredientTagDto> tagDtos = new ArrayList<>();
        tagNames.forEach(n -> {
            IngredientTagDto tagDto = DtoMapper.toDto(n.getTag(), languageId);
            tagDto = new IngredientTagDto(tagDto.getId(), n.getName(), tagDto.getIngredients());
            tagDtos.add(tagDto);
        });

        return tagDtos;
    }
}
