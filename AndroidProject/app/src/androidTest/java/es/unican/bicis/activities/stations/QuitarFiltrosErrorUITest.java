package es.unican.bicis.activities.stations;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static es.unican.bicis.utils.StationsUITestUtils.TUEBICI;
import static es.unican.bicis.utils.StationsUITestUtils.openStations;
import static org.hamcrest.CoreMatchers.allOf;

import android.Manifest;
import android.app.Activity;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

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
 * Prueba de interfaz de "Quitar filtros": mensaje de error al no poder quitar los filtros.
 * Comprueba que la pantalla de estaciones muestra el texto de error cuando el presenter
 * se lo pide con mostrarErrorQuitarFiltros().
 */
@HiltAndroidTest
@UninstallModules(LocationModule.class)
public class QuitarFiltrosErrorUITest {

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
    public void testErrorQuitarFiltrosMuestraMensaje() {
        // Se abre la pantalla de estaciones de la red de prueba
        openStations(TUEBICI);

        // Se pide a la pantalla de estaciones que muestre el error
        getInstrumentation().runOnMainSync(() -> {
            Activity actual = ActivityLifecycleMonitorRegistry.getInstance()
                    .getActivitiesInStage(Stage.RESUMED).iterator().next();
            ((StationsView) actual).mostrarErrorQuitarFiltros();
        });

        // Se ve el mensaje de error en lugar de la lista
        onView(withId(R.id.tvStationsMessage)).check(matches(allOf(
                isDisplayed(),
                withText(R.string.clearFilterError))));
    }
}