package matchers;

import com.fasterxml.jackson.databind.JsonNode;

public class ResultHasIngredientNameWithAlts extends ResultHasJsonFieldWithValues<String> {
    private ResultHasIngredientNameWithAlts(int index, String ... expected) {
        super("$.items["+index+"].altNames", "alt name to be", expected);
    }

    @Override
    String retrieveValue(JsonNode e) {
        return e.asText();
    }

    public static ResultHasIngredientNameWithAlts hasIngredientNameWithAlts(int index, String ... expected) {
        return new ResultHasIngredientNameWithAlts(index, expected);
    }
}
