package matchers;

import com.fasterxml.jackson.databind.JsonNode;

public class ResultHasFavoriteRecipesWithIds extends ResultHasJsonFieldWithValues<Long> {
    private ResultHasFavoriteRecipesWithIds(Long ... expected) {
        super("ids to be", expected);
    }

    @Override
    Long retrieveValue(JsonNode e) {
        return e.get("recipeId").asLong();
    }

    public static ResultHasFavoriteRecipesWithIds hasFavoriteRecipesWithIds(Long ... ids) {
        return new ResultHasFavoriteRecipesWithIds(ids);
    }
}
