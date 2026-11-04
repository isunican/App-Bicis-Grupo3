package es.unican.bicis.activities.stations;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static es.unican.bicis.utils.Matchers.listContainsStation;
import static es.unican.bicis.utils.Matchers.stationNamed;
import static es.unican.bicis.utils.StationsUITestUtils.SANTANDER_LATITUDE;
import static es.unican.bicis.utils.StationsUITestUtils.SANTANDER_LONGITUDE;
import static es.unican.bicis.utils.StationsUITestUtils.applyDistanceFilter;
import static es.unican.bicis.utils.StationsUITestUtils.checkHeader;
import static es.unican.bicis.utils.StationsUITestUtils.waitForHeader;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
 * UI.1 del plan de pruebas de US532304 (Filtrar estaciones por distancia).
 * Prueba 1. Caso de éxito: distancia máxima de 1 km sobre TUeBICI, con el usuario
 * en el centro de Santander (Anexo 1).
 */
@HiltAndroidTest
@UninstallModules(LocationModule.class)
public class FiltrarEstacionesPorDistanciaExitoUITest {

    @Rule(order = 0)
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    // para que no salga el dialogo del permiso de ubicacion al abrir la app
    @Rule(order = 1)
    public GrantPermissionRule permisos = GrantPermissionRule.grant(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION);

    // el usuario esta en el centro de Santander: la app detecta TUeBICI y abre sus estaciones,
    // y la pantalla de estaciones usa esta misma ubicacion para el filtro por distancia
    @BindValue
    ILocationProvider ubicacionFalsa = cb -> cb.onSuccess(SANTANDER_LATITUDE, SANTANDER_LONGITUDE);

    @Rule(order = 2)
    public ActivityScenarioRule<MainView> activityRule =
            new ActivityScenarioRule<>(MainView.class);

    @Test
    public void testUI1_FiltroUnKmMuestraTreceEstacionesOrdenadasPorDistancia() {
        // La pantalla de estaciones de TUeBICI se abre sola al detectar la ciudad
        waitForHeader("Santander, ES · 42 estaciones");

        applyDistanceFilter("1");

        // Cabecera con el número de estaciones y subtítulo con el filtro activo
        checkHeader("Santander, ES · 13 estaciones");
        onView(withText("A menos de 1,0 km")).check(matches(isDisplayed()));

        // De la más cercana a la más lejana, y al final la que no tiene coordenadas
        List<Station> stations = getShownStations();
        assertEquals(13, stations.size());
        assertEquals("Ayuntamiento (NE)", stations.get(0).getName());
        assertEquals("Tabacalera/Varadero", stations.get(11).getName());
        assertEquals("Valdenoja 2 (NE)", stations.get(12).getName());

        // La distancia aparece junto al nombre ("-" si no se puede calcular)
        checkDistanceShown("Ayuntamiento (NE)", "81 m");
        checkDistanceShown("Tabacalera/Varadero", "967 m");
        checkDistanceShown("Valdenoja 2 (NE)", "-");

        // "Instituto Pereda (NE)" está a 1,06 km, fuera de la distancia
        onView(withId(R.id.lvStations))
                .check(matches(not(listContainsStation("Instituto Pereda (NE)"))));
    }

    /**
     * Comprueba el texto de la distancia que se muestra junto al nombre de una estación.
     * @param stationName el nombre de la estación
     * @param expected la distancia esperada, como se ve en la lista
     */
    private void checkDistanceShown(String stationName, String expected) {
        onData(stationNamed(stationName))
                .inAdapterView(withId(R.id.lvStations))
                .onChildView(withId(R.id.tvStationDistance))
                .check(matches(withText(expected)));
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
