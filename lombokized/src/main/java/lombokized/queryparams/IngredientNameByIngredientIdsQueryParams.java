package lombokized.queryparams;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.List;

@Constraints.Validate
@Getter
@Setter
@ToString
public class IngredientNameByIngredientIdsQueryParams implements Constraints.Validatable<ValidationError> {
    @Constraints.Required
    private Long languageId;

    @Constraints.Required
    private List<Long> ingredientIds;

    @Override
    public ValidationError validate() {
        return null;
    }
}
