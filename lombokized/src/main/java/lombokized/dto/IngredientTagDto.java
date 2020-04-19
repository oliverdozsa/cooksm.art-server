package lombokized.dto;

import lombok.Data;

import java.util.List;

@Data
public class IngredientTagDto {
    private final Long id;
    private final String name;
    private final List<Long> ingredients;
}
