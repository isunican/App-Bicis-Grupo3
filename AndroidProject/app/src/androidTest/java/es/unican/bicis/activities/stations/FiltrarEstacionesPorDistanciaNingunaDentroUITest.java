package es.unican.bicis.activities.stations;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static es.unican.bicis.utils.StationsUITestUtils.SANTANDER_LATITUDE;
import static es.unican.bicis.utils.StationsUITestUtils.SANTANDER_LONGITUDE;
import static es.unican.bicis.utils.StationsUITestUtils.applyDistanceFilter;
import static es.unican.bicis.utils.StationsUITestUtils.checkHeader;
import static es.unican.bicis.utils.StationsUITestUtils.waitForHeader;
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
 * UI.2 del plan de pruebas de US532304 (Filtrar estaciones por distancia).
 * Prueba 3. Ninguna estación dentro de la distancia: distancia máxima de 0,05 km (50 m)
 * sobre TUeBICI, con el usuario en el centro de Santander (Anexo 1).
 */
@HiltAndroidTest
@UninstallModules(LocationModule.class)
public class FiltrarEstacionesPorDistanciaNingunaDentroUITest {

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
    public void testUI2_FiltroCincuentaMetrosMuestraMensajeSinResultados() {
        // La pantalla de estaciones de TUeBICI se abre sola al detectar la ciudad
        waitForHeader("Santander, ES · 42 estaciones");

        applyDistanceFilter("0.05");

        // Se oculta la lista (tampoco sale "Valdenoja 2 (NE)") y se muestra el mensaje
        onView(withId(R.id.tvStationsMessage)).check(matches(allOf(
                isDisplayed(),
                withText("No hay estaciones dentro de la distancia seleccionada"))));
        onView(withId(R.id.lvStations)).check(matches(not(isDisplayed())));
        onView(withText("A menos de 50 m")).check(matches(isDisplayed()));

        // El usuario cambia la distancia a 1 km: desaparece el mensaje y salen las 13 de la Prueba 1
        applyDistanceFilter("1");

        onView(withId(R.id.tvStationsMessage)).check(matches(not(isDisplayed())));
        onView(withId(R.id.lvStations)).check(matches(isDisplayed()));
        checkHeader("Santander, ES · 13 estaciones");
    }
}
