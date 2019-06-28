package controllers;

import com.github.database.rider.core.configuration.DataSetConfig;
import com.github.database.rider.core.connection.ConnectionHolderImpl;
import com.github.database.rider.core.dataset.DataSetExecutorImpl;
import com.github.database.rider.core.util.EntityManagerProvider;
import models.entities.Ingredient;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;

import java.util.List;

import static com.github.database.rider.core.util.EntityManagerProvider.em;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class TryingDbRiderTest extends WithApplication {
    private EntityManagerProvider emProvider;
    private DataSetExecutorImpl executor;

    @Before
    public void before(){
        emProvider = EntityManagerProvider.instance("openrecipesPU");
        executor = DataSetExecutorImpl.instance(new ConnectionHolderImpl(emProvider.connection()));
        DataSetConfig dataSetConfig = new DataSetConfig("datasets/yml/ingredients.yml");
        executor.createDataSet(dataSetConfig);
    }

    @Test
    public void shouldListUsers() {
        List<Ingredient> ingredients = em().createQuery("select i from Ingredient i").getResultList();
        assertThat(ingredients, notNullValue());
    }

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder().build();
    }

}
