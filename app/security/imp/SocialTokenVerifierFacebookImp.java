package security.imp;

import security.SocialTokenVerifier;

import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class SocialTokenVerifierFacebookImp implements SocialTokenVerifier {
    @Override
    public CompletionStage<Boolean> verify(String token) {
        return completedFuture(false);
    }
}
