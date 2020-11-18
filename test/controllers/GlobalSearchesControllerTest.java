package controllers;

import clients.GlobalSearchesTestClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.database.rider.core.api.dataset.DataSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.libs.Json;
import play.mvc.Result;
import rules.RuleChainForTests;
import utils.Base62Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static matchers.ResultHasGlobalSearchesWithNames.hasGlobalSearchesWithNames;
import static matchers.ResultHasGlobalSearchesWithSearchIds.hasGlobalSearchesWithSearchIds;
import static matchers.ResultHasGlobalSearchesWithUrlFriendlyNames.hasGlobalSearchesWithUrlFriendlyNames;
import static matchers.ResultHasJsonSize.hasJsonSize;
import static matchers.ResultStatusIs.statusIs;
import static org.junit.Assert.*;
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
        assertThat(result, statusIs(OK));
        assertThat(result, hasJsonSize(3));
        assertThat(result, hasGlobalSearchesWithNames("globalQuery1", "globalQuery2", "globalQuery3"));
        assertThat(result, hasGlobalSearchesWithUrlFriendlyNames("global-query-1", "global-query-2", "global-query-3"));
        assertThat(result, hasGlobalSearchesWithSearchIds(239328L, 239329L, 239330L));
    }
}
