package matchers;

import com.fasterxml.jackson.databind.JsonNode;
import utils.Base62Utils;

public class ResultHasGlobalSearchesWithSearchIds extends ResultHasJsonFieldWithValues<String> {
    private ResultHasGlobalSearchesWithSearchIds(Long ... expected) {
        super("search ids to be", toEncodedIds(expected));

    }

    @Override
    String retrieveValue(JsonNode e) {
        return e.get("searchId").asText();
    }

    public static ResultHasGlobalSearchesWithSearchIds hasGlobalSearchesWithSearchIds(Long ... expected) {
        return new ResultHasGlobalSearchesWithSearchIds(expected);
    }

    private static String[] toEncodedIds(Long ... ids) {
        String[] encoded = new String[ids.length];

        for(int i = 0; i < ids.length; i++) {
            encoded[i] = Base62Utils.encode(ids[i]);
        }

        return encoded;
    }
}
