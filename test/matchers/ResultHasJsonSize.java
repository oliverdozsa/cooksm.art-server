package matchers;

import com.fasterxml.jackson.databind.JsonNode;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import play.libs.Json;
import play.mvc.Result;

import static play.test.Helpers.contentAsString;

public class ResultHasJsonSize extends TypeSafeMatcher<Result> {
    private int expectedSize;

    private ResultHasJsonSize(int expectedSize) {
        this.expectedSize = expectedSize;
    }

    @Override
    protected boolean matchesSafely(Result item) {
        JsonNode json = Json.parse(contentAsString(item));
        return json.size() == expectedSize;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("size to be " + expectedSize);
    }

    public static ResultHasJsonSize hasJsonSize(int expectedSize) {
        return new ResultHasJsonSize(expectedSize);
    }
}
