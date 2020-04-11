package controllers.v1;

import dto.*;
import models.entities.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class DtoMapper {
    public static IngredientNameDto toDto(IngredientName entity) {
        List<String> altNames = entity.getAltNames()
                .stream()
                .map(a -> a.getName())
                .collect(Collectors.toList());

        return new IngredientNameDto(entity.getIngredient().getId(), entity.getName(), altNames);
    }

    public static SourcePageDto toDto(SourcePage entity) {
        return new SourcePageDto(entity.getId(), entity.getName());
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
                ingredientNameDtos
        );
    }

    public static FavoriteRecipeDto toDto(FavoriteRecipe entity) {
        return new FavoriteRecipeDto(
                entity.getId(),
                entity.getRecipe().getName(),
                entity.getRecipe().getUrl(),
                entity.getRecipe().getId()
        );
    }

    public static RecipeSearchDto toDto(RecipeSearch entity) {
        return new RecipeSearchDto(entity.getId(), entity.getName(), entity.getQuery());
    }

    public static IngredientTagDto toDto(IngredientTag entity) {
        List<Long> ingredientsIds =
                entity.getIngredients().stream().map(Ingredient::getId).collect(Collectors.toList());
        return new IngredientTagDto(entity.getId(), entity.getName(), ingredientsIds);
    }
}
