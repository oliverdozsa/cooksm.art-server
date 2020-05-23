package lombokized.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder(builderClassName = "Builder")
public class RecipeSearchCreateDto {
    String searchMode;
    Integer minIngs;
    Integer maxIngs;
    String orderBy;
    String orderBySort;
    Integer unknownIngs;
    String unknownIngsRel;
    Integer goodIngs;
    Integer goodAdditionalIngs;
    String goodIngsRel;
    Float goodIngsRatio;
    String nameLike;

    List<IngredientNameDto> exIngs;
    List<IngredientTagDto> exIngTags;
    List<IngredientNameDto> inIngs;
    List<IngredientTagDto> inIngTags;
    List<IngredientNameDto> addIngs;
    List<IngredientTagDto> addIngTags;
    List<SourcePageDto> sourcePages;

    Long languageId;
}
