package lombokized.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder(builderClassName = "Builder")
public class RecipeSearchQueryDto {
    String searchMode;
    Integer minIngs;
    Integer maxIngs;
    String orderBy;
    String orderBySort;
    Integer unknownIngs;
    String unknownIngsRel;
    Integer goodIngs;
    Integer goodAdditionalIngs;
    String goodAdditionalIngsRel;
    String goodIngsRel;
    Float goodIngsRatio;
    String nameLike;
    Boolean useFavoritesOnly;

    List<IngredientNameDto> exIngs;
    List<IngredientTagDto> exIngTags;
    List<IngredientNameDto> inIngs;
    List<IngredientTagDto> inIngTags;
    List<IngredientNameDto> addIngs;
    List<IngredientTagDto> addIngTags;
    List<SourcePageDto> sourcePages;
    List<Integer> times;
    List<RecipeBookDto> recipeBooks;

    Long languageId;
}
