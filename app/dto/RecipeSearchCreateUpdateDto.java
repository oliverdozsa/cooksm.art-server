package dto;

import lombok.Data;

@Data
public class RecipeSearchCreateUpdateDto {
    private final String name;
    private final String queryParams;
}
