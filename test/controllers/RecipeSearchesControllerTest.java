package controllers;

import org.junit.Rule;
import play.Logger;
import rules.PlayApplicationWithGuiceDbRider;

public class RecipeSearchesControllerTest {
    @Rule
    public PlayApplicationWithGuiceDbRider application = new PlayApplicationWithGuiceDbRider();

    private static final Logger.ALogger logger = Logger.of(IngredientNamesControllerTest.class);


}
