package utils;

import security.SocialTokenVerifier;

public class MockSocialTokenVerifier implements SocialTokenVerifier {
    private static boolean mockResult = false;

    @Override
    public boolean verify(String token) {
        return mockResult;
    }

    public static void setMockResult(boolean mockResult) {
        MockSocialTokenVerifier.mockResult = mockResult;
    }
}
