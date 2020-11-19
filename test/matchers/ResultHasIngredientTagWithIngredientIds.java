package matchers;

import com.fasterxml.jackson.databind.JsonNode;

public class ResultHasIngredientTagWithIngredientIds extends ResultHasJsonFieldWithValues<Long> {
    private ResultHasIngredientTagWithIngredientIds(int index, Long... expected) {
        super("$.items[" + index + "].ingredients", "ingredient ids to be", expected);
    }

    @Override
    Long retrieveValue(JsonNode e) {
        return e.asLong();
    }

    public static ResultHasIngredientTagWithIngredientIds hasIngredientTagWithIngredientIds(int index, Long... expected) {
        return new ResultHasIngredientTagWithIngredientIds(index, expected);
    }
}
