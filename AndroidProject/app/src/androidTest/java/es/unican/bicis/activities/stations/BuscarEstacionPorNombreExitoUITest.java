
package es.unican.bicis.activities.stations;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static es.unican.bicis.utils.StationsUITestUtils.TUEBICI;
import static es.unican.bicis.utils.StationsUITestUtils.openStations;

import android.Manifest;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;

import dagger.hilt.android.testing.BindValue;
import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import dagger.hilt.android.testing.UninstallModules;
import es.unican.bicis.R;
import es.unican.bicis.activities.main.MainView;
import es.unican.bicis.injection.LocationModule;
import es.unican.bicis.location.ILocationProvider;
import es.unican.bicis.model.Station;

/**
 * UI.1 - HU 532314.
 * Comprueba la búsqueda de una estación existente y su ubicación.
 */
@HiltAndroidTest
@UninstallModules(LocationModule.class)
public class BuscarEstacionPorNombreExitoUITest {

    @Rule(order = 0)
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule(order = 1)
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION);

    @BindValue
    ILocationProvider ubicacionFalsa = cb -> { };

    @Rule(order = 2)
    public ActivityScenarioRule<MainView> activityRule =
            new ActivityScenarioRule<>(MainView.class);

    /**
     * UI.1 - Buscar Magdalena y consultar su ubicación.
     */
    @Test
    public void testUI1_BuscarMagdalenaMuestraUbicacion() {

        openStations(TUEBICI);

        onView(withId(R.id.lvStations))
                .check(matches(isDisplayed()));

        // Abrir el buscador y escribir Magdalena.
        onView(withContentDescription(R.string.menuSearch))
                .perform(click());

        onView(withId(androidx.appcompat.R.id.search_src_text))
                .perform(typeText("Magdalena"));

        // Comprobar que solo aparece Magdalena.
        onView(withId(R.id.lvStations))
                .check((view, exception) -> {
                    if (exception != null) {
                        throw exception;
                    }

                    ListAdapter adapter = ((ListView) view).getAdapter();

                    assertNotNull(adapter);
                    assertEquals(1, adapter.getCount());

                    Station station = (Station) adapter.getItem(0);
                    assertEquals("Magdalena", station.getName());
                });

        // Seleccionar Magdalena desde los resultados del buscador.
        onData(anything())
                .inRoot(isPlatformPopup())
                .atPosition(0)
                .perform(click());

        // Comprobar las coordenadas.
        onView(allOf(
                androidx.test.espresso.matcher.ViewMatchers.withText(
                        containsString("43.4686")),
                androidx.test.espresso.matcher.ViewMatchers.withText(
                        containsString("-3.7759"))
        )).check(matches(isDisplayed()));
    }
}
