package matchers;

import com.fasterxml.jackson.databind.JsonNode;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import play.libs.Json;
import play.mvc.Result;

import static play.test.Helpers.contentAsString;

public class ResultHasSingleFavoriteRecipeWithId extends TypeSafeMatcher<Result> {
    private final Long expected;
    private Long actual;

    private ResultHasSingleFavoriteRecipeWithId(Long expected) {
        this.expected = expected;
    }

    @Override
    protected boolean matchesSafely(Result item) {
        String jsonStr = contentAsString(item);
        JsonNode json = Json.parse(jsonStr);
        actual = json.get("recipeId").asLong();

        return expected.equals(actual);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("recipe id to be " + expected);
    }

    @Override
    protected void describeMismatchSafely(Result item, Description mismatchDescription) {
        mismatchDescription.appendText("was").appendValue(actual);
    }

    public static ResultHasSingleFavoriteRecipeWithId hasSingleFavoriteRecipeWithId(Long expectedId) {
        return new ResultHasSingleFavoriteRecipeWithId(expectedId);
    }
}
