package security;

import lombokized.security.VerifiedUserInfo;

import java.util.concurrent.CompletionStage;

public interface SocialTokenVerifier {
    CompletionStage<VerifiedUserInfo> verify(String token);
}
