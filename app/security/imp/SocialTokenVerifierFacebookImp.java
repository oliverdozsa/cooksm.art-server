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
    private String secret;
    private String clientId;
    private String tokenValidatorUrl;
    private String userInfoUrl;

    private static Logger.ALogger logger = Logger.of(SocialTokenVerifierFacebookImp.class);

    @Inject
    public SocialTokenVerifierFacebookImp(WSClient wsClient, Config config) {
        this.wsClient = wsClient;
        secret = config.getString("facebook.secret");
        clientId = config.getString("facebook.clientid");
        tokenValidatorUrl = config.getString("facebook.apiurl");
        userInfoUrl = config.getString("facebook.userinfourl");
    }

    @Override
    public CompletionStage<VerifiedUserInfo> verify(String token) {
        WSRequest request = wsClient.url(tokenValidatorUrl);
        request.addQueryParameter("input_token", token);
        request.addQueryParameter("access_token", clientId + "|" + secret);

        return request.get()
                .thenApply(WSResponse::asJson)
                .thenApply(responseJson -> {
                    assertResponse(responseJson);
                    return getUserId(responseJson);
                })
                .thenCompose(userid -> createUserInfoRequest(userid, token).get())
                .thenApply(WSResponse::asJson)
                .thenApply(this::toVerifiedUserInfo);
    }

    private void assertResponse(JsonNode json) {
        if (json == null) {
            throw new FacebookVerifierException("Json response is null!");
        }

        if (json.get("data") == null) {
            throw new FacebookVerifierException("Response json doesn't have 'data' field!");
        }

        JsonNode data = json.get("data");
        boolean isValid = data.get("is_valid").asBoolean();
        if (!isValid) {
            throw new FacebookVerifierException("checkTokenValidityResponseContent(): token is invalid!");
        }

        String receivedClientId = data.get("app_id").asText();
        if (!receivedClientId.equals(clientId)) {
            String message = String.format("checkResponseContent(): client id mismatch! receivedClientId = %s, clientId = %s",
                    receivedClientId, clientId);
            throw new FacebookVerifierException(message);
        }
    }

    private VerifiedUserInfo toVerifiedUserInfo(JsonNode json){
        String fullName = json.get("name").asText();
        String email = json.get("email").asText();
        String userId = json.get("id").asText();

        return new VerifiedFacebookUserInfo(fullName, email, userId);
    }

    private String getUserId(JsonNode json) {
        return json.get("data").get("user_id").asText();
    }

    private static class FacebookVerifierException extends TokenVerificationException {
        public FacebookVerifierException(String message) {
            super(message);
        }
    }

    private WSRequest createUserInfoRequest(String userId, String token){
        WSRequest request = wsClient.url(userInfoUrl + "/" + userId);
        request.addQueryParameter("fields", "id,name,email");
        request.addQueryParameter("access_token", token);
        return request;
    }
}
