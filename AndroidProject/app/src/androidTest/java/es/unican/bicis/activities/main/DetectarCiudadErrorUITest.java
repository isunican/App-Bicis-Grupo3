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
 * Prueba 2 de la HU Detectar la ciudad segun mi ubicacion
 */
@HiltAndroidTest
@UninstallModules(LocationModule.class)
public class DetectarCiudadErrorUITest {

    @Rule(order = 0)
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule(order = 1)
    public GrantPermissionRule permisos = GrantPermissionRule.grant(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION);

    // si es null la ubicacion falla
    private double[] ubicacion;

    @BindValue
    ILocationProvider ubicacionFalsa = cb -> {
        if (ubicacion == null) {
            cb.onFailure(new Exception("sin ubicacion"));
        } else {
            cb.onSuccess(ubicacion[0], ubicacion[1]);
        }
    };

    private ActivityScenario<MainView> escenario;
    private View decorView;

    /**
     * Cierra la aplicacion despues de cada test.
     */
    @After
    public void tearDown() {
        escenario.close();
    }

    /**
     * Abre la aplicacion y guarda su decorView para poder comprobar los Toast.
     */
    private void abrirApp() {
        escenario = ActivityScenario.launch(MainView.class);
        escenario.onActivity(activity -> decorView = activity.getWindow().getDecorView());
    }

    /**
     * Comprueba que sale el mensaje de error de ubicacion y que la lista tiene las 3 redes.
     * Como los Toast salen en cola, se reintenta durante 10 segundos como maximo.
     * @throws InterruptedException si se interrumpe la espera
     */
    private void comprobarMensajeErrorYLista() throws InterruptedException {
        for (int i = 0; i < 20; i++) {
            try {
                onView(withText(R.string.locationError))
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

    /**
     * A1.c.1: no se puede obtener la ubicacion del dispositivo.
     * @throws InterruptedException si se interrumpe la espera
     */
    @Test
    public void testUbicacionNoDisponible() throws InterruptedException {
        ubicacion = null;
        abrirApp();
        comprobarMensajeErrorYLista();
    }

    /**
     * A1.c.2: el usuario esta en Paris y no hay ninguna red a menos de 20 km.
     * @throws InterruptedException si se interrumpe la espera
     */
    @Test
    public void testUbicacionSinRedCerca() throws InterruptedException {
        ubicacion = new double[]{48.8566, 2.3522};
        abrirApp();
        comprobarMensajeErrorYLista();
    }
}