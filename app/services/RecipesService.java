package services;

import com.typesafe.config.Config;
import lombokized.repositories.RecipeRepositoryParams;
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

    @Inject
    private RecipeRepositoryQueryCheck queryCheck;

    public CompletionStage<PageDto<RecipeDto>> pageOfQueryTypeNumber(RecipesQueryParams.Params queryParams) {
        Supplier<QueryTypeNumber> repositoryParamsSupplier = () -> toQueryTypeNumber(queryParams);
        return pageOfQueryTypeNumber(repositoryParamsSupplier, queryParams.languageId);
    }

    public CompletionStage<PageDto<RecipeDto>> pageOfQueryTypeNumber(RecipesQueryParams.Params queryParams, Long userId) {
        Supplier<QueryTypeNumber> repositoryParamsSupplier = () -> toQueryTypeNumber(queryParams, userId);
        return pageOfQueryTypeNumber(repositoryParamsSupplier, queryParams.languageId);
    }

    public CompletionStage<PageDto<RecipeDto>> pageOfQueryTypeRatio(RecipesQueryParams.Params queryParams) {
        Supplier<QueryTypeRatio> repositoryParamsSupplier = () -> toQueryTypeRatio(queryParams);
        return pageOfQueryTypeRatio(repositoryParamsSupplier, queryParams.languageId);
    }

    public CompletionStage<PageDto<RecipeDto>> pageOfQueryTypeRatio(RecipesQueryParams.Params queryParams, Long userId) {
        Supplier<QueryTypeRatio> repositoryParamsSupplier = () -> toQueryTypeRatio(queryParams, userId);
        return pageOfQueryTypeRatio(repositoryParamsSupplier, queryParams.languageId);
    }

    public CompletionStage<PageDto<RecipeDto>> pageOfQueryTypeNone(RecipesQueryParams.Params queryParams) {
        CompletionStage<RecipeRepositoryParams.Common> repositoryParams = supplyAsync(() -> toCommon(queryParams));
        return queryRecipesByCommonParams(repositoryParams, queryParams.languageId);
    }

    public CompletionStage<PageDto<RecipeDto>> pageOfQueryTypeNone(RecipesQueryParams.Params queryParams, Long userId) {
        CompletionStage<RecipeRepositoryParams.Common> repositoryParams = supplyAsync(() -> toCommon(queryParams, userId));
        return queryRecipesByCommonParams(repositoryParams, queryParams.languageId);
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

    private CompletionStage<PageDto<RecipeDto>> pageOfQueryTypeRatio(Supplier<QueryTypeRatio> querySupplier, Long languageId) {
        Supplier<QueryTypeRatio> checkedSupplier = decorateQueryTypeRatioSupplierWithCheck(querySupplier);
        CompletionStage<QueryTypeRatio> repositoryParams = supplyAsync(checkedSupplier);
        return queryRecipesByQueryTypeRatio(repositoryParams, languageId);
    }

    private CompletionStage<PageDto<RecipeDto>> pageOfQueryTypeNumber(Supplier<QueryTypeNumber> querySupplier, Long languageId) {
        Supplier<QueryTypeNumber> checkedSupplier = decorateQueryTypeNumberSupplierWithCheck(querySupplier);
        CompletionStage<QueryTypeNumber> repositoryParams = supplyAsync(checkedSupplier);
        return queryRecipesByQueryTypeNumber(repositoryParams, languageId);
    }

    private PageDto<RecipeDto> toPageDto(Page<Recipe> page, Long languageId) {
        Long usedLanguageId = languageService.getLanguageIdOrDefault(languageId);
        List<RecipeDto> dtos = page.getItems()
                .stream()
                .map(entity -> DtoMapper.toDto(entity, usedLanguageId))
                .collect(Collectors.toList());
        return new PageDto<>(dtos, page.getTotalCount());
    }

    private Supplier<QueryTypeNumber> decorateQueryTypeNumberSupplierWithCheck(Supplier<QueryTypeNumber> querySupplier) {
        return () -> {
            QueryTypeNumber query = querySupplier.get();
            queryCheck.check(query);
            return query;
        };
    }

    private Supplier<QueryTypeRatio> decorateQueryTypeRatioSupplierWithCheck(Supplier<QueryTypeRatio> querySupplier) {
        return () -> {
            QueryTypeRatio query = querySupplier.get();
            queryCheck.check(query);
            return query;
        };
    }

    private CompletionStage<PageDto<RecipeDto>> queryRecipesByCommonParams(CompletionStage<Common> paramsStage, Long languageId) {
        return paramsStage.thenCompose(p -> repository.pageOfQueryTypeNone(p))
                .thenApplyAsync(p -> toPageDto(p, languageId));
    }

    private CompletionStage<PageDto<RecipeDto>> queryRecipesByQueryTypeRatio(CompletionStage<QueryTypeRatio> paramsStage, Long languageId) {
        return paramsStage.thenCompose(q -> repository.pageOfQueryTypeRatio(q))
                .thenApplyAsync(p -> toPageDto(p, languageId));
    }

    private CompletionStage<PageDto<RecipeDto>> queryRecipesByQueryTypeNumber(CompletionStage<QueryTypeNumber> paramsStage, Long languageId) {
        return paramsStage.thenCompose(q -> repository.pageOfQueryTypeNumber(q))
                .thenApplyAsync(p -> toPageDto(p, languageId));
    }
}
