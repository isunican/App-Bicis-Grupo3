package es.unican.bicis.activities.stations;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static es.unican.bicis.utils.StationsUITestUtils.TUEBICI;
import static es.unican.bicis.utils.StationsUITestUtils.openFilterDialog;
import static es.unican.bicis.utils.StationsUITestUtils.openStations;
import static org.hamcrest.Matchers.not;
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
 * HU 532317 - Quitar los filtros aplicados.
 *
 * UI.3: No hay filtros aplicados.
 */
@HiltAndroidTest
@UninstallModules(LocationModule.class)
public class QuitarFiltrosSinFiltrosUITest {

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
     * UI.3 - HU 532317.
     * Comprueba que la opción de quitar filtros está
     * deshabilitada cuando no hay filtros activos.
     */
    @Test
    public void testUI3_SinFiltrosOpcionQuitarDeshabilitada() {


        openStations(TUEBICI);


        onView(withId(R.id.tvNetworkLocation))
                .check(matches(withText(
                        "Santander, ES \u00b7 42 estaciones")));


        List<String> originalOrder = getStationNames();

        assertEquals(42, originalOrder.size());


        openFilterDialog();


        onView(withText(R.string.filterClear))
                .check(matches(isDisplayed()));


        onView(withText(R.string.filterClear))
                .check(matches(not(isEnabled())));

        // Cerrar el diálogo y comprobar que el listado permanece sin modificaciones
        onView(withText(R.string.filterCancel))
                .perform(androidx.test.espresso.action.ViewActions.click());

        List<String> finalOrder = getStationNames();

        assertEquals(originalOrder, finalOrder);
    }

    /**
     * Recupera los nombres de las estaciones
     * en el orden del adaptador.
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