package extractors;

import play.mvc.Result;
import utils.Base62Utils;

import java.util.List;
import java.util.stream.Collectors;

public class GlobalSearchesFromResult {
    public static List<String> globalSearchNamesOf(Result result) {
        ListOfValuesFromResult<String> values = new ListOfValuesFromResult<String>()
                .converting(n -> n.get("name").asText());
        return values.of(result);
    }

    public static List<String> globalSearchUrlFriendlyNamesOf(Result result) {
        ListOfValuesFromResult<String> values = new ListOfValuesFromResult<String>()
                .converting(n -> n.get("urlFriendlyName").asText());
        return values.of(result);
    }

    public static List<Long> globalSearchIdsOf(Result result) {
        ListOfValuesFromResult<String> values = new ListOfValuesFromResult<String>()
                .converting(n -> n.get("searchId").asText());
        List<String> searchIdsStrs = values.of(result);
        return searchIdsStrs.stream().map(Base62Utils::decode)
                .collect(Collectors.toList());
    }
}
