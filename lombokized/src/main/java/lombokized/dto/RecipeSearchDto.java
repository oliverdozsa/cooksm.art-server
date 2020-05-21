package lombokized.dto;

import lombok.Value;

@Value
public class RecipeSearchDto {
    String id;
    String query;
}
