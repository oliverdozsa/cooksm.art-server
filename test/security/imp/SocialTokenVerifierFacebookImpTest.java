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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class SocialTokenVerifierFacebookImpTest {
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

    private String mockClientId = "mockClientId";
    private String mockApiUrl = "mockApiUrl";
    private String mockFacebookSecret = "mockFacebookSecret";
    private String mockUserInfoUrl = "mockUserInfoUrl";

    private SocialTokenVerifierFacebookImp verifier;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        when(mockConfig.getString("facebook.clientid")).thenReturn(mockClientId);
        when(mockConfig.getString("facebook.apiurl")).thenReturn(mockApiUrl);
        when(mockConfig.getString("facebook.secret")).thenReturn(mockFacebookSecret);
        when(mockConfig.getString("facebook.userinfourl")).thenReturn(mockUserInfoUrl);
        when(mockWsClient.url(anyString())).thenReturn(mockWsRequest);
        when(mockWsRequest.get()).thenReturn(completedFuture(mockWsResponse));

        verifier = new SocialTokenVerifierFacebookImp(mockWsClient, mockConfig);
    }

    @Test
    public void testVerificationSuccessful() throws ExecutionException, InterruptedException {
        ObjectNode jsonResponseData = Json.newObject();
        jsonResponseData.put("is_valid", true);
        jsonResponseData.put("app_id", mockClientId);
        jsonResponseData.put("user_id", "4221");

        ObjectNode jsonRespone = Json.newObject();
        jsonRespone.set("data", jsonResponseData);

        ObjectNode userInfoJson = Json.newObject();
        userInfoJson.put("name", "Some One");
        userInfoJson.put("email", "some@one.com");
        userInfoJson.put("id", "4221");

        when(mockWsResponse.asJson())
                .thenReturn(jsonRespone)
                .thenReturn(userInfoJson);

        assertNotNull("Verification result should be not null!", verifier.verify("someToken").toCompletableFuture().get());
    }

    @Test
    public void testErrorResponse() throws ExecutionException, InterruptedException {
        ObjectNode jsonResponseData = Json.newObject();
        jsonResponseData.put("is_valid", false);

        ObjectNode jsonRespone = Json.newObject();
        jsonRespone.set("data", jsonResponseData);
        when(mockWsResponse.asJson()).thenReturn(jsonRespone);

        exceptionRule.expect(ExecutionException.class);
        exceptionRule.expectMessage("token is invalid");
        verifier.verify("someToken").toCompletableFuture().get();
    }

    @Test
    public void testNullResponse() throws ExecutionException, InterruptedException {
        when(mockWsResponse.asJson()).thenReturn(null);

        exceptionRule.expect(ExecutionException.class);
        exceptionRule.expectMessage("response is null");
        verifier.verify("someToken").toCompletableFuture().get();
    }

    @Test
    public void testClientIdMismatch() throws ExecutionException, InterruptedException {
        ObjectNode jsonResponseData = Json.newObject();
        jsonResponseData.put("is_valid", true);
        jsonResponseData.put("app_id", mockClientId + "someStringToMismatch");

        ObjectNode jsonRespone = Json.newObject();
        jsonRespone.set("data", jsonResponseData);
        when(mockWsResponse.asJson()).thenReturn(jsonRespone);

        exceptionRule.expect(ExecutionException.class);
        exceptionRule.expectMessage("client id mismatch");
        verifier.verify("someToken").toCompletableFuture().get();
    }

    @Test
    public void testNullData() throws ExecutionException, InterruptedException {
        ObjectNode jsonRespone = Json.newObject();
        when(mockWsResponse.asJson()).thenReturn(jsonRespone);

        exceptionRule.expect(ExecutionException.class);
        exceptionRule.expectMessage("doesn't have 'data' field");
        verifier.verify("someToken").toCompletableFuture().get();
    }
}
