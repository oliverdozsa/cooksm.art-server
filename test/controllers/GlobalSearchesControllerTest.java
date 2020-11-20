package controllers;

import clients.GlobalSearchesTestClient;
import com.github.database.rider.core.api.dataset.DataSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.mvc.Result;
import rules.RuleChainForTests;

import static extractors.DataFromResult.sizeAsJsonOf;
import static extractors.DataFromResult.statusOf;
import static extractors.GlobalSearchesFromResult.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static play.test.Helpers.OK;

public class GlobalSearchesControllerTest {
    private final RuleChainForTests ruleChainForTests = new RuleChainForTests();

    @Rule
    public RuleChain chain = ruleChainForTests.getRuleChain();

    private GlobalSearchesTestClient client;

    @Before
    public void setup() {
        client = new GlobalSearchesTestClient(ruleChainForTests.getApplication());
    }

    @Test
    // Given
    @DataSet(value = "datasets/yml/globalsearches.yml", disableConstraints = true, cleanBefore = true)
    public void testAll() {
        // When
        Result result = client.all();

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(sizeAsJsonOf(result), equalTo(3));
        assertThat(globalSearchNamesOf(result), containsInAnyOrder("globalQuery1", "globalQuery2", "globalQuery3"));
        assertThat(globalSearchUrlFriendlyNamesOf(result), containsInAnyOrder("global-query-1", "global-query-2", "global-query-3"));
        assertThat(globalSearchIdsOf(result), containsInAnyOrder(239328L, 239329L, 239330L));
    }
}
