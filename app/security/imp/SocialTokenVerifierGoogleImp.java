package security.imp;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import lombokized.security.VerifiedUserInfo;
import play.Logger;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import security.SocialTokenVerifier;
import security.TokenVerificationException;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class SocialTokenVerifierGoogleImp implements SocialTokenVerifier {
    private WSClient wsClient;
    private String clientId;
    private String apiUrl;

    private static Logger.ALogger logger = Logger.of(SocialTokenVerifierGoogleImp.class);

    @Inject
    public SocialTokenVerifierGoogleImp(WSClient wsClient, Config config) {
        this.wsClient = wsClient;
        clientId = config.getString("google.clientid");
        apiUrl = config.getString("google.apiurl");
    }

    @Override
    public CompletionStage<VerifiedUserInfo> verify(String token) {
        WSRequest wsRequest = wsClient.url(apiUrl);
        wsRequest.addQueryParameter("id_token", token);

        return wsRequest.get()
                .thenApply(WSResponse::asJson)
                .thenApply(responseJson -> {
                    assertResponse(responseJson);
                    return toVerifiedUserInfo(responseJson);
                });
    }

    private void assertResponse(JsonNode response) {
        if (response == null) {
            throw new GoogleVerifierException("Response json is null!");
        }

        if (response.get("aud") == null) {
            throw new GoogleVerifierException("Response json doesn't have aud field!");
        }

        String receivedClientId = response.get("aud").asText();
        if (!receivedClientId.equals(clientId)) {
            String message = String.format("checkResponseContent(): client id mismatch! receivedClientId = %s, clientId = %s",
                    receivedClientId, clientId);
            throw new GoogleVerifierException(message);
        }

        if (response.get("email") == null) {
            throw new GoogleVerifierException("Response json doesn't have email field!");
        }

        if (response.get("name") == null) {
            throw new GoogleVerifierException("Response json doesn't have name field!");
        }
    }

    private VerifiedUserInfo toVerifiedUserInfo(JsonNode json) {
        String fullName = json.get("name").asText();
        String email = json.get("email").asText();
        return new VerifiedUserInfo(fullName, email);
    }

    private static class GoogleVerifierException extends TokenVerificationException {
        public GoogleVerifierException(String message) {
            super(message);
        }
    }
}
