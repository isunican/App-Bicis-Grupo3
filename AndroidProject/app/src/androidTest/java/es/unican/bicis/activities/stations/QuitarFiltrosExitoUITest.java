package es.unican.bicis.activities.stations;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static es.unican.bicis.utils.StationsUITestUtils.TUEBICI;
import static es.unican.bicis.utils.StationsUITestUtils.applyFilter;
import static es.unican.bicis.utils.StationsUITestUtils.openFilterDialog;
import static es.unican.bicis.utils.StationsUITestUtils.openStations;
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

@HiltAndroidTest
@UninstallModules(LocationModule.class)
public class QuitarFiltrosExitoUITest {

    @Rule(order = 0)
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule(order = 1)
    public GrantPermissionRule permissionRule =
            GrantPermissionRule.grant(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            );

    /** La app no recibe ubicación: se queda en la lista de redes y no salta a una ciudad */
    @BindValue
    ILocationProvider ubicacionFalsa = cb -> { };

    @Rule(order = 2)
    public ActivityScenarioRule<MainView> activityRule =
            new ActivityScenarioRule<>(MainView.class);

    /**
     * UI.1 - HU 532317.
     * Comprueba que al quitar el filtro se recuperan todas
     * las estaciones en el orden original de dichas estaciones.
     */
    @Test
    public void testUI1_QuitarFiltrosRestauraEstaciones() {

        openStations(TUEBICI);

        onView(withId(R.id.tvNetworkLocation))
                .check(matches(withText(
                        "Santander, ES \u00b7 42 estaciones")));

        List<String> originalOrder = getStationNames();

        assertEquals(42, originalOrder.size());

        // Aplicar un filtro de mínimo 5 bicicletas.
        applyFilter("5");

        // Comprobar que quedan 10 estaciones.
        onView(withId(R.id.tvNetworkLocation))
                .check(matches(withText(
                        "Santander, ES \u00b7 10 estaciones")));

        openFilterDialog();

        onView(withText(R.string.filterClear))
                .perform(click());

        // Comprobar que vuelven las 42 estaciones.
        onView(withId(R.id.tvNetworkLocation))
                .check(matches(withText(
                        "Santander, ES \u00b7 42 estaciones")));

        // Comprobar que se recupera exactamente el orden original.
        List<String> restoredOrder = getStationNames();

        assertEquals(originalOrder, restoredOrder);
    }

    /**
     * Obtiene los nombres de todas las estaciones
     * siguiendo el orden del adaptador.
     */
    private List<String> getStationNames() {

        List<String> names = new ArrayList<>();

        onView(withId(R.id.lvStations))
                .check((view, exception) -> {

                    if (exception != null) {
                        throw exception;
                    }

                    ListView listView = (ListView) view;
                    ListAdapter adapter = listView.getAdapter();

                    assertNotNull(adapter);

                    for (int i = 0; i < adapter.getCount(); i++) {

                        Station station =
                                (Station) adapter.getItem(i);

                        names.add(station.getName());
                    }
                });

        return names;
    }
}