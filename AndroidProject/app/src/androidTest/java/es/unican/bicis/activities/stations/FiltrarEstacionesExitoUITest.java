package es.unican.bicis.activities.stations;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static es.unican.bicis.utils.Matchers.hasNumberAtLeast;
import static es.unican.bicis.utils.StationsUITestUtils.TUEBICI;
import static es.unican.bicis.utils.StationsUITestUtils.applyFilter;
import static es.unican.bicis.utils.StationsUITestUtils.checkHeader;
import static es.unican.bicis.utils.StationsUITestUtils.openStations;
import static org.hamcrest.CoreMatchers.anything;

import android.Manifest;

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

/** UI.1 - Prueba 1. Caso de éxito: filtro 5 sobre TUeBICI. */
@HiltAndroidTest
@UninstallModules(LocationModule.class)
public class FiltrarEstacionesExitoUITest {

    @Rule(order = 0)
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    // para que no salga el dialogo del permiso de ubicacion al abrir la app
    @Rule(order = 1)
    public GrantPermissionRule permisos = GrantPermissionRule.grant(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION);

    // ubicacion que no responde, asi la app no detecta la ciudad y se queda en la lista de redes
    @BindValue
    ILocationProvider ubicacionFalsa = cb -> { };

    @Rule(order = 2)
    public ActivityScenarioRule<MainView> activityRule =
            new ActivityScenarioRule<>(MainView.class);

    @Test
    public void testUI1_FiltroCincoMuestraDiezEstaciones() {
        openStations(TUEBICI);
        applyFilter("5");

        checkHeader("Santander, ES · 10 estaciones");
        onView(withText("Con al menos 5 bicis libres")).check(matches(isDisplayed()));

        // La primera es la que más bicis tiene
        onData(anything()).inAdapterView(withId(R.id.lvStations)).atPosition(0)
                .onChildView(withId(R.id.tvStationName))
                .check(matches(withText("Nueva Montaña")));

        // Todas las mostradas tienen 5 o más bicis
        for (int i = 0; i < 10; i++) {
            onData(anything()).inAdapterView(withId(R.id.lvStations)).atPosition(i)
                    .onChildView(withId(R.id.tvStationBikes))
                    .check(matches(hasNumberAtLeast(5)));
        }
    }
}