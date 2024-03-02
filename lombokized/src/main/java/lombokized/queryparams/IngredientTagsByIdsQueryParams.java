package lombokized.queryparams;

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
public class IngredientTagsByIdsQueryParams implements Constraints.Validatable<ValidationError> {
    @Constraints.Required
    private Long languageId;

    @Constraints.Required
    private List<Long> tagIds;

    @Override
    public ValidationError validate() {
        return null;
    }
}
