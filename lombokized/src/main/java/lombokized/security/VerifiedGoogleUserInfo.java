package lombokized.security;

import lombok.Data;

@Data
public class VerifiedGoogleUserInfo implements VerifiedUserInfo {
    private final String fullName;
    private final String email;
    private final String socialId;
    private final String picture;
}
