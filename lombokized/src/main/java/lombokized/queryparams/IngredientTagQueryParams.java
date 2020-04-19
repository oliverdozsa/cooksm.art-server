package lombokized.queryparams;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;

@Constraints.Validate
@Getter
@Setter
@ToString
public class IngredientTagQueryParams implements Constraints.Validatable<ValidationError> {
    @Constraints.Required
    private Long languageId;

    @Constraints.MinLength(2)
    private String nameLike;

    @Constraints.Min(0)
    private Integer offset;

    @Constraints.Min(1)
    @Constraints.Max(50)
    private Integer limit = 25;

    @Override
    public ValidationError validate() {
        return null;
    }
}
