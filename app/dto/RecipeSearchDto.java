package dto;

import lombok.Data;

@Data
public class RecipeSearchDto {
    private final Long id;
    private final String name;
    private final String query;
}
