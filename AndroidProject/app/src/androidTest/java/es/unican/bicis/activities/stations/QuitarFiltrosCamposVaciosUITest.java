package es.unican.bicis.activities.stations;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static es.unican.bicis.utils.StationsUITestUtils.TUEBICI;
import static es.unican.bicis.utils.StationsUITestUtils.applyFilter;
import static es.unican.bicis.utils.StationsUITestUtils.openFilterDialog;
import static es.unican.bicis.utils.StationsUITestUtils.openStations;

import android.Manifest;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import es.unican.bicis.R;
import es.unican.bicis.activities.main.MainView;

/**
 * HU 532317 - Quitar los filtros aplicados.
 *
 * UI.4: Volver a abrir los filtros después de quitarlos.
 */
@HiltAndroidTest
public class QuitarFiltrosCamposVaciosUITest {

    @Rule(order = 0)
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule(order = 1)
    public GrantPermissionRule permissionRule =
            GrantPermissionRule.grant(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            );

    @Rule(order = 2)
    public ActivityScenarioRule<MainView> activityRule =
            new ActivityScenarioRule<>(MainView.class);

    /**
     * UI.4 - HU 532317.
     *
     * Comprueba que el campo del filtro aparece vacío
     * después de quitar un filtro previamente aplicado.
     */
    @Test
    public void testUI4_CampoVacioDespuesDeQuitarFiltros() {


        openStations(TUEBICI);


        onView(withId(R.id.tvNetworkLocation))
                .check(matches(withText(
                        "Santander, ES \u00b7 42 estaciones")));


        applyFilter("5");


        onView(withId(R.id.tvNetworkLocation))
                .check(matches(withText(
                        "Santander, ES \u00b7 10 estaciones")));


        openFilterDialog();

        onView(withText(R.string.filterClear))
                .perform(click());


        onView(withId(R.id.tvNetworkLocation))
                .check(matches(withText(
                        "Santander, ES \u00b7 42 estaciones")));


        openFilterDialog();

        // Comprobar que el campo está visible.
        onView(withId(R.id.etMinFreeBikes))
                .check(matches(isDisplayed()));

        // Comprobar que el campo está vacío.
        onView(withId(R.id.etMinFreeBikes))
                .check(matches(withText("")));


        onView(withText(R.string.filterCancel))
                .perform(click());
    }
}
