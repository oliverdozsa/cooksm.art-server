package dto;

import play.data.validation.Constraints;
import queryparams.RecipesQueryParams;

import javax.validation.Valid;

public class UserSearchCreateDto {
    @Constraints.MinLength(3)
    @Constraints.Required
    public String name;

    @Constraints.Required
    @Valid
    public RecipesQueryParams.Params query;
}
