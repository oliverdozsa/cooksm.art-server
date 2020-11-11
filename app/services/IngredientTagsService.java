package services;

import com.typesafe.config.Config;
import data.entities.Ingredient;
import data.entities.IngredientName;
import data.entities.IngredientTag;
import data.repositories.IngredientNameRepository;
import data.repositories.IngredientTagRepository;
import data.repositories.exceptions.BusinessLogicViolationException;
import data.repositories.exceptions.ForbiddenExeption;
import data.repositories.exceptions.NotFoundException;
import dto.IngredientTagCreateUpdateDto;
import lombokized.dto.IngredientNameDto;
import lombokized.dto.IngredientTagDto;
import lombokized.dto.IngredientTagResolvedDto;
import lombokized.queryparams.IngredientTagQueryParams;
import lombokized.repositories.IngredientTagRepositoryParams;
import lombokized.repositories.Page;

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

    @Inject
    public IngredientTagsService(IngredientTagRepository repository, IngredientNameRepository ingredientNameRepository, LanguageService languageService, Config config) {
        this.repository = repository;
        this.ingredientNameRepository = ingredientNameRepository;
        this.languageService = languageService;
        this.maxPerUser = config.getInt("receptnekem.userdefinedtags.maxperuser");
    }

    public CompletionStage<Page<IngredientTagDto>> page(IngredientTagQueryParams queryParams) {
        IngredientTagRepositoryParams.Page repositoryParams = toPageParams(queryParams);
        return repository.page(repositoryParams)
                .thenApplyAsync(this::toPageDto);
    }

    public CompletionStage<Page<IngredientTagDto>> page(IngredientTagQueryParams queryParams, Long userId) {
        IngredientTagRepositoryParams.Page repositoryParams = toPageParams(queryParams, userId);
        return repository.page(repositoryParams)
                .thenApplyAsync(this::toPageDto);
    }

    public CompletionStage<Long> create(IngredientTagCreateUpdateDto dto, Long userId) {
        List<Long> uniqueIngredientIds = new ArrayList<>(new HashSet<>(dto.ingredientIds));

        return repository.count(userId)
                .thenAcceptAsync(this::checkTagCountLimitReached)
                .thenComposeAsync(v -> checkTagWithNameExists(userId, dto.name))
                .thenComposeAsync(v -> checkAllIngredientIdsExist(uniqueIngredientIds))
                .thenComposeAsync(v -> repository.create(userId, dto.name, uniqueIngredientIds, languageService.getDefault()))
                .thenApplyAsync(IngredientTag::getId);
    }

    public CompletionStage<IngredientTagResolvedDto> single(Long id, Long languageId, Long userId) {
        CompletionStage<IngredientTag> tagCompletionStage = repository.byId(id, userId);
        CompletionStage<List<IngredientName>> ingredientNamesDtoCompletionStage =
                tagCompletionStage.thenComposeAsync(tag -> {
                    if(tag != null) {
                        List<Long> ingredientIds = toIds(tag.getIngredients());
                        return ingredientNameRepository
                                .byIngredientIds(ingredientIds, languageService.getLanguageIdOrDefault(languageId));
                    }

                    return null;
                });

        return tagCompletionStage
                .thenCombineAsync(ingredientNamesDtoCompletionStage, (tag, names) -> {
                    if(tag == null) {
                        throw new NotFoundException("Not found tag with id =" + id + ", userId = " + userId);
                    }

                    List<IngredientNameDto> namesDto = names.stream()
                            .map(DtoMapper::toDto)
                            .collect(Collectors.toList());

                    return DtoMapper.toDto(tag, namesDto);
                });
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

    private CompletionStage<List<IngredientName>> checkAllIngredientIdsExist(List<Long> ingredientIds) {
        return ingredientNameRepository.byIngredientIds(ingredientIds, languageService.getDefault());
    }

    private List<Long> toIds(List<Ingredient> ingredients) {
        return ingredients.stream()
                .map(Ingredient::getId)
                .collect(Collectors.toList());
    }
}
