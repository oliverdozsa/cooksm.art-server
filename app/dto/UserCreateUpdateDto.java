package dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import play.data.validation.Constraints;

@Getter
@RequiredArgsConstructor
public class UserCreateUpdateDto {
    @Constraints.Email
    private final String email;

    @Constraints.MinLength(3)
    private final String fullName;
}
