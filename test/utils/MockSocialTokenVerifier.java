package utils;

import security.SocialTokenVerifier;

import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class MockSocialTokenVerifier implements SocialTokenVerifier {
    private static boolean mockResult = false;

    @Override
    public CompletionStage<Boolean> verify(String token) {
        return completedFuture(mockResult);
    }

    public static void setMockResult(boolean mockResult) {
        MockSocialTokenVerifier.mockResult = mockResult;
    }
}
