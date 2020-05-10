package lombokized.security;

import lombok.Data;

@Data
public class VerifiedFacebookUserInfo implements VerifiedUserInfo {
    private final String fullName;
    private final String email;
    private final String socialId;
}
