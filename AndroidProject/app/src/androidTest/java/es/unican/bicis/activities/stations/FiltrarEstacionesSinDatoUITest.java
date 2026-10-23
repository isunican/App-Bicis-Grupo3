package es.unican.bicis.activities.stations;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static es.unican.bicis.utils.Matchers.listContainsStation;
import static es.unican.bicis.utils.Matchers.stationNamed;
import static es.unican.bicis.utils.StationsUITestUtils.TUEBICI;
import static es.unican.bicis.utils.StationsUITestUtils.applyFilter;
import static es.unican.bicis.utils.StationsUITestUtils.checkHeader;
import static es.unican.bicis.utils.StationsUITestUtils.openStations;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;

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

/** UI.4 - Prueba 4. Estaciones sin dato de bicis: filtro 1 sobre TUeBICI. */
@HiltAndroidTest
@UninstallModules(LocationModule.class)
public class FiltrarEstacionesSinDatoUITest {

    private static final String SIN_DATO_1 = "Plaza Pombo (NE)";
    private static final String SIN_DATO_2 = "Calle Alta (NE)";

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
        onView(withId(R.id.lvStations))
                .check(matches(not(listContainsStation(SIN_DATO_1))));
        onView(withId(R.id.lvStations))
                .check(matches(not(listContainsStation(SIN_DATO_2))));
    }
}