package controllers;

import org.junit.Rule;
import play.Logger;
import rules.PlayApplicationWithGuiceDbRider;

public class SecurityControllerTest {
    @Rule
    public PlayApplicationWithGuiceDbRider application = new PlayApplicationWithGuiceDbRider();

    private static final Logger.ALogger logger = Logger.of(IngredientNamesControllerTest.class);
    private static final String RESOURCE_PATH = "/v1/security";

    // TODO
}