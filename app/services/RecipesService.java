package services;

import com.typesafe.config.Config;
import queryparams.RecipesQueryParams;
import data.entities.Recipe;
import data.repositories.RecipeRepository;
import lombokized.dto.PageDto;
import lombokized.dto.RecipeDto;
import lombokized.repositories.Page;

import javax.inject.Inject;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static services.RecipesQueryParamsMapping.*;
import static lombokized.repositories.RecipeRepositoryParams.*;

public class RecipesService {
    @Inject
    private RecipeRepository repository;

    @Inject
    private Config config;

    public CompletionStage<PageDto<RecipeDto>> pageOfQueryTypeNumber(RecipesQueryParams.Params params) {
        QueryTypeNumber query = toQueryTypeNumber(params);
        return repository.pageOfQueryTypeNumber(query)
                .thenApplyAsync(p -> toPageDto(p, params.languageId));
    }

    public CompletionStage<PageDto<RecipeDto>> pageOfQueryTypeRatio(RecipesQueryParams.Params params) {
        QueryTypeRatio query = toQueryTypeRatio(params);
        return repository.pageOfQueryTypeRatio(query)
                .thenApplyAsync(p -> toPageDto(p, params.languageId));
    }

    public CompletionStage<PageDto<RecipeDto>> pageOfQueryTypeNone(RecipesQueryParams.Params params) {
        Common query = toCommon(params);
        return repository.pageOfQueryTypeNone(query)
                .thenApplyAsync(p -> toPageDto(p, params.languageId));
    }

    public CompletionStage<RecipeDto> single(Long id, Long languageId) {
        return repository.single(id)
                .thenApplyAsync(e -> DtoMapper.toDto(e, languageId));
    }

    private PageDto<RecipeDto> toPageDto(Page<Recipe> page, Long languageId) {
        Long usedLanguageId = getLanguageIdOrDefault(languageId);
        List<RecipeDto> dtos = page.getItems()
                .stream()
                .map(entity -> DtoMapper.toDto(entity, usedLanguageId))
                .collect(Collectors.toList());
        return new PageDto<>(dtos, page.getTotalCount());
    }

    private Long getLanguageIdOrDefault(Long id) {
        if (id == null) {
            return getDefaultLanguageId();
        }

        if (id == 0L) {
            return getDefaultLanguageId();
        }

        return id;
    }

    private Long getDefaultLanguageId() {
        return config.getLong("openrecipes.default.languageid");
    }
}
