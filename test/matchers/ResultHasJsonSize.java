package matchers;

import com.fasterxml.jackson.databind.JsonNode;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import play.libs.Json;
import play.mvc.Result;

import static play.test.Helpers.contentAsString;

public class ResultHasJsonSize extends TypeSafeMatcher<Result> {
    private final int expected;
    private int actual;

    private ResultHasJsonSize(int expected) {
        this.expected = expected;
    }

    @Override
    protected boolean matchesSafely(Result item) {
        JsonNode json = Json.parse(contentAsString(item));
        actual = json.size();
        return actual == expected;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("size to be").appendValue(expected);
    }

    @Override
    protected void describeMismatchSafely(Result item, Description mismatchDescription) {
        mismatchDescription.appendText("was").appendValue(actual);
    }

    public static ResultHasJsonSize hasJsonSize(int expectedSize) {
        return new ResultHasJsonSize(expectedSize);
    }
}
