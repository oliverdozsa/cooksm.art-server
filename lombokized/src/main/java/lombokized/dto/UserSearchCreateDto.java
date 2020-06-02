package lombokized.dto;

import lombok.Data;
import play.data.validation.Constraints;

@Data
public class UserSearchCreateDto {
    @Constraints.MinLength(3)
    final String name;

    final String query;
}
