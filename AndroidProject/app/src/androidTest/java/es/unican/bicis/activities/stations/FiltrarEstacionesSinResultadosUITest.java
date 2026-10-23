package es.unican.bicis.activities.stations;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static es.unican.bicis.utils.StationsUITestUtils.TUEBICI;
import static es.unican.bicis.utils.StationsUITestUtils.applyFilter;
import static es.unican.bicis.utils.StationsUITestUtils.openStations;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import es.unican.bicis.R;
import es.unican.bicis.activities.main.MainView;

/** UI.2 - Prueba 2. Sin resultados: filtro 16 sobre TUeBICI. */
@HiltAndroidTest
public class FiltrarEstacionesSinResultadosUITest {

    @Rule(order = 0)
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule(order = 1)
    public ActivityScenarioRule<MainView> activityRule =
            new ActivityScenarioRule<>(MainView.class);

    @Test
    public void testUI2_FiltroDieciseisMuestraMensajeSinResultados() {
        openStations(TUEBICI);
        applyFilter("16");

        onView(withId(R.id.tvStationsMessage)).check(matches(allOf(
                isDisplayed(),
                withText("No hay estaciones con al menos 16 bicis libres"))));
        onView(withId(R.id.lvStations)).check(matches(not(isDisplayed())));
        onView(withText("Con al menos 16 bicis libres")).check(matches(isDisplayed()));

        // La aplicación no se ha cerrado: la pantalla sigue respondiendo
        onView(withId(R.id.tvNetworkName)).check(matches(withText("TUeBICI")));
    }
}