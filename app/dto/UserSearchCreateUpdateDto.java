package dto;

import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import queryparams.RecipesQueryParams;

import javax.validation.Valid;

@Constraints.Validate
public class UserSearchCreateUpdateDto implements Constraints.Validatable<ValidationError> {
    @Constraints.MinLength(3)
    @Constraints.Required(groups = {ValidationGroupForCreate.class})
    public String name;

    @Constraints.Required(groups = {ValidationGroupForCreate.class})
    @Valid
    public RecipesQueryParams.Params query;

    @Override
    public ValidationError validate() {
        if (this.name == null && this.query == null) {
            return new ValidationError("", "one of name, and query must be present");
        }

        if (this.name != null && this.name.isEmpty()) {
            return new ValidationError("name", "name cannot be empty!");
        }

        return null;
    }

    @Override
    public String toString() {
        return "UserSearchCreateDto{" +
                "name='" + name + '\'' +
                ", query=" + query +
                '}';
    }

    public interface ValidationGroupForCreate {
    }
}
