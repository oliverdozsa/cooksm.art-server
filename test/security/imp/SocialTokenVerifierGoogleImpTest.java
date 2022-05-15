package security.imp;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
import static org.junit.Assert.assertNotNull;
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

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private String mockApiUrl = "mockApiUrl";

    private SocialTokenVerifierGoogleImp verifier;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        when(mockConfig.getString("google.apiurl")).thenReturn(mockApiUrl);
        when(mockWsClient.url(mockApiUrl)).thenReturn(mockWsRequest);
        when(mockWsRequest.get()).thenReturn(completedFuture(mockWsResponse));

        verifier = new SocialTokenVerifierGoogleImp(mockWsClient, mockConfig);
    }

    @Test
    public void testVerificationSuccessful() throws ExecutionException, InterruptedException {
        ObjectNode jsonRespone = Json.newObject();
        jsonRespone.put("name", "someName");
        jsonRespone.put("email", "someEmail");
        jsonRespone.put("id", "4242");
        when(mockWsResponse.asJson()).thenReturn(jsonRespone);

        assertNotNull("Verification result should be not null!", verifier.verify("someToken").toCompletableFuture().get());
    }

    @Test
    public void testErrorResponse() throws ExecutionException, InterruptedException {
        ObjectNode jsonRespone = Json.newObject();
        jsonRespone.put("error_response", "someErrorMsg");
        when(mockWsResponse.asJson()).thenReturn(jsonRespone);

        exceptionRule.expect(ExecutionException.class);
        exceptionRule.expectMessage("doesn't have email");
        verifier.verify("someToken").toCompletableFuture().get();
    }

    @Test
    public void testNullResponse() throws ExecutionException, InterruptedException {
        when(mockWsResponse.asJson()).thenReturn(null);

        exceptionRule.expect(ExecutionException.class);
        exceptionRule.expectMessage("Response json is null");
        verifier.verify("someToken").toCompletableFuture().get();
    }

    @Test
    public void testMissingEmail() throws ExecutionException, InterruptedException {
        ObjectNode jsonRespone = Json.newObject();
        jsonRespone.put("name", "someName");
        when(mockWsResponse.asJson()).thenReturn(jsonRespone);

        exceptionRule.expect(ExecutionException.class);
        exceptionRule.expectMessage("doesn't have email");
        verifier.verify("someToken").toCompletableFuture().get();
    }

    @Test
    public void testMissingName() throws ExecutionException, InterruptedException {
        ObjectNode jsonRespone = Json.newObject();
        jsonRespone.put("email", "some@oneName");
        when(mockWsResponse.asJson()).thenReturn(jsonRespone);

        exceptionRule.expect(ExecutionException.class);
        exceptionRule.expectMessage("doesn't have name");
        verifier.verify("someToken").toCompletableFuture().get();
    }
}
