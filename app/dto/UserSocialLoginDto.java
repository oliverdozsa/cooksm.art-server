package dto;

import lombok.Data;

@Data
public class UserSocialLoginDto {
    private final String fullName;
    private final String email;
    private final String token;
}
