package es.unican.bicis.activities.stations;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static es.unican.bicis.utils.Matchers.listContainsStation;
import static es.unican.bicis.utils.StationsUITestUtils.TUEBICI;
import static es.unican.bicis.utils.StationsUITestUtils.applyEmptySlotsFilter;
import static es.unican.bicis.utils.StationsUITestUtils.checkHeader;
import static es.unican.bicis.utils.StationsUITestUtils.openStations;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.Manifest;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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
 * UI.1 del plan de pruebas de US532335 (Filtrar estaciones por huecos).
 * Prueba 1. Caso de éxito: mínimo de 15 huecos libres sobre TUeBICI.
 */
@HiltAndroidTest
@UninstallModules(LocationModule.class)
public class FiltrarEstacionesPorHuecosExitoUITest {

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
    public void testUI1_FiltroQuinceHuecosMuestraOchoEstacionesOrdenadas() {
        openStations(TUEBICI);
        checkHeader("Santander, ES · 42 estaciones");

        applyEmptySlotsFilter("15");

        // Cabecera con el número de estaciones y subtítulo con el filtro activo
        checkHeader("Santander, ES · 8 estaciones");
        onView(withText("Con al menos 15 huecos libres")).check(matches(isDisplayed()));

        // La primera es la que más huecos tiene
        List<Station> stations = getShownStations();
        assertEquals(8, stations.size());
        assertEquals("Renfe", stations.get(0).getName());

        // Todas tienen 15 o más huecos y están ordenadas de más a menos huecos
        for (int i = 0; i < stations.size(); i++) {
            int emptySlots = stations.get(i).getEmptySlots();
            assertTrue(emptySlots >= 15);
            if (i > 0) {
                assertTrue(stations.get(i - 1).getEmptySlots() >= emptySlots);
            }
        }

        // "PTCAN 1" (12 huecos) no llega al mínimo
        onView(withId(R.id.lvStations)).check(matches(not(listContainsStation("PTCAN 1"))));
    }

    /**
     * Devuelve las estaciones que muestra la lista, en el orden del adaptador.
     * @return las estaciones mostradas
     */
    private List<Station> getShownStations() {
        List<Station> stations = new ArrayList<>();
        onView(withId(R.id.lvStations)).check((view, exception) -> {
            if (exception != null) {
                throw exception;
            }
            ListAdapter adapter = ((ListView) view).getAdapter();
            assertNotNull(adapter);
            for (int i = 0; i < adapter.getCount(); i++) {
                stations.add((Station) adapter.getItem(i));
            }
        });
        return stations;
    }
}