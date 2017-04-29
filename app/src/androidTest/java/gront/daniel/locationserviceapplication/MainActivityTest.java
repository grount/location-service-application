package gront.daniel.locationserviceapplication;

import android.os.Build;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by Dani on 4/29/2017.
 */

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    @Rule
    public ActivityTestRule<MainActivity> activityTestRule
            = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void grantPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            getInstrumentation().getUiAutomation().executeShellCommand(
                    "pm grant " + getTargetContext().getPackageName()
                    + " android.permission.ACCESS_FINE_LOCATION");
        }
    }

    @Test
    public void checkCurrentUpdateText(){
        onView(withId(R.id.currentLocationRefreshImageButton)).perform((click()));
        onView(withId(R.id.currentLocationInputTextView)).check(matches(isDisplayed()));
    }

    @Test
    public void checkLatLngText()
    {
        onView(withId(R.id.latLongCurrentLocationRefreshImageButton)).perform(click());
        onView(withId(R.id.latInputTextView)).check(matches(isDisplayed()));
        onView(withId(R.id.longInputTextView)).check(matches(isDisplayed()));
        onView(withId(R.id.latLongCurrentLocationTextView)).check(matches(isDisplayed()));
        onView(withId(R.id.searchesInputTextView)).check(matches(isDisplayed()));
    }

    @Test (expected = RuntimeException.class )
    public void useAppContext() throws Exception {
        MainActivity activity = activityTestRule.getActivity();
        double lat = 0, lng = 0;

        activity.getAddress(lat, lng);
    }
}