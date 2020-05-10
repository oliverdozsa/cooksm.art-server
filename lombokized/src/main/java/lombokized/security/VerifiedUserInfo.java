package lombokized.security;

import lombok.Data;

@Data
public class VerifiedUserInfo {
    private final String fullName;
    private final String email;
}
