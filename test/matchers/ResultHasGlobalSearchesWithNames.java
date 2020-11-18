package matchers;

import com.fasterxml.jackson.databind.JsonNode;

public class ResultHasGlobalSearchesWithNames extends ResultHasJsonFieldWithValues<String> {
    private ResultHasGlobalSearchesWithNames(String ... expected) {
        super("search names to be", expected);
    }

    @Override
    String retrieveValue(JsonNode e) {
        return e.get("name").asText();
    }

    public static ResultHasGlobalSearchesWithNames hasGlobalSearchesWithNames(String ... expected) {
        return new ResultHasGlobalSearchesWithNames(expected);
    }
}
