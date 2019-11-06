package security.imp;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import play.Logger;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import security.SocialTokenVerifier;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class SocialTokenVerifierFacebookImp implements SocialTokenVerifier {
    private WSClient wsClient;
    private String secret;
    private String clientId;
    private String url;

    private static Logger.ALogger logger = Logger.of(SocialTokenVerifierFacebookImp.class);

    @Inject
    public SocialTokenVerifierFacebookImp(WSClient wsClient, Config config) {
        this.wsClient = wsClient;
        secret = config.getString("facebook.secret");
        clientId = config.getString("facebook.clientid");
        url = config.getString("facebook.apiurl");
    }

    @Override
    public CompletionStage<Boolean> verify(String token) {
        WSRequest request = wsClient.url(url);
        request.addQueryParameter("input_token", token);
        request.addQueryParameter("access_token", clientId + "|" + secret);

        return request.get()
                .thenApply(WSResponse::asJson)
                .thenApply(responseJson -> {
                    assertResponse(responseJson);

                    JsonNode data = responseJson.get("data");

                    if (data.get("is_valid").asBoolean()) {
                        return checkResponseContent(data.get("app_id"));
                    } else {
                        logger.warn("verify(): Invalid token!");
                    }

                    return false;
                })
                .exceptionally(t -> {
                    logger.warn("Failed to verify token due to exception!", t);
                    return false;
                });
    }

    private void assertResponse(JsonNode response) {
        if (response == null) {
            throw new FacebookVerifierException("Json response is null!");
        }

        if (response.get("data") == null) {
            throw new FacebookVerifierException("Json response content is null!");
        }
    }

    private boolean checkResponseContent(JsonNode jsonContent) {
        String receivedClientId = jsonContent.asText();

        if (receivedClientId.equals(clientId)) {
            logger.info("checkResponseContent(): verification success!");
            return true;
        } else {
            logger.warn("checkResponseContent(): client id mismatch! receivedClientId = {}, clientId = {}",
                    receivedClientId, clientId);
            return false;
        }
    }

    private static class FacebookVerifierException extends RuntimeException {
        public FacebookVerifierException(String message) {
            super(message);
        }
    }
}
