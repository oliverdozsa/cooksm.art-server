package matchers;

import data.entities.User;
import io.ebean.Ebean;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.Optional;

public class UserExistsInDb extends TypeSafeMatcher<String> {
    private String actual;

    private UserExistsInDb() {

    }

    @Override
    protected boolean matchesSafely(String email) {
        actual = email;

        Optional<User> userOpt = Ebean.createQuery(User.class)
                .where()
                .eq("email", email)
                .findOneOrEmpty();

        return userOpt.isPresent();
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("user with email to exist in db").appendValue(actual);
    }

    @Override
    protected void describeMismatchSafely(String item, Description mismatchDescription) {
        mismatchDescription.appendText("user is not present");
    }

    public static UserExistsInDb existsInDb() {
        return new UserExistsInDb();
    }
}
