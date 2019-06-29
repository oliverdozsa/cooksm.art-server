package dto;

import lombok.Data;

import java.util.List;

@Data
public class IngredientNamesDto {
    private final List<IngredientNameDto> ingredientNames;
    private final Long totalCount;
}
