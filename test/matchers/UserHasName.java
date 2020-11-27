package matchers;

import data.entities.User;
import io.ebean.Ebean;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class UserHasName extends TypeSafeMatcher<String> {
    private String expected;
    private String actual;

    private UserHasName(String expected) {
        this.expected = expected;
    }

    @Override
    protected boolean matchesSafely(String email) {
        User user = Ebean.createQuery(User.class)
                .where()
                .eq("email", email)
                .findOne();
        actual = user.getFullName();

        return actual.equals(expected);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("user name to be").appendValue(expected);
    }

    @Override
    protected void describeMismatchSafely(String item, Description mismatchDescription) {
        mismatchDescription.appendText("was").appendValue(actual);
    }

    public static UserHasName hasName(String expected) {
        return new UserHasName(expected);
    }
}
