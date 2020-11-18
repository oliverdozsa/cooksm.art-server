package controllers;

import com.github.database.rider.core.api.dataset.DataSet;
import org.junit.Rule;
import org.junit.Test;
import play.Logger;
import rules.PlayApplicationWithGuiceDbRiderRule;

public class IngredientTagsControllerTest_DeleteTest {
    @Rule
    public PlayApplicationWithGuiceDbRiderRule application = new PlayApplicationWithGuiceDbRiderRule();

    private static final Logger.ALogger logger = Logger.of(IngredientTagsControllerTest_DeleteTest.class);

    @Test
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUserDefined_Delete() {
        // TODO
    }

    @Test
    @DataSet(value = {"datasets/yml/ingredienttags.yml", "datasets/yml/ingredienttags-user-defined.yml"}, disableConstraints = true, cleanBefore = true)
    public void testUserDefined_Delete_InvalidId() {
        // TODO
    }
}
