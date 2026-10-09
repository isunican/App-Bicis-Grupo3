
package es.unican.bicis;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;

import es.unican.bicis.activities.main.MainView;
import es.unican.bicis.model.Network;

@HiltAndroidTest
public class DetailsViewUITest {

    @Rule(order = 0)
    public HiltAndroidRule hiltRule =
            new HiltAndroidRule(this);

    @Rule(order = 1)
    public ActivityScenarioRule<MainView> activityRule =
            new ActivityScenarioRule<>(MainView.class);

    @Test
    public void testA1a1_BicingDatosBasicos() {

        // Seleccionar Bicing por nombre, sin depender de su posición
        onData(new TypeSafeMatcher<Network>() {

            @Override
            protected boolean matchesSafely(Network network) {
                return "Bicing".equals(network.getName());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Red de bicicletas Bicing");
            }
        })
                .inAdapterView(withId(R.id.lvNetworks))
                .perform(click());

        // Comprobar el nombre de la red
        onView(withId(R.id.tvName))
                .check(matches(withText("Bicing")));

        // Comprobar la ciudad
        onView(withId(R.id.tvCity))
                .check(matches(withText("Barcelona")));
    }
}
