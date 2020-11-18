package rules;

import com.github.database.rider.core.DBUnitRule;
import com.github.database.rider.core.util.EntityManagerProvider;
import io.ebean.Ebean;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.Helpers;

public class PlayApplicationWithGuiceDbRiderRule implements TestRule {
    private EntityManagerProvider emProvider;
    private DBUnitRule dbUnitRule;
    private Application application;
    private GuiceApplicationBuilder appBuilder;

    public PlayApplicationWithGuiceDbRiderRule() {
        appBuilder = new GuiceApplicationBuilder();
    }

    public PlayApplicationWithGuiceDbRiderRule(GuiceApplicationBuilder appBuilder) {
        this.appBuilder = appBuilder;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                startPlay();
                try {
                    Statement emStatement = emProvider.apply(base, description);
                    Statement dbUnitStatement = dbUnitRule.apply(emStatement, description);
                    dbUnitStatement.evaluate();
                } finally {
                    stopPlay();
                }
            }
        };
    }

    public Application getApplication() {
        return application;
    }

    private void startPlay() {
        application = appBuilder.build();
        Helpers.start(application);
        emProvider = EntityManagerProvider.instance("openrecipesPU");
        dbUnitRule = DBUnitRule.instance(emProvider.connection());
    }

    private void stopPlay() {
        if (application != null) {
            Ebean.createSqlUpdate("DROP ALL OBJECTS").execute();
            Helpers.stop(application);
            application = null;
        }
    }
}
