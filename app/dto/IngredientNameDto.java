package dto;

import lombok.Data;

import java.util.List;

@Data
public class IngredientNameDto {
    private final Long id;
    private final String name;
    private final List<String> altNames;
}
