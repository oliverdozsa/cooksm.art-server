package lombokized.dto;

import lombok.Data;

@Data
public class RecipeInRecipeBookSummaryDto {
    public final Long id;
    public final String name;
    public final String url;
}
