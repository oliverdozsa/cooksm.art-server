package dto;

import lombokized.dto.RecipeSearchQueryDto;

public class RecipeSearchDto {
    private final String id;
    private final RecipeSearchQueryDto query;

    public RecipeSearchDto(String id, RecipeSearchQueryDto query) {
        this.id = id;
        this.query = query;
    }

    public String getId() {
        return id;
    }

    public RecipeSearchQueryDto getQuery() {
        return query;
    }
}
