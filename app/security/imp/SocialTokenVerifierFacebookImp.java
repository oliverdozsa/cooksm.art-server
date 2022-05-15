package security.imp;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import lombokized.security.VerifiedFacebookUserInfo;
import lombokized.security.VerifiedUserInfo;
import play.Logger;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import security.SocialTokenVerifier;
import security.TokenVerificationException;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class SocialTokenVerifierFacebookImp implements SocialTokenVerifier {
    private WSClient wsClient;
    private String userInfoUrl;

    private static Logger.ALogger logger = Logger.of(SocialTokenVerifierFacebookImp.class);

    @Inject
    public SocialTokenVerifierFacebookImp(WSClient wsClient, Config config) {
        this.wsClient = wsClient;
        userInfoUrl = config.getString("facebook.userinfourl");
    }

    @Override
    public CompletionStage<VerifiedUserInfo> verify(String token) {
        WSRequest request = wsClient.url(userInfoUrl);
        request.addQueryParameter("fields", "name,email");
        request.addQueryParameter("access_token", token);

        return request.get()
                .thenApply(WSResponse::asJson)
                .thenApply(json -> {
                    assertResponse(json);
                    return toVerifiedUserInfo(json);
                });
    }

    private void assertResponse(JsonNode json) {
        if (json == null) {
            throw new FacebookVerifierException("Json response is null!");
        }

        if (json.get("name") == null) {
            throw new FacebookVerifierException("Response json doesn't have name field!");
        }

        if (json.get("email") == null) {
            throw new FacebookVerifierException("Response json doesn't have email field!");
        }
    }

    private VerifiedUserInfo toVerifiedUserInfo(JsonNode json){
        String fullName = json.get("name").asText();
        String email = json.get("email").asText();
        String userId = json.get("id").asText();

        return new VerifiedFacebookUserInfo(fullName, email, userId);
    }

    private static class FacebookVerifierException extends TokenVerificationException {
        public FacebookVerifierException(String message) {
            super(message);
        }
    }
}
