package matchers;

import com.fasterxml.jackson.databind.JsonNode;

public class ResultHasSingleIngredientTagWithIngredientNames extends ResultHasJsonFieldWithValues<String> {
    private ResultHasSingleIngredientTagWithIngredientNames(String... expected) {
        super("$.ingredients", "ingredient names to be", expected);
    }

    @Override
    String retrieveValue(JsonNode e) {
        return e.get("name").asText();
    }

    public static ResultHasSingleIngredientTagWithIngredientNames
    hasSingleIngredientTagWithIngredientNames(String... expected) {
        return new ResultHasSingleIngredientTagWithIngredientNames(expected);
    }
}
