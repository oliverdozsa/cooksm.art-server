package dto;

import queryparams.RecipesQueryParams;

public class RecipeSearchDto {
    private final String id;
    private final RecipesQueryParams.Params query;

    public RecipeSearchDto(String id, RecipesQueryParams.Params query) {
        this.id = id;
        this.query = query;
    }

    public String getId() {
        return id;
    }

    public RecipesQueryParams.Params getQuery() {
        return query;
    }
}
