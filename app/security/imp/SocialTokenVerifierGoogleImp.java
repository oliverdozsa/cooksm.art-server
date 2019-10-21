package security.imp;

import security.SocialTokenVerifier;

public class SocialTokenVerifierGoogleImp implements SocialTokenVerifier {
    @Override
    public boolean verify(String token) {
        return false;
    }
}
