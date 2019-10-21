package security.imp;

import security.SocialTokenVerifier;

public class SocialTokenVerifierFacebookImp implements SocialTokenVerifier {
    @Override
    public boolean verify(String token) {
        return false;
    }
}
