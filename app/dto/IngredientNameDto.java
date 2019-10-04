package dto;

import lombok.Data;

import java.util.List;

@Data
public class IngredientNameDto {
    private final Long ingredientId;
    private final String name;
    private final List<String> altNames;
}
