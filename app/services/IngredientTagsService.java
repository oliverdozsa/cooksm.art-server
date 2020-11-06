package services;

import data.entities.IngredientTag;
import data.repositories.IngredientTagRepository;
import lombokized.dto.IngredientTagDto;
import lombokized.queryparams.IngredientTagQueryParams;
import lombokized.repositories.IngredientTagRepositoryParams;
import lombokized.repositories.Page;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class IngredientTagsService {
    @Inject
    private IngredientTagRepository repository;

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
}
