package security.imp;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

import java.util.concurrent.ExecutionException;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.when;

public class SocialTokenVerifierGoogleImpTest {
    @Mock
    private Config mockConfig;

    @Mock
    private WSClient mockWsClient;

    @Mock
    private WSRequest mockWsRequest;

    @Mock
    private WSResponse mockWsResponse;

    private String mockClientId = "mockClientId";
    private String mockApiUrl = "mockApiUrl";

    private SocialTokenVerifierGoogleImp verifier;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        when(mockConfig.getString("google.clientid")).thenReturn(mockClientId);
        when(mockConfig.getString("google.apiurl")).thenReturn(mockApiUrl);
        when(mockWsClient.url(mockApiUrl)).thenReturn(mockWsRequest);
        when(mockWsRequest.get()).thenReturn(completedFuture(mockWsResponse));

        verifier = new SocialTokenVerifierGoogleImp(mockWsClient, mockConfig);
    }

    @Test
    public void testVerificationSuccessful() throws ExecutionException, InterruptedException {
        ObjectNode jsonRespone = Json.newObject();
        jsonRespone.put("aud", mockClientId);
        when(mockWsResponse.asJson()).thenReturn(jsonRespone);

        assertTrue("Verification result should be true!", verifier.verify("someToken").toCompletableFuture().get());
    }

    @Test
    public void testErrorResponse() throws ExecutionException, InterruptedException {
        ObjectNode jsonRespone = Json.newObject();
        jsonRespone.put("error_response", "someErrorMsg");
        when(mockWsResponse.asJson()).thenReturn(jsonRespone);

        assertFalse("Verification result should be false!", verifier.verify("someToken").toCompletableFuture().get());
    }

    @Test
    public void testNullResponse() throws ExecutionException, InterruptedException {
        when(mockWsResponse.asJson()).thenReturn(null);

        assertFalse("Verification result should be false!", verifier.verify("someToken").toCompletableFuture().get());
    }

    @Test
    public void testClientIdMismatch() throws ExecutionException, InterruptedException {
        ObjectNode jsonRespone = Json.newObject();
        jsonRespone.put("aud", mockClientId + "addingSomeContentToGetDifferentClientId");
        when(mockWsResponse.asJson()).thenReturn(jsonRespone);

        assertFalse("Verification result should be false!", verifier.verify("someToken").toCompletableFuture().get());
    }
}
