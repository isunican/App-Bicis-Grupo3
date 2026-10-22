package es.unican.bicis.activities.stations;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static es.unican.bicis.utils.Matchers.stationNamed;
import static es.unican.bicis.utils.StationsUITestUtils.TUEBICI;
import static es.unican.bicis.utils.StationsUITestUtils.applyFilter;
import static es.unican.bicis.utils.StationsUITestUtils.checkHeader;
import static es.unican.bicis.utils.StationsUITestUtils.openStations;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.startsWith;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import es.unican.bicis.R;
import es.unican.bicis.activities.main.MainView;

/** UI.4 - Prueba 4. Estaciones sin dato de bicis: filtro 1 sobre TUeBICI. */
@HiltAndroidTest
public class FiltrarEstacionesSinDatoUITest {

    private static final String SIN_DATO_1 = "Plaza Pombo (NE)";
    private static final String SIN_DATO_2 = "Calle Alta (NE)";

    @Rule(order = 0)
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule(order = 1)
    public ActivityScenarioRule<MainView> activityRule =
            new ActivityScenarioRule<>(MainView.class);

    @Test
    public void testUI4_EstacionesSinDatoNoAparecenConFiltro() {
        openStations(TUEBICI);

        // Lista completa: las dos estaciones sin dato van al final con un guion (formato "-/total")
        for (int pos : new int[]{40, 41}) {
            onData(anything()).inAdapterView(withId(R.id.lvStations)).atPosition(pos)
                    .onChildView(withId(R.id.tvStationBikes))
                    .check(matches(withText(startsWith("-/"))));
        }
        onData(stationNamed(SIN_DATO_1)).inAdapterView(withId(R.id.lvStations))
                .check(matches(isDisplayed()));
        onData(stationNamed(SIN_DATO_2)).inAdapterView(withId(R.id.lvStations))
                .check(matches(isDisplayed()));

        // Con filtro 1 desaparecen
        applyFilter("1");
        checkHeader("Santander, ES · 30 estaciones");
        onData(stationNamed(SIN_DATO_1)).inAdapterView(withId(R.id.lvStations))
                .check(doesNotExist());
        onData(stationNamed(SIN_DATO_2)).inAdapterView(withId(R.id.lvStations))
                .check(doesNotExist());
    }
}