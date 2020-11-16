package matchers;

import com.fasterxml.jackson.databind.JsonNode;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import play.libs.Json;
import play.mvc.Result;

import static play.test.Helpers.contentAsString;

public class ResultHasSingleFavoriteRecipeWithId extends TypeSafeMatcher<Result> {
    private Long expectedRecipeId;

    public ResultHasSingleFavoriteRecipeWithId(Long expectedRecipeId) {
        this.expectedRecipeId = expectedRecipeId;
    }

    @Override
    protected boolean matchesSafely(Result item) {
        String jsonStr = contentAsString(item);
        JsonNode json = Json.parse(jsonStr);
        Long actualRecipeId = json.get("recipeId").asLong();

        return expectedRecipeId.equals(actualRecipeId);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("recipe id to be " + expectedRecipeId);
    }

    public static ResultHasSingleFavoriteRecipeWithId hasSingleFavoriteRecipeWithId(Long expectedId) {
        return new ResultHasSingleFavoriteRecipeWithId(expectedId);
    }
}
