
package es.unican.bicis.activities.stations;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static es.unican.bicis.utils.StationsUITestUtils.TUEBICI;
import static es.unican.bicis.utils.StationsUITestUtils.openStations;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;

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
 * UI.2 - HU 532314.
 * Comprueba la sugerencia de una estación cuando el nombre no existe.
 */
@HiltAndroidTest
@UninstallModules(LocationModule.class)
public class BuscarEstacionPorNombreNoExisteUITest {

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
     * UI.2 - Buscar Magdalna, sugerir Magdalena y consultar su ubicación.
     */
    @Test
    public void testUI2_BusquedaSugiereMagdalena() {

        openStations(TUEBICI);

        // Abrir el buscador.
        onView(withContentDescription(R.string.menuSearch))
                .perform(click());

        // Buscar Magdalena con una letra de menos.
        onView(withId(androidx.appcompat.R.id.search_src_text))
                .perform(typeText("Magdalna"));

        // Comprobar que no se muestra la lista de estaciones.
        onView(withId(R.id.lvStations))
                .check(matches(withEffectiveVisibility(
                        androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE)));

        // Comprobar el mensaje de estación inexistente.
        onView(withText(R.string.searchNotFound))
                .check(matches(isDisplayed()));

        // Comprobar la sugerencia de Magdalena.
        onView(withText(R.string.searchSuggestionTitle))
                .check(matches(isDisplayed()));

        onView(withId(R.id.tvSuggestion))
                .check(matches(allOf(
                        isDisplayed(),
                        withText("Magdalena"))));

        // Pulsar la sugerencia.
        onView(withId(R.id.tvSuggestion))
                .perform(click());

        // Comprobar las coordenadas de Magdalena.
        onView(allOf(
                withText(containsString("43.4686")),
                withText(containsString("-3.7759"))
        )).check(matches(isDisplayed()));
    }
}
