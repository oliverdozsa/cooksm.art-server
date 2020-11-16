package rules;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import play.Logger;

public class TestMethodNameLogger implements TestRule {
    private static final Logger.ALogger logger = Logger.of(TestMethodNameLogger.class);

    @Override
    public Statement apply(Statement base, Description description) {
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("-- RUNNING TEST: {}", description.getMethodName());
        logger.info("------------------------------------------------------------------------------------------------");

        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                base.evaluate();
            }
        };
    }
}
