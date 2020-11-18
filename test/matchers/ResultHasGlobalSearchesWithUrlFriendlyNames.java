package matchers;

import com.fasterxml.jackson.databind.JsonNode;

public class ResultHasGlobalSearchesWithUrlFriendlyNames extends ResultHasJsonFieldWithValues<String> {
    private ResultHasGlobalSearchesWithUrlFriendlyNames(String ... expected) {
        super("url friendly names to be", expected);
    }

    @Override
    String retrieveValue(JsonNode e) {
        return e.get("urlFriendlyName").asText();
    }

    public static ResultHasGlobalSearchesWithUrlFriendlyNames hasGlobalSearchesWithUrlFriendlyNames(String ... expected) {
        return new ResultHasGlobalSearchesWithUrlFriendlyNames(expected);
    }
}
