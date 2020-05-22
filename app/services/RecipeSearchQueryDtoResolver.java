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

    public RecipeSearchQueryDtoResolver(RecipesQueryParams.Params queryParams) {
        this.queryParams = queryParams;
    }

    public void setIngredientNameRepository(IngredientNameRepository ingredientNameRepository) {
        this.ingredientNameRepository = ingredientNameRepository;
    }

    public void setIngredientTagRepository(IngredientTagRepository ingredientTagRepository) {
        this.ingredientTagRepository = ingredientTagRepository;
    }

    public void setSourcePageRepository(SourcePageRepository sourcePageRepository) {
        this.sourcePageRepository = sourcePageRepository;
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
        return ingredientNameRepository.byIds(queryParams.inIngs)
                .thenAcceptAsync(l -> includedIngredients = toIngredientNameDtoList(l));
    }

    private CompletionStage<Void> collectExcludedIngredients() {
        return ingredientNameRepository.byIds(queryParams.exIngs)
                .thenAcceptAsync(l -> excludedIngredients = toIngredientNameDtoList(l));
    }

    private CompletionStage<Void> collectAdditionalIngredients() {
        return ingredientNameRepository.byIds(queryParams.addIngs)
                .thenAcceptAsync(l -> additionalIngredients = toIngredientNameDtoList(l));
    }

    private CompletionStage<Void> collectIncludedIngredientTags() {
        return ingredientTagRepository.byIds(queryParams.inIngTags)
                .thenAcceptAsync(l -> includedIngredientTags = toIngredientTagDtoList(l));
    }

    private CompletionStage<Void> collectExcludedIngredientTags() {
        return ingredientTagRepository.byIds(queryParams.exIngTags)
                .thenAcceptAsync(l -> excludedIngredientTags = toIngredientTagDtoList(l));
    }

    private CompletionStage<Void> collectAdditionalIngredientTags() {
        return ingredientTagRepository.byIds(queryParams.addIngTags)
                .thenAcceptAsync(l -> additionalIngredientTags = toIngredientTagDtoList(l));
    }

    private CompletionStage<Void> collectSourcePages() {
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
                .nameLike(queryParams.nameLike)
                .languageId(queryParams.languageId)
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
