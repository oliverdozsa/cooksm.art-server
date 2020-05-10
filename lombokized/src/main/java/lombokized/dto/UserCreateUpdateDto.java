package lombokized.dto;

import lombok.Data;
import lombok.ToString;
import play.data.validation.Constraints;

@Data
@ToString
public class UserCreateUpdateDto {
    @Constraints.Email
    private final String email;

    @Constraints.MinLength(3)
    private final String fullName;

    private final String googleUserId;

    private final String facebookUserId;
}
