package es.unican.bicis.activities.stations;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static es.unican.bicis.utils.StationsUITestUtils.TUEBICI;
import static es.unican.bicis.utils.StationsUITestUtils.applyFilter;
import static es.unican.bicis.utils.StationsUITestUtils.openFilterDialog;
import static es.unican.bicis.utils.StationsUITestUtils.openStations;
import static org.hamcrest.Matchers.not;

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

/**
 * Pruebas de interfaz de la HU 532317 - Quitar los filtros aplicados.
 *
 * UI.2: Quitar filtros cuando ninguna estación cumple el filtro.
 */
@HiltAndroidTest
@UninstallModules(LocationModule.class)
public class QuitarFiltrosSinResultadosUITest {

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
     * UI.2 - HU 532317.
     * Comprueba que quitar un filtro sin resultados
     * restaura la lista completa de estaciones.
     */
    @Test
    public void testUI2_QuitarFiltroSinResultadosRestauraLista() {

        openStations(TUEBICI);

        onView(withId(R.id.tvNetworkLocation))
                .check(matches(withText(
                        "Santander, ES \u00b7 42 estaciones")));

        applyFilter("16");

        // Comprobar que aparece el mensaje de ausencia de resultados.
        onView(withId(R.id.tvStationsMessage))
                .check(matches(isDisplayed()));


        openFilterDialog();

        onView(withText(R.string.filterClear))
                .perform(click());


        onView(withId(R.id.tvNetworkLocation))
                .check(matches(withText(
                        "Santander, ES \u00b7 42 estaciones")));

        // Comprobar que el mensaje de ausencia de resultados
        // deja de estar visible.
        onView(withId(R.id.tvStationsMessage))
                .check(matches(not(isDisplayed())));

        onView(withId(R.id.lvStations))
                .check(matches(isDisplayed()));
    }
}