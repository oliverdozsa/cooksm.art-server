package lombokized.dto;

import lombok.Data;

import java.util.List;

@Data
public class MenuGroupDto {
    private final List<RecipeDto> recipes;
}
