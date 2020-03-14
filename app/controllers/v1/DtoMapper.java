package controllers.v1;

import dto.IngredientNameDto;
import dto.IngredientTagDto;
import dto.RecipeDto;
import dto.SourcePageDto;
import models.entities.IngredientName;
import models.entities.IngredientTag;
import models.entities.Recipe;
import models.entities.SourcePage;

import java.util.List;
import java.util.stream.Collectors;

class DtoMapper {
    public static IngredientNameDto toDto(IngredientName entity) {
        return new IngredientNameDto(entity.getIngredient().getId(), entity.getName());
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

    public static IngredientTagDto toDto(IngredientTag entity) {
        return new IngredientTagDto(entity.getId(), entity.getName());
    }
}
