package matchers;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import play.mvc.Result;

public class ResultHasLocationHeader extends TypeSafeMatcher<Result> {
    @Override
    protected boolean matchesSafely(Result item) {
        return item.headers().get("Location") != null;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("\"Location\" header to exist");
    }

    public static ResultHasLocationHeader hasLocationHeader() {
        return new ResultHasLocationHeader();
    }
}
