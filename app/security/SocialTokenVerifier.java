package security;

import java.util.concurrent.CompletionStage;

public interface SocialTokenVerifier {
    CompletionStage<Boolean> verify(String token);
}
