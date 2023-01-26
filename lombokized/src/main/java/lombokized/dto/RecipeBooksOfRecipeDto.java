package lombokized.dto;

import lombok.Data;

import java.util.List;

@Data
public class RecipeBooksOfRecipeDto {
    private final List<Long> recipeBookIds;
}
