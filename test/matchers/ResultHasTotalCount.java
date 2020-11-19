package matchers;

import com.fasterxml.jackson.databind.JsonNode;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import play.libs.Json;
import play.mvc.Result;

import static play.test.Helpers.contentAsString;

public class ResultHasTotalCount extends TypeSafeMatcher<Result> {
    int expected;

    private ResultHasTotalCount(int expected) {
        this.expected = expected;
    }

    @Override
    protected boolean matchesSafely(Result item) {
        String jsonStr = contentAsString(item);
        JsonNode json = Json.parse(jsonStr);
        int actual = json.get("totalCount").asInt();

        return actual == expected;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("items size to be " + expected);
    }

    public static ResultHasTotalCount hasTotalCount(int expected) {
        return new ResultHasTotalCount(expected);
    }
}
