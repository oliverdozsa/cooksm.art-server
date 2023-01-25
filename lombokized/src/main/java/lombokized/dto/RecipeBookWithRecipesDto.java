package lombokized.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class RecipeBookWithRecipesDto {
    private final Long id;
    private final String name;
    private final Instant lastAccessed;
    private final List<RecipeInRecipeBookSummaryDto> recipeSummaries;
}
