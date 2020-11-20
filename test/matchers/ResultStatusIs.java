package matchers;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import play.mvc.Result;

public class ResultStatusIs extends TypeSafeMatcher<Result> {
    private final int expected;
    private int actual;


    private ResultStatusIs(int expected) {
        this.expected = expected;
    }

    @Override
    protected boolean matchesSafely(Result item) {
        actual = item.status();
        return actual == expected;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("" + expected);
    }

    @Override
    protected void describeMismatchSafely(Result item, Description mismatchDescription) {
        mismatchDescription.appendText("was").appendValue(actual);
    }

    public static ResultStatusIs statusIs(int expectedStatus) {
        return new ResultStatusIs(expectedStatus);
    }
}
