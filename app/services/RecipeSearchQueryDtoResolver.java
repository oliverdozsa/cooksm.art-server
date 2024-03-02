package services;

import data.entities.IngredientName;
import data.entities.IngredientTag;
import data.entities.RecipeBook;
import data.entities.SourcePage;
import data.repositories.IngredientNameRepository;
import data.repositories.IngredientTagRepository;
import data.repositories.RecipeBookRepository;
import data.repositories.SourcePageRepository;
import lombokized.dto.IngredientNameDto;
import lombokized.dto.IngredientTagDto;
import lombokized.dto.RecipeBookDto;
import lombokized.dto.RecipeSearchQueryDto;
import lombokized.dto.SourcePageDto;
import lombokized.repositories.Page;
import queryparams.RecipesQueryParams;

import java.util.List;
import java.util.stream.Collectors;

import static services.DtoMapper.*;

class RecipeSearchQueryDtoResolver {
    private IngredientNameRepository ingredientNameRepository;
    private IngredientTagRepository ingredientTagRepository;
    private SourcePageRepository sourcePageRepository;
    private RecipeBookRepository recipeBookRepository;

    private RecipesQueryParams.Params queryParams;

    private List<IngredientNameDto> includedIngredients;
    private List<IngredientNameDto> excludedIngredients;
    private List<IngredientNameDto> additionalIngredients;
    private List<IngredientTagDto> includedIngredientTags;
    private List<IngredientTagDto> excludedIngredientTags;
    private List<IngredientTagDto> additionalIngredientTags;
    private List<SourcePageDto> sourcePages;
    private List<RecipeBookDto> recipeBooks;

    private RecipeSearchQueryDto dto;

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

    public void setRecipeBookRepository(RecipeBookRepository recipeBookRepository) {
        this.recipeBookRepository = recipeBookRepository;
    }

    public void setUsedLanguageId(Long usedLanguageId) {
        this.usedLanguageId = usedLanguageId;
    }

    public void setQueryParams(RecipesQueryParams.Params queryParams) {
        this.queryParams = queryParams;
    }

    public RecipeSearchQueryDto resolve() {
        collectIncludedIngredients();
        collectExcludedIngredients();
        collectAdditionalIngredients();
        collectIncludedIngredientTags();
        collectExcludedIngredientTags();
        collectAdditionalIngredientTags();
        collectSourcePages();
        collectRecipeBooks();

        dto = toDto();
        return dto;
    }

    public RecipeSearchQueryDto getDto() {
        return dto;
    }

    private void collectIncludedIngredients() {
        if (queryParams.inIngs != null) {
            List<IngredientName> ingredientNames = ingredientNameRepository.byIngredientIds(queryParams.inIngs, usedLanguageId);
            includedIngredients = toIngredientNameDtoList(ingredientNames);
        }
    }

    private void collectExcludedIngredients() {
        if (queryParams.exIngs != null) {
            List<IngredientName> ingredientNames = ingredientNameRepository.byIngredientIds(queryParams.exIngs, usedLanguageId);
            excludedIngredients = toIngredientNameDtoList(ingredientNames);
        }
    }

    private void collectAdditionalIngredients() {
        if (queryParams.addIngs != null) {
            List<IngredientName> ingredientNames = ingredientNameRepository.byIngredientIds(queryParams.addIngs, usedLanguageId);
            additionalIngredients = toIngredientNameDtoList(ingredientNames);
        }
    }

    private void collectIncludedIngredientTags() {
        if (queryParams.inIngTags != null) {
            List<IngredientTag> ingredientTags = ingredientTagRepository.byIds(queryParams.inIngTags);
            includedIngredientTags = toIngredientTagDtoList(ingredientTags, usedLanguageId);
        }
    }

    private void collectExcludedIngredientTags() {
        if (queryParams.exIngTags != null) {
            List<IngredientTag> ingredientTags = ingredientTagRepository.byIds(queryParams.exIngTags);
            excludedIngredientTags = toIngredientTagDtoList(ingredientTags, usedLanguageId);
        }
    }

    private void collectAdditionalIngredientTags() {
        if (queryParams.addIngTags != null) {
            List<IngredientTag> ingredientTags = ingredientTagRepository.byIds(queryParams.addIngTags);
            additionalIngredientTags = toIngredientTagDtoList(ingredientTags, usedLanguageId);
        }
    }

    private void collectSourcePages() {
        if (queryParams.sourcePages != null) {
            Page<SourcePage> sourcePagesPage = sourcePageRepository.allSourcePages();
            RecipesQueryParams.Params query = queryParams;
            List<SourcePage> filtered = sourcePagesPage.getItems().stream()
                    .filter(s -> query.sourcePages.contains(s.getId()))
                    .collect(Collectors.toList());
            sourcePages = toSourcePageDtoList(filtered);
        }
    }

    private void collectRecipeBooks() {
        if (queryParams.recipeBooks != null && queryParams.recipeBooks.size() != 0) {
            List<RecipeBook> recipeBooksEntities =  recipeBookRepository.byIds(queryParams.recipeBooks);
            recipeBooks = toRecipeBookDtoList(recipeBooksEntities);
        }
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
                .times(queryParams.times)
                .recipeBooks(recipeBooks)
                .build();
    }
}
