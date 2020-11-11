package lombokized.dto;

import lombok.Data;

import java.util.List;

@Data
public class IngredientTagResolvedDto {
    private final Long id;
    private final String name;
    private final List<IngredientNameDto> ingredients;
}
