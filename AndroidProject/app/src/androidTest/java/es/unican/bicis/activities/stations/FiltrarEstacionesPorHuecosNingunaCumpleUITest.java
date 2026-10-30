package es.unican.bicis.activities.stations;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static es.unican.bicis.utils.StationsUITestUtils.TUEBICI;
import static es.unican.bicis.utils.StationsUITestUtils.applyEmptySlotsFilter;
import static es.unican.bicis.utils.StationsUITestUtils.checkHeader;
import static es.unican.bicis.utils.StationsUITestUtils.openStations;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;

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
 * UI.2 del plan de pruebas de US532335 (Filtrar estaciones por huecos).
 * Prueba 3. Ninguna estación cumple el filtro: mínimo de 21 huecos libres sobre TUeBICI
 * (la que más tiene, "Renfe", tiene 20). Después se cambia el valor a 15.
 */
@HiltAndroidTest
@UninstallModules(LocationModule.class)
public class FiltrarEstacionesPorHuecosNingunaCumpleUITest {

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
    public void testUI2_FiltroVeintiunHuecosMuestraMensajeSinResultados() {
        openStations(TUEBICI);

        applyEmptySlotsFilter("21");

        // Se oculta la lista y se muestra el mensaje
        onView(withId(R.id.tvStationsMessage)).check(matches(allOf(
                isDisplayed(),
                withText("No hay estaciones con los huecos libres seleccionados"))));
        onView(withId(R.id.lvStations)).check(matches(not(isDisplayed())));
        onView(withText("Con al menos 21 huecos libres")).check(matches(isDisplayed()));

        // El usuario puede cambiar el valor y volver a aplicar el filtro
        applyEmptySlotsFilter("15");

        onView(withId(R.id.tvStationsMessage)).check(matches(not(isDisplayed())));
        onView(withId(R.id.lvStations)).check(matches(isDisplayed()));
        checkHeader("Santander, ES · 8 estaciones");
    }
}