package utils;

import lombokized.security.VerifiedUserInfo;
import security.SocialTokenVerifier;
import security.TokenVerificationException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class MockSocialTokenVerifier implements SocialTokenVerifier {
    private static VerifiedUserInfo mockResult = null;
    private static boolean shouldThrowException = false;

    @Override
    public CompletionStage<VerifiedUserInfo> verify(String token) {
        if (shouldThrowException) {
            return CompletableFuture.supplyAsync(() -> {
                throw new TokenVerificationException("");
            });
        } else {
            return completedFuture(mockResult);
        }
    }

    public static void setMockResult(VerifiedUserInfo mockResult) {
        MockSocialTokenVerifier.mockResult = mockResult;
    }

    public static void shouldThrowException(boolean shouldThrow) {
        shouldThrowException = shouldThrow;
    }
}
