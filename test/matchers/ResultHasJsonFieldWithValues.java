package matchers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import play.libs.Json;
import play.mvc.Result;

import java.util.*;

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
        ArrayNode jsonActualValues = selectField(jsonStr);

        List<T> actualValues = new ArrayList<>();
        jsonActualValues.forEach(n -> actualValues.add(retrieveValue(n)));

        return actualValues.size() == expectedValues.size() && expectedValues.containsAll(actualValues);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(failureText + " " + expectedValues);
    }

    abstract T retrieveValue(JsonNode e);

    private ArrayNode selectField(String jsonStr) {
        if(fieldSelector == null) {
            return (ArrayNode) Json.parse(jsonStr);
        } else {
            return JsonPath.read(jsonStr, fieldSelector);
        }
    }

    static {
        Configuration.setDefaults(new Configuration.Defaults() {
            private final JsonProvider jsonProvider = new JacksonJsonNodeJsonProvider();
            private final MappingProvider mappingProvider = new JacksonMappingProvider();

            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public MappingProvider mappingProvider() {
                return mappingProvider;
            }

            @Override
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }
        });
    }
}
