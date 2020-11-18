package matchers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import play.libs.Json;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static play.test.Helpers.contentAsString;

abstract class ResultHasJsonFieldWithValues<T> extends TypeSafeMatcher<Result> {
    private final String fieldSelector;
    private final List<T> expectedValues;
    private final String failureText;

    @SafeVarargs
    ResultHasJsonFieldWithValues(String fieldSelector, String failureText, T ... expectedValues) {
        this.fieldSelector = fieldSelector;
        this.failureText = failureText;
        this.expectedValues = Arrays.asList(expectedValues);
    }

    @SafeVarargs
    ResultHasJsonFieldWithValues(String failureText, T ... expectedValues) {
        this(null, failureText, expectedValues);
    }

    @Override
    protected boolean matchesSafely(Result item) {
        String jsonStr = contentAsString(item);
        JsonNode json = Json.parse(jsonStr);
        ArrayNode jsonActualValues = selectField(json);

        List<T> actualValues = new ArrayList<>();
        jsonActualValues.forEach(n -> actualValues.add(retrieveValue(n)));

        return actualValues.size() == expectedValues.size() && expectedValues.containsAll(actualValues);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(failureText + " " + expectedValues);
    }

    abstract T retrieveValue(JsonNode e);

    private ArrayNode selectField(JsonNode json) {
        if(fieldSelector == null) {
            return (ArrayNode) json;
        } else {
            String[] fields = fieldSelector.split("\\.");
            JsonNode current = json;

            for(String field: fields) {
                current = current.get(field);
            }

            return (ArrayNode) current;
        }
    }
}
