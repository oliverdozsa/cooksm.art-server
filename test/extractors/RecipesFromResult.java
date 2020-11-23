package extractors;

import play.mvc.Result;

import java.util.List;

public class RecipesFromResult {
    public static List<Long> recipeIdsOfResult(Result result) {
        ListOfValuesFromResult<Long> values = new ListOfValuesFromResult<Long>()
                .select("$.items")
                .converting(n -> n.get("id").asLong());

        return values.of(result);
    }
}
