package lombokized.dto;

import lombok.Data;
import lombok.ToString;
import play.data.validation.Constraints;

@Data
public class FavoriteRecipeCreateDto {
    @Constraints.Required
    final Long recipeId;
}
