package matchers;

import com.fasterxml.jackson.databind.JsonNode;

public class ResultHasIngredientTagsWithIds extends ResultHasJsonFieldWithValues<Long> {
    private ResultHasIngredientTagsWithIds(Long... expected) {
        super("$.items", "ingredient tag ids to be", expected);
    }

    @Override
    Long retrieveValue(JsonNode e) {
        return e.get("id").asLong();
    }

    public static ResultHasIngredientTagsWithIds hasIngredientTagsWithIds(Long... expected) {
        return new ResultHasIngredientTagsWithIds(expected);
    }
}
