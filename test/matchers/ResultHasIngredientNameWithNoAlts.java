package matchers;

import com.fasterxml.jackson.databind.JsonNode;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import play.libs.Json;
import play.mvc.Result;

import static play.test.Helpers.contentAsString;

public class ResultHasIngredientNameWithNoAlts extends TypeSafeMatcher<Result> {
    private final int index;
    private int actualSize;

    private ResultHasIngredientNameWithNoAlts(int index) {
        this.index = index;
    }

    @Override
    protected boolean matchesSafely(Result item) {
        String jsonStr = contentAsString(item);
        JsonNode json = Json.parse(jsonStr);

        JsonNode altNames = json
                .get("items")
                .get(index)
                .get("altNames");

        actualSize = altNames.size();

        return actualSize == 0;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("alt names to be empty");
    }

    @Override
    protected void describeMismatchSafely(Result item, Description mismatchDescription) {
        mismatchDescription.appendText("was").appendValue(actualSize);
    }

    public static ResultHasIngredientNameWithNoAlts hasIngredientNameWithNoAlts(int index) {
        return new ResultHasIngredientNameWithNoAlts(index);
    }
}
