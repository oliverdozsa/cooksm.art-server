package security.imp;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import play.Logger;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import security.SocialTokenVerifier;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class SocialTokenVerifierGoogleImp implements SocialTokenVerifier {
    private WSClient wsClient;
    private String clientId;
    private String apiUrl;
    private HttpExecutionContext executionContext;

    private static Logger.ALogger logger = Logger.of(SocialTokenVerifierGoogleImp.class);

    @Inject
    public SocialTokenVerifierGoogleImp(WSClient wsClient, Config config, HttpExecutionContext executionContext) {
        this.wsClient = wsClient;
        this.executionContext = executionContext;
        clientId = config.getString("google.clientid");
        apiUrl = config.getString("google.apiurl");
    }

    @Override
    public CompletionStage<Boolean> verify(String token) {
        WSRequest wsRequest = wsClient.url(apiUrl);
        wsRequest.addQueryParameter("id_token", token);

        return wsRequest.get()
                .thenApply(WSResponse::asJson)
                .thenApply(responseJson -> {
                    assertNotNull(responseJson);

                    if (responseJson.get("aud") != null && responseJson.get("aud").asText().equals(clientId)) {
                        logger.info("verify(): success");
                        return true;
                    } else if (responseJson.get("error_response") != null) {
                        logger.warn("verify(): verification result is error!");
                    }

                    return false;
                })
                .exceptionally(t -> {
                    logger.warn("Failed to verify token due to exception!", t);
                    return false;
                });
    }

    private static void assertNotNull(JsonNode response) {
        if (response == null) {
            throw new GoogleVerifierException("Response json is null!");
        }
    }

    private static class GoogleVerifierException extends RuntimeException {
        public GoogleVerifierException(String message) {
            super(message);
        }
    }
}
