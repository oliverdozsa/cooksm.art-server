package matchers;

import com.fasterxml.jackson.databind.JsonNode;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import play.libs.Json;
import play.mvc.Result;

import static play.test.Helpers.contentAsString;

public class ResultHasItemsSize extends TypeSafeMatcher<Result> {
    int expected;

    private ResultHasItemsSize(int expected) {
        this.expected = expected;
    }

    @Override
    protected boolean matchesSafely(Result item) {
        String jsonStr = contentAsString(item);
        JsonNode json = Json.parse(jsonStr);
        JsonNode items = json.get("items");

        return items.size() == expected;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("items size to be " + expected);
    }

    public static ResultHasItemsSize hasItemsSize(int expected) {
        return new ResultHasItemsSize(expected);
    }
}
