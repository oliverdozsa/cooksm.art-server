package dto;

import play.data.validation.Constraints;
import queryparams.RecipesQueryParams;

import javax.validation.Valid;

public class UserSearchCreateUpdateDto {
    @Constraints.MinLength(3)
    @Constraints.Required
    public String name;

    @Constraints.Required
    @Valid
    public RecipesQueryParams.Params query;

    @Override
    public String toString() {
        return "UserSearchCreateDto{" +
                "name='" + name + '\'' +
                ", query=" + query +
                '}';
    }
}
