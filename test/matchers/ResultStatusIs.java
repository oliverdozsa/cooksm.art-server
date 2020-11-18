package matchers;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import play.mvc.Result;

public class ResultStatusIs extends TypeSafeMatcher<Result> {
    private int expectedStatus;

    private ResultStatusIs(int expectedStatus) {
        this.expectedStatus = expectedStatus;
    }

    @Override
    protected boolean matchesSafely(Result item) {
        return item.status() == expectedStatus;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("" + expectedStatus);
    }

    public static ResultStatusIs statusIs(int expectedStatus) {
        return new ResultStatusIs(expectedStatus);
    }
}
