package matchers;

import com.fasterxml.jackson.databind.JsonNode;

public class ResultHasIngredientTagWithIds extends ResultHasJsonFieldWithValues<Long> {
    private ResultHasIngredientTagWithIds(Long... expected) {
        super("$.items", "ingredient tag ids to be", expected);
    }

    @Override
    Long retrieveValue(JsonNode e) {
        return e.get("id").asLong();
    }

    public static ResultHasIngredientTagWithIds hasIngredientTagWithIds(Long... expected) {
        return new ResultHasIngredientTagWithIds(expected);
    }
}
