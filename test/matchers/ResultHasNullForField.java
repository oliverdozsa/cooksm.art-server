package matchers;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import play.mvc.Result;

import java.util.EnumSet;
import java.util.Set;

import static play.test.Helpers.contentAsString;

public class ResultHasNullForField extends TypeSafeMatcher<Result> {
    private String expected;

    public ResultHasNullForField(String expected) {
        this.expected = expected;
    }

    @Override
    protected boolean matchesSafely(Result item) {
        String jsonStr = contentAsString(item);
        JsonNode node = JsonPath.read(jsonStr, expected);
        return node.isNull();
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(expected + " field to be null");
    }

    @Override
    protected void describeMismatchSafely(Result item, Description mismatchDescription) {
        mismatchDescription.appendText("was not null");
    }

    public static ResultHasNullForField hasNullForField(String expected) {
        return new ResultHasNullForField(expected);
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
