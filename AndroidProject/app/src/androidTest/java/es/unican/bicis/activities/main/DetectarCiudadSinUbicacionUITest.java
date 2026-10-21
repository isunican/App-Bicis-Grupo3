package es.unican.bicis.activities.main;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;

import android.Manifest;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.rule.GrantPermissionRule;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import dagger.hilt.android.testing.BindValue;
import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import dagger.hilt.android.testing.UninstallModules;
import es.unican.bicis.R;
import es.unican.bicis.injection.LocationModule;
import es.unican.bicis.location.ILocationProvider;

/**
 * Prueba 4 de la HU Detectar la ciudad segun mi ubicacion: no se puede obtener la ubicacion.
 */
@HiltAndroidTest
@UninstallModules(LocationModule.class)
public class DetectarCiudadSinUbicacionUITest {

    @Rule(order = 0)
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule(order = 1)
    public GrantPermissionRule permisos = GrantPermissionRule.grant(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION);

    // ubicacion falsa que siempre falla, como si el GPS no respondiera
    @BindValue
    ILocationProvider ubicacionFalsa = cb -> cb.onFailure(new Exception("sin ubicacion"));

    private ActivityScenario<MainView> escenario;
    private View decorView;

    /**
     * Cierra la aplicacion despues del test.
     */
    @After
    public void tearDown() {
        if (escenario != null) {
            escenario.close();
        }
    }

    /**
     * Prueba 4: no se puede obtener la ubicacion, sale su mensaje y se muestra la lista
     * con todas las ciudades.
     * @throws InterruptedException si se interrumpe la espera
     */
    @Test
    public void testNoSePuedeObtenerLaUbicacion() throws InterruptedException {
        escenario = ActivityScenario.launch(MainView.class);
        escenario.onActivity(activity -> decorView = activity.getWindow().getDecorView());

        // los Toast salen en cola, asi que se reintenta hasta que aparezca (max 10 s)
        for (int i = 0; i < 20; i++) {
            try {
                onView(withText(R.string.locationUnavailable))
                        .inRoot(withDecorView(not(decorView)))
                        .check(matches(isDisplayed()));
                break;
            } catch (RuntimeException | AssertionError e) {
                if (i == 19) {
                    throw e;
                }
                Thread.sleep(500);
            }
        }

        onView(withId(R.id.lvNetworks)).check(matches(hasChildCount(3)));
    }
}