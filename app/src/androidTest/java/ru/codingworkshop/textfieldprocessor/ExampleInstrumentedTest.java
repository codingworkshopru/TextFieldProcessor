package ru.codingworkshop.textfieldprocessor;

import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.is;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Rule
    public ActivityTestRule<MainActivity> activityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void emptyEmailTest() {
        onView(withId(R.id.button)).perform(click());
        onView(withId(R.id.textInputLayout)).check(matches(hasError(new EmptyFieldValidator().getErrorText())));
    }

    @Test
    public void invalidEmailTest() {
        onView(withClassName(is(TextInputEditText.class.getCanonicalName()))).perform(typeText("qwerty"));
        onView(withId(R.id.textInputLayout)).check(matches(hasError(new EmailValidator().getErrorText())));
    }

    @Test
    public void validEmailTest() {
        onView(withClassName(is(TextInputEditText.class.getCanonicalName()))).perform(typeText("qwerty@mail.com"));
        onView(withId(R.id.textInputLayout)).check(matches(withoutError()));
    }

    public static Matcher<View> hasError(String error) {
        return new BoundedMatcher<View, TextInputLayout>(TextInputLayout.class) {
            @Override
            public void describeTo(Description description) {

            }

            @Override
            protected boolean matchesSafely(TextInputLayout item) {
                String actual = item.getError().toString();
                return error.equals(actual);
            }
        };
    }

    public static Matcher<View> withoutError() {
        return new BoundedMatcher<View, TextInputLayout>(TextInputLayout.class) {
            @Override
            public void describeTo(Description description) {

            }

            @Override
            protected boolean matchesSafely(TextInputLayout item) {
                return item.getError() == null || item.getError().toString().isEmpty();
            }
        };
    }
}
