package rules;

import com.github.database.rider.core.DBUnitRule;
import com.github.database.rider.core.util.EntityManagerProvider;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.Helpers;

public class PlayApplicationWithGuiceDbRider implements TestRule {
    private EntityManagerProvider emProvider;
    private DBUnitRule dbUnitRule;
    private Application application;

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                startPlay();
                Statement emStatement = emProvider.apply(base, description);
                Statement dbUnitStatement = dbUnitRule.apply(emStatement, description);
                dbUnitStatement.evaluate();
                stopPlay();
            }
        };
    }

    public Application getApplication() {
        return application;
    }

    private void startPlay() {
        application = new GuiceApplicationBuilder().build();
        Helpers.start(application);
        emProvider = EntityManagerProvider.instance("openrecipesPU");
        dbUnitRule = DBUnitRule.instance(emProvider.connection());
    }

    private void stopPlay() {
        if (application != null) {
            Helpers.stop(application);
            application = null;
        }
    }
}
