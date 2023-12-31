package lombokized.dto;

import lombok.Data;

@Data
public class UserInfoDto {
    private final String jwtAuthToken;
    private final String email;
    private final String fullName;
    private final String picture;
}
