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
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static services.RecipesQueryParamsMapping.*;
import static lombokized.repositories.RecipeRepositoryParams.*;

public class RecipesService {
    @Inject
    private RecipeRepository repository;

    @Inject
    private Config config;

    @Inject
    private LanguageService languageService;

    public CompletionStage<PageDto<RecipeDto>> pageOfQueryTypeNumber(RecipesQueryParams.Params params) {
        return supplyAsync(supplyQueryTypeNumber(params))
                .thenCompose(q -> repository.pageOfQueryTypeNumber(q))
                .thenApplyAsync(p -> toPageDto(p, params.languageId));
    }

    public CompletionStage<PageDto<RecipeDto>> pageOfQueryTypeRatio(RecipesQueryParams.Params params) {
        return supplyAsync(supplyQueryTypeRatio(params))
                .thenCompose(q -> repository.pageOfQueryTypeRatio(q))
                .thenApplyAsync(p -> toPageDto(p, params.languageId));
    }

    public CompletionStage<PageDto<RecipeDto>> pageOfQueryTypeNone(RecipesQueryParams.Params params) {
        return supplyAsync(() -> toCommon(params))
                .thenCompose(p -> repository.pageOfQueryTypeNone(p))
                .thenApplyAsync(p -> toPageDto(p, params.languageId));
    }

    public CompletionStage<RecipeDto> single(Long id, Long languageId) {
        return repository.single(id)
                .thenApplyAsync(e -> {
                    if (e != null) {
                        Long usedLanguageId = languageService.getLanguageIdOrDefault(languageId);
                        return DtoMapper.toDto(e, usedLanguageId);
                    } else {
                        return null;
                    }
                });
    }

    private PageDto<RecipeDto> toPageDto(Page<Recipe> page, Long languageId) {
        Long usedLanguageId = languageService.getLanguageIdOrDefault(languageId);
        List<RecipeDto> dtos = page.getItems()
                .stream()
                .map(entity -> DtoMapper.toDto(entity, usedLanguageId))
                .collect(Collectors.toList());
        return new PageDto<>(dtos, page.getTotalCount());
    }

    private Supplier<QueryTypeNumber> supplyQueryTypeNumber(RecipesQueryParams.Params params) {
        return () -> {
            QueryTypeNumber query = toQueryTypeNumber(params);
            RecipeRepositoryQueryCheck.check(query);
            return query;
        };
    }

    private Supplier<QueryTypeRatio> supplyQueryTypeRatio(RecipesQueryParams.Params params) {
        return () -> {
            QueryTypeRatio query = toQueryTypeRatio(params);
            RecipeRepositoryQueryCheck.check(query);
            return query;
        };
    }
}
