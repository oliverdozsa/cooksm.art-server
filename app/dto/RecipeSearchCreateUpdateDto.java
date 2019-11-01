package dto;

import lombok.ToString;
import play.data.validation.Constraints;

import javax.validation.constraints.NotNull;

@ToString
public class RecipeSearchCreateUpdateDto {
    private String name;
    private String query;

    public RecipeSearchCreateUpdateDto() {
    }

    public RecipeSearchCreateUpdateDto(String name, String query) {
        this.name = name;
        this.query = query;
    }

    public String getName() {
        return name;
    }

    public void setName(@Constraints.MinLength(2) String name) {
        this.name = name;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(@NotNull String query) {
        this.query = query;
    }
}
