package matchers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import play.libs.Json;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static play.test.Helpers.contentAsString;

public class ResultHasFavoriteRecipesWithIds extends TypeSafeMatcher<Result> {
    private List<Long> expectedIds;

    public ResultHasFavoriteRecipesWithIds(Long ... ids) {
        expectedIds = Arrays.asList(ids);
    }

    @Override
    protected boolean matchesSafely(Result item) {
        String jsonStr = contentAsString(item);
        ArrayNode json = (ArrayNode) Json.parse(jsonStr);
        List<Long> actualIds = new ArrayList<>();

        json.forEach(n -> actualIds.add(n.get("recipeId").asLong()));

        return expectedIds.size() == actualIds.size() && expectedIds.containsAll(actualIds);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("ids to be " + expectedIds);
    }

    public static ResultHasFavoriteRecipesWithIds hasFavoriteRecipesWithIds(Long ... ids) {
        return new ResultHasFavoriteRecipesWithIds(ids);
    }
}
