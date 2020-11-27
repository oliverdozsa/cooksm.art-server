package matchers;

import com.fasterxml.jackson.databind.JsonNode;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import play.libs.Json;
import play.mvc.Result;

import static play.test.Helpers.contentAsString;

public class ResultHasJwt extends TypeSafeMatcher<Result> {
    private ResultHasJwt() {

    }

    @Override
    protected boolean matchesSafely(Result item) {
        String jsonStr = contentAsString(item);
        JsonNode json = Json.parse(jsonStr);

        return json.get("jwtAuthToken") != null && !json.get("jwtAuthToken").isNull();
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("JWT to exist");
    }

    @Override
    protected void describeMismatchSafely(Result item, Description mismatchDescription) {
        mismatchDescription.appendText("JWT is not present");
    }

    public static ResultHasJwt hasJwt() {
        return new ResultHasJwt();
    }
}
