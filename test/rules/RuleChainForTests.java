package rules;

import org.junit.rules.RuleChain;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;

public class RuleChainForTests {
    private PlayApplicationWithGuiceDbRiderRule applicationRule;
    private TestMethodNameLoggerRule methodNameLoggerRule;
    private RuleChain ruleChain;

    public RuleChainForTests() {
        this(null);
    }

    public RuleChainForTests(GuiceApplicationBuilder appBuilder) {
        if(appBuilder != null) {
            applicationRule = new PlayApplicationWithGuiceDbRiderRule(appBuilder);
        } else {
            applicationRule = new PlayApplicationWithGuiceDbRiderRule();
        }

        methodNameLoggerRule = new TestMethodNameLoggerRule();
        ruleChain = RuleChain.outerRule(applicationRule)
                .around(methodNameLoggerRule);
    }

    public RuleChain getRuleChain() {
        return ruleChain;
    }

    public Application getApplication() {
        return applicationRule.getApplication();
    }
}
