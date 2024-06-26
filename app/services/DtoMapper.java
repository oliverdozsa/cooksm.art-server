package services;

import data.entities.*;
import lombokized.dto.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DtoMapper {
    public static IngredientNameDto toDto(IngredientName entity) {
        List<String> altNames = entity.getAltNames()
                .stream()
                .map(a -> a.getName())
                .collect(Collectors.toList());

        return new IngredientNameDto(entity.getIngredient().getId(), entity.getName(), altNames);
    }

    public static SourcePageDto toDto(SourcePage entity) {
        return new SourcePageDto(entity.getId(), entity.getName(), entity.getLanguage().getIsoName());
    }

    public static RecipeDto toDto(Recipe recipe, Long languageId) {
        List<IngredientNameDto> ingredientNameDtos = recipe.getIngredients()
                .stream()
                .map(ri -> {
                    List<IngredientName> ingredientNames = ri.getIngredient().getNames();
                    return ingredientNames
                            .stream()
                            .filter(i -> i.getLanguage().getId().equals(languageId))
                            .findFirst().orElseThrow(() -> new IllegalArgumentException("Ingredient name with the given language not found!"));
                })
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());

        return new RecipeDto(
                recipe.getId(),
                recipe.getName(),
                recipe.getUrl(),
                recipe.getNumofings(),
                toDto(recipe.getSourcePage()),
                ingredientNameDtos,
                recipe.getTime().ordinal(),
                recipe.getImageUrl()
        );
    }

    public static FavoriteRecipeDto toDto(FavoriteRecipe entity) {
        return new FavoriteRecipeDto(
                entity.getId(),
                entity.getRecipe().getId()
        );
    }

    public static IngredientTagDto toDto(IngredientTag entity, Long languageId) {
        List<Long> ingredientsIds =
                entity.getIngredients().stream().map(Ingredient::getId).collect(Collectors.toList());

        IngredientTagName tagName = entity.getNames().stream().filter(n -> n.getLanguage().getId().equals(languageId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tag name with the given language not found! language = " + languageId));

        return new IngredientTagDto(entity.getId(), tagName.getName(), ingredientsIds);
    }

    public static List<IngredientNameDto> toIngredientNameDtoList(List<IngredientName> ingredientNames) {
        List<IngredientNameDto> dtoList = new ArrayList<>();
        ingredientNames.forEach(e -> dtoList.add(toDto(e)));
        return dtoList;
    }

    public static List<IngredientTagDto> toIngredientTagDtoList(List<IngredientTag> ingredientTags, Long languageId) {
        List<IngredientTagDto> dtoList = new ArrayList<>();
        ingredientTags.forEach(e -> dtoList.add(toDto(e, languageId)));
        return dtoList;
    }

    public static List<SourcePageDto> toSourcePageDtoList(List<SourcePage> sourcePages) {
        List<SourcePageDto> dtoList = new ArrayList<>();
        sourcePages.forEach(e -> dtoList.add(toDto(e)));
        return dtoList;
    }

    public static List<RecipeBookDto> toRecipeBookDtoList(List<RecipeBook> recipeBooks) {
        List<RecipeBookDto> dtoList = new ArrayList<>();
        recipeBooks.forEach(e -> dtoList.add(toDto(e)));
        return dtoList;
    }

    public static UserSearchDto toDto(UserSearch entity) {
        String encodedSearchId = Base62Conversions.encode(entity.getSearch().getId());
        return new UserSearchDto(entity.getId(), encodedSearchId, entity.getName());
    }

    public static GlobalSearchDto toDto(GlobalSearch entity) {
        String encodedId = Base62Conversions.encode(entity.getSearch().getId());
        return new GlobalSearchDto(entity.getName(), encodedId, entity.getUrlFriendlyName());
    }

    public static IngredientTagResolvedDto toDto(IngredientTag tag, List<IngredientNameDto> names, Long languageId) {
        IngredientTagName tagName = tag.getNames().stream().filter(n -> n.getLanguage().getId().equals(languageId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tag name with the given language not found! language = " + languageId));

        return new IngredientTagResolvedDto(tag.getId(), tagName.getName(), names);
    }

    public static RecipeBookDto toDto(RecipeBook entity) {
        return new RecipeBookDto(entity.getId(), entity.getName(), entity.getLastAccessed());
    }

    public static RecipeBookWithRecipesDto toRecipeBookWithRecipesDto(RecipeBook entity) {
        List<RecipeInRecipeBookSummaryDto> recipeSummaries = entity.getRecipes().stream()
                .map(DtoMapper::toSummaryDto)
                .collect(Collectors.toList());

        return new RecipeBookWithRecipesDto(entity.getId(), entity.getName(), entity.getLastAccessed(), recipeSummaries);
    }

    public static ShoppingListListElementDto toShoppingListListElementDto(ShoppingList entity) {
        return new ShoppingListListElementDto(entity.getId(), entity.getName());
    }

    public static ShoppingListDto toDto(ShoppingList entity) {
        List<ShoppingListItemDto> itemsDto = entity.getItems().stream()
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());

        return new ShoppingListDto(entity.getId(), entity.getName(), itemsDto);
    }

    public static RecipeBooksOfRecipeDto toDto(List<RecipeBook> recipeBooks) {
        List<Long> ids = recipeBooks.stream().map(RecipeBook::getId)
                .collect(Collectors.toList());
        return new RecipeBooksOfRecipeDto(ids);
    }

    public static MenuDto toDto(Menu entity, Long languageId) {
        List<MenuGroupDto> menuGroupDtos = entity.getGroups().stream()
                .map(e -> DtoMapper.toMenuGroupDto(e, languageId))
                .collect(Collectors.toList());

        return new MenuDto(entity.getId(), entity.getName(), menuGroupDtos);
    }

    public static List<MenuTitleDto> toMenuTitleDtos(List<Menu> menus) {
        return menus.stream()
                .map(DtoMapper::toMenuTitleDto)
                .collect(Collectors.toList());
    }

    private static MenuTitleDto toMenuTitleDto(Menu menu) {
        return new MenuTitleDto(menu.getId(), menu.getName());
    }

    private static ShoppingListItemDto toDto(ShoppingListItem entity) {
        return new ShoppingListItemDto(entity.getId(), entity.getName(), entity.isCompleted(), entity.getCategory().getId());
    }

    private static RecipeInRecipeBookSummaryDto toSummaryDto(Recipe recipe) {
        return new RecipeInRecipeBookSummaryDto(
                recipe.getId(), recipe.getName(), recipe.getUrl()
        );
    }

    private static MenuGroupDto toMenuGroupDto(MenuGroup entity, Long languageId) {
        List<RecipeDto> recipeDtos = entity.getRecipes().stream()
                .map(r -> DtoMapper.toDto(r, languageId))
                .collect(Collectors.toList());
        return new MenuGroupDto(recipeDtos);
    }
}
