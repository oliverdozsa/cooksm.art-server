package extractors;

import com.fasterxml.jackson.databind.JsonNode;
import play.mvc.Result;

import java.util.List;

import static extractors.DataFromResult.toJson;

public class RecipeSearchesFromResult {
    public static String searchModeOfSingleRecipeSearchOf(Result result) {
        JsonNode queryJson = queryJsonOf(result);
        return queryJson.get("searchMode").asText();
    }

    public static int goodIngredientsOfSingleRecipeSearchOf(Result result) {
        JsonNode queryJson = queryJsonOf(result);
        return queryJson.get("goodIngs").asInt();
    }

    public static double goodIngredientsRatioOf(Result result) {
        JsonNode queryJson = queryJsonOf(result);
        return queryJson.get("goodIngsRatio").asDouble();
    }

    public static boolean useFavoritesOnlyOf(Result result) {
        JsonNode queryJson = queryJsonOf(result);
        return queryJson.get("useFavoritesOnly").asBoolean();
    }

    public static List<String> includedIngredientsOfSingleRecipeSearchOf(Result result) {
        return convertToNames("$.query.inIngs", result);
    }

    public static List<String> includedIngredientTagsOfSingleRecipeSearchOf(Result result) {
        return convertToNames("$.query.inIngTags", result);
    }

    public static List<String> excludedIngredientsOfSingleRecipeSearchOf(Result result) {
        return convertToNames("$.query.exIngs", result);
    }

    public static List<String> excludedIngredientTagsOfSingleRecipeSearchOf(Result result) {
        return convertToNames("$.query.exIngTags", result);
    }

    public static List<String> additionalIngredientsOfSingleRecipeSearchOf(Result result) {
        return convertToNames("$.query.addIngs", result);
    }

    public static List<String> additionalIngredientTagsOfSingleRecipeSearchOf(Result result) {
        return convertToNames("$.query.addIngTags", result);
    }

    public static List<String> sourcePagesOfSingleRecipeSearchOf(Result result) {
        return convertToNames("$.query.sourcePages", result);
    }

    public static List<String> recipeBooksOfSingleRecipeSearchOf(Result result) {
        return convertToNames("$.query.recipeBooks", result);
    }

    private static List<String> convertToNames(String field, Result result) {
        ListOfValuesFromResult<String> values = new ListOfValuesFromResult<String>()
                .select(field)
                .converting(n -> n.get("name").asText());

        return values.of(result);
    }

    private static JsonNode queryJsonOf(Result result) {
        JsonNode json = toJson(result);
        return json.get("query");
    }
}
