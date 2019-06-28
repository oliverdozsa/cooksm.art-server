package controllers;

import com.github.database.rider.core.api.dataset.DataSet;
import models.entities.Ingredient;
import org.junit.Rule;
import org.junit.Test;
import rules.PlayApplicationWithGuiceDbRider;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class TryingDbRiderTest {
    @Rule
    public PlayApplicationWithGuiceDbRider application = new PlayApplicationWithGuiceDbRider();

    @Test
    @DataSet(value = "datasets/yml/ingredients.yml")
    public void shouldListIngredients() {
        List<Ingredient> ingredients = application.getEmProvider().getEm().createQuery("select i from Ingredient i").getResultList();
        assertThat(ingredients, notNullValue());
        assertThat(ingredients.size(), equalTo(2));
    }
}
