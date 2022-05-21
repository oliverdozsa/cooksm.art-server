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

    private String mockUserInfoUrl = "mockUserInfoUrl";

    private SocialTokenVerifierFacebookImp verifier;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        when(mockConfig.getString("facebook.userinfourl")).thenReturn(mockUserInfoUrl);
        when(mockWsClient.url(anyString())).thenReturn(mockWsRequest);
        when(mockWsRequest.get()).thenReturn(completedFuture(mockWsResponse));

        verifier = new SocialTokenVerifierFacebookImp(mockWsClient, mockConfig);
    }

    @Test
    public void testVerificationSuccessful() throws ExecutionException, InterruptedException {
        ObjectNode userInfoJson = Json.newObject();
        userInfoJson.put("name", "Some One");
        userInfoJson.put("email", "some@one.com");
        userInfoJson.put("id", "4221");

        ObjectNode pictureDataJson = Json.newObject();
        pictureDataJson.put("url", "some-url");

        ObjectNode pictureJson = Json.newObject();
        pictureJson.set("data", pictureDataJson);

        userInfoJson.set("picture", pictureJson);

        when(mockWsResponse.asJson())
                .thenReturn(userInfoJson);

        assertNotNull("Verification result should be not null!", verifier.verify("someToken").toCompletableFuture().get());
    }

    @Test
    public void testMissingEmail() throws ExecutionException, InterruptedException {
        ObjectNode userInfoJson = Json.newObject();
        userInfoJson.put("name", "Some One");
        userInfoJson.put("id", "4221");

        ObjectNode pictureDataJson = Json.newObject();
        pictureDataJson.put("url", "some-url");

        ObjectNode pictureJson = Json.newObject();
        pictureJson.set("data", pictureDataJson);

        userInfoJson.set("picture", pictureJson);

        when(mockWsResponse.asJson()).thenReturn(userInfoJson);

        exceptionRule.expect(ExecutionException.class);
        exceptionRule.expectMessage("doesn't have email");
        verifier.verify("someToken").toCompletableFuture().get();
    }

    @Test
    public void testMissingPicture() throws ExecutionException, InterruptedException {
        ObjectNode userInfoJson = Json.newObject();
        userInfoJson.put("name", "Some One");
        userInfoJson.put("email", "some@one.com");
        userInfoJson.put("id", "4221");

        when(mockWsResponse.asJson()).thenReturn(userInfoJson);

        exceptionRule.expect(ExecutionException.class);
        exceptionRule.expectMessage("doesn't have picture");
        verifier.verify("someToken").toCompletableFuture().get();
    }

    @Test
    public void testMissingName() throws ExecutionException, InterruptedException {
        ObjectNode userInfoJson = Json.newObject();
        userInfoJson.put("email", "some@one.com");
        userInfoJson.put("id", "4221");

        ObjectNode pictureDataJson = Json.newObject();
        pictureDataJson.put("url", "some-url");

        ObjectNode pictureJson = Json.newObject();
        pictureJson.set("data", pictureDataJson);

        userInfoJson.set("picture", pictureJson);

        when(mockWsResponse.asJson()).thenReturn(userInfoJson);

        exceptionRule.expect(ExecutionException.class);
        exceptionRule.expectMessage("doesn't have name");
        verifier.verify("someToken").toCompletableFuture().get();
    }

    @Test
    public void testNullResponse() throws ExecutionException, InterruptedException {
        when(mockWsResponse.asJson()).thenReturn(null);

        exceptionRule.expect(ExecutionException.class);
        exceptionRule.expectMessage("response is null");
        verifier.verify("someToken").toCompletableFuture().get();
    }
}
