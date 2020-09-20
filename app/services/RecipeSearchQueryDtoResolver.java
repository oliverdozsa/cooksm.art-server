package services;

import data.entities.SourcePage;
import data.repositories.IngredientNameRepository;
import data.repositories.IngredientTagRepository;
import data.repositories.SourcePageRepository;
import lombokized.dto.IngredientNameDto;
import lombokized.dto.IngredientTagDto;
import lombokized.dto.RecipeSearchQueryDto;
import lombokized.dto.SourcePageDto;
import queryparams.RecipesQueryParams;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static services.DtoMapper.*;

class RecipeSearchQueryDtoResolver {
    private IngredientNameRepository ingredientNameRepository;
    private IngredientTagRepository ingredientTagRepository;
    private SourcePageRepository sourcePageRepository;

    private RecipesQueryParams.Params queryParams;

    private List<IngredientNameDto> includedIngredients;
    private List<IngredientNameDto> excludedIngredients;
    private List<IngredientNameDto> additionalIngredients;
    private List<IngredientTagDto> includedIngredientTags;
    private List<IngredientTagDto> excludedIngredientTags;
    private List<IngredientTagDto> additionalIngredientTags;
    private List<SourcePageDto> sourcePages;

    private RecipeSearchQueryDto dto;

    private CompletionStage<Void> noopStage = completedFuture(null);

    private Long usedLanguageId;

    public void setIngredientNameRepository(IngredientNameRepository ingredientNameRepository) {
        this.ingredientNameRepository = ingredientNameRepository;
    }

    public void setIngredientTagRepository(IngredientTagRepository ingredientTagRepository) {
        this.ingredientTagRepository = ingredientTagRepository;
    }

    public void setSourcePageRepository(SourcePageRepository sourcePageRepository) {
        this.sourcePageRepository = sourcePageRepository;
    }

    public void setUsedLanguageId(Long usedLanguageId) {
        this.usedLanguageId = usedLanguageId;
    }

    public void setQueryParams(RecipesQueryParams.Params queryParams) {
        this.queryParams = queryParams;
    }

    public CompletionStage<RecipeSearchQueryDto> resolve() {
        return collectIncludedIngredients()
                .thenComposeAsync(v -> collectIncludedIngredients())
                .thenComposeAsync(v -> collectExcludedIngredients())
                .thenComposeAsync(v -> collectAdditionalIngredients())
                .thenComposeAsync(v -> collectIncludedIngredientTags())
                .thenComposeAsync(v -> collectExcludedIngredientTags())
                .thenComposeAsync(v -> collectAdditionalIngredientTags())
                .thenComposeAsync(v -> collectSourcePages())
                .thenApplyAsync(v -> {
                    dto = toDto();
                    return dto;
                });
    }

    public RecipeSearchQueryDto getDto() {
        return dto;
    }

    private CompletionStage<Void> collectIncludedIngredients() {
        if (queryParams.inIngs == null) {
            return noopStage;
        }

        return ingredientNameRepository.byIngredientIds(queryParams.inIngs, usedLanguageId)
                .thenAcceptAsync(l -> includedIngredients = toIngredientNameDtoList(l));
    }

    private CompletionStage<Void> collectExcludedIngredients() {
        if (queryParams.exIngs == null) {
            return noopStage;
        }

        return ingredientNameRepository.byIngredientIds(queryParams.exIngs, usedLanguageId)
                .thenAcceptAsync(l -> excludedIngredients = toIngredientNameDtoList(l));
    }

    private CompletionStage<Void> collectAdditionalIngredients() {
        if (queryParams.addIngs == null) {
            return noopStage;
        }

        return ingredientNameRepository.byIngredientIds(queryParams.addIngs, usedLanguageId)
                .thenAcceptAsync(l -> additionalIngredients = toIngredientNameDtoList(l));
    }

    private CompletionStage<Void> collectIncludedIngredientTags() {
        if (queryParams.inIngTags == null) {
            return noopStage;
        }

        return ingredientTagRepository.byIds(queryParams.inIngTags)
                .thenAcceptAsync(l -> includedIngredientTags = toIngredientTagDtoList(l));
    }

    private CompletionStage<Void> collectExcludedIngredientTags() {
        if (queryParams.exIngTags == null) {
            return noopStage;
        }

        return ingredientTagRepository.byIds(queryParams.exIngTags)
                .thenAcceptAsync(l -> excludedIngredientTags = toIngredientTagDtoList(l));
    }

    private CompletionStage<Void> collectAdditionalIngredientTags() {
        if (queryParams.addIngTags == null) {
            return noopStage;
        }

        return ingredientTagRepository.byIds(queryParams.addIngTags)
                .thenAcceptAsync(l -> additionalIngredientTags = toIngredientTagDtoList(l));
    }

    private CompletionStage<Void> collectSourcePages() {
        if (queryParams.sourcePages == null) {
            return noopStage;
        }

        return sourcePageRepository.allSourcePages()
                .thenAcceptAsync(l -> {
                    RecipesQueryParams.Params query = queryParams;
                    List<SourcePage> filtered = l.getItems().stream()
                            .filter(s -> query.sourcePages.contains(s.getId()))
                            .collect(Collectors.toList());
                    sourcePages = toSourcePageDtoList(filtered);
                });
    }

    private RecipeSearchQueryDto toDto() {
        return RecipeSearchQueryDto.builder()
                .searchMode(queryParams.searchMode)
                .minIngs(queryParams.minIngs)
                .maxIngs(queryParams.maxIngs)
                .orderBy(queryParams.orderBy)
                .orderBySort(queryParams.orderBySort)
                .unknownIngs(queryParams.unknownIngs)
                .unknownIngsRel(queryParams.unknownIngsRel)
                .goodIngs(queryParams.goodIngs)
                .goodIngsRel(queryParams.goodIngsRel)
                .goodIngsRatio(queryParams.goodIngsRatio)
                .goodAdditionalIngs(queryParams.goodAdditionalIngs)
                .goodAdditionalIngsRel(queryParams.goodAdditionalIngsRel)
                .nameLike(queryParams.nameLike)
                .languageId(queryParams.languageId)
                .useFavoritesOnly(queryParams.useFavoritesOnly)
                .addIngs(additionalIngredients)
                .addIngTags(additionalIngredientTags)
                .inIngs(includedIngredients)
                .inIngTags(includedIngredientTags)
                .exIngs(excludedIngredients)
                .exIngTags(excludedIngredientTags)
                .sourcePages(sourcePages)
                .build();
    }
}
