package services;

import com.typesafe.config.Config;
import data.DatabaseExecutionContext;
import data.entities.Recipe;
import data.entities.RecipeBook;
import data.repositories.RecipeBookRepository;
import data.repositories.RecipeRepository;
import lombokized.dto.PageDto;
import lombokized.dto.RecipeBooksOfRecipeDto;
import lombokized.dto.RecipeDto;
import lombokized.repositories.Page;
import lombokized.repositories.RecipeRepositoryParams;
import play.Logger;
import queryparams.RecipesQueryParams;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static lombokized.repositories.RecipeRepositoryParams.*;
import static services.RecipesQueryParamsMapping.*;

public class RecipesService {
    @Inject
    private RecipeRepository repository;

    @Inject
    private RecipeBookRepository recipeBookRepository;

    @Inject
    private Config config;

    @Inject
    private LanguageService languageService;

    @Inject
    private RecipeRepositoryQueryCheck queryCheck;

    @Inject
    private DatabaseExecutionContext dbExecContext;

    private static final Logger.ALogger logger = Logger.of(RecipesService.class);

    public CompletionStage<PageDto<RecipeDto>> pageOfQueryTypeNumber(RecipesQueryParams.Params queryParams) {
        logger.info("pageOfQueryTypeNumber(): queryParams = {}", queryParams);
        QueryTypeNumber queryTypeNumber = toQueryTypeNumber(queryParams);
        return queryRecipesByQueryTypeNumber(queryTypeNumber, queryParams.languageId);
    }

    public CompletionStage<PageDto<RecipeDto>> pageOfQueryTypeNumber(RecipesQueryParams.Params queryParams, Long userId) {
        logger.info("pageOfQueryTypeNumber(): userId = {}, queryParams = {}", userId, queryParams);
        QueryTypeNumber queryTypeNumber = toQueryTypeNumber(queryParams, userId);
        return queryRecipesByQueryTypeNumber(queryTypeNumber, queryParams.languageId);
    }

    public CompletionStage<PageDto<RecipeDto>> pageOfQueryTypeRatio(RecipesQueryParams.Params queryParams) {
        logger.info("pageOfQueryTypeRatio(): queryParams = {}", queryParams);
        QueryTypeRatio queryTypeRatio = toQueryTypeRatio(queryParams);
        return queryRecipesByQueryTypeRatio(queryTypeRatio, queryParams.languageId);
    }

    public CompletionStage<PageDto<RecipeDto>> pageOfQueryTypeRatio(RecipesQueryParams.Params queryParams, Long userId) {
        logger.info("pageOfQueryTypeRatio(): userId = {}. queryParams = {}", userId, queryParams);
        QueryTypeRatio queryTypeRatio = toQueryTypeRatio(queryParams, userId);
        return queryRecipesByQueryTypeRatio(queryTypeRatio, queryParams.languageId);
    }

    public CompletionStage<PageDto<RecipeDto>> pageOfQueryTypeNone(RecipesQueryParams.Params queryParams) {
        logger.info("pageOfQueryTypeNone(): queryParams = {}", queryParams);
        RecipeRepositoryParams.Common commonQueryParams = toCommon(queryParams);
        return queryRecipesByCommonParams(commonQueryParams, queryParams.languageId);
    }

    public CompletionStage<PageDto<RecipeDto>> pageOfQueryTypeNone(RecipesQueryParams.Params queryParams, Long userId) {
        logger.info("pageOfQueryTypeNone(): userId = {}, queryParams = {}", userId, queryParams);
        RecipeRepositoryParams.Common commonQueryParams = toCommon(queryParams, userId);
        return queryRecipesByCommonParams(commonQueryParams, queryParams.languageId);
    }

    public CompletionStage<RecipeDto> single(Long id, Long languageId) {
        logger.info("single(): id = {}, languageId = {}", id, languageId);
        return supplyAsync(() -> {
            Recipe recipe = repository.single(id);
            if(recipe!= null){
                Long usedLanguageId = languageService.getLanguageIdOrDefault(languageId);
                return DtoMapper.toDto(recipe, usedLanguageId);
            } else {
                return null;
            }

        }, dbExecContext);
    }

    public CompletionStage<RecipeBooksOfRecipeDto> recipeBooksOf(Long id, Long userId) {
        logger.info("recipeBooksOf(): id = {}, userId = {}", id, userId);
        return supplyAsync(() -> {
            List<RecipeBook> recipeBooks = repository.recipeBooksOf(id, userId);
            return DtoMapper.toDto(recipeBooks);
        }, dbExecContext);
    }

    private PageDto<RecipeDto> toPageDto(Page<Recipe> page, Long languageId) {
        Long usedLanguageId = languageService.getLanguageIdOrDefault(languageId);
        List<RecipeDto> dtos = page.getItems()
                .stream()
                .map(entity -> DtoMapper.toDto(entity, usedLanguageId))
                .collect(Collectors.toList());
        return new PageDto<>(dtos, page.getTotalCount());
    }

    private CompletionStage<PageDto<RecipeDto>> queryRecipesByCommonParams(Common params, Long languageId) {
        return supplyAsync(() -> {
            checkRecipeBookOfUserIfNeeded(params.getUsedRecipeBooks(), params.getUserId());
            Page<Recipe> recipesPage = repository.pageOfQueryTypeNone(params);
            return toPageDto(recipesPage, languageId);
        }, dbExecContext);
    }

    private CompletionStage<PageDto<RecipeDto>> queryRecipesByQueryTypeRatio(QueryTypeRatio params, Long languageId) {
        return supplyAsync(() -> {
            queryCheck.check(params);
            checkRecipeBookOfUserIfNeeded(params.getCommon().getUsedRecipeBooks(), params.getCommon().getUserId());
            Page<Recipe> recipePage = repository.pageOfQueryTypeRatio(params);
            return toPageDto(recipePage, languageId);
        }, dbExecContext);
    }

    private CompletionStage<PageDto<RecipeDto>> queryRecipesByQueryTypeNumber(QueryTypeNumber params, Long languageId) {
        return supplyAsync(() -> {
            queryCheck.check(params);
            checkRecipeBookOfUserIfNeeded(params.getCommon().getUsedRecipeBooks(), params.getCommon().getUserId());
            Page<Recipe> recipePage = repository.pageOfQueryTypeNumber(params);
            return toPageDto(recipePage, languageId);
        }, dbExecContext);
    }

    private void checkRecipeBookOfUserIfNeeded(List<Long> recipeBookIds, Long userId){
        if(recipeBookIds == null || recipeBookIds.size() == 0 || userId == 0) {
            return;
        }

        recipeBookRepository.checkRecipeBooksOfUser(recipeBookIds, userId);
    }
}
