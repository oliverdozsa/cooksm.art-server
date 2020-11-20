package extractors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import play.libs.Json;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static play.test.Helpers.contentAsString;

class ListOfValuesFromResult<T> {
    private String selector;
    private Function<JsonNode, T> converter;

    public ListOfValuesFromResult<T> select(String selector) {
        this.selector = selector;
        return this;
    }

    public ListOfValuesFromResult<T> converting(Function<JsonNode, T> converter){
        this.converter = converter;
        return this;
    }

    public List<T> of(Result result) {
        String jsonStr = contentAsString(result);
        ArrayNode json = selectField(jsonStr);

        List<T> values = new ArrayList<>();
        json.forEach(n -> values.add(converter.apply(n)));
        return values;
    }

    private ArrayNode selectField(String jsonStr) {
        if(selector == null) {
            return (ArrayNode) Json.parse(jsonStr);
        } else {
            return JsonPath.read(jsonStr, selector);
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
