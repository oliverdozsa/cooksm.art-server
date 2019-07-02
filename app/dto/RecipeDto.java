package dto;

import lombok.Data;

import java.util.List;

@Data
public class RecipeDto {
    private final Long id;
    private final String name;
    private final String url;
    private final Integer numofings;
    private final SourcePageDto sourcePage;
    private final List<IngredientNameDto> ingredients;
}
