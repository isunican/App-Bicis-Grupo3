package es.unican.bicis.activities.main;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;

import android.Manifest;
import android.content.Context;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import es.unican.bicis.R;
import es.unican.bicis.utils.MockLocationManager;

/**
 * Prueba 5 de la HU Detectar la ciudad segun mi ubicacion: no hay red de bicis en la ciudad del usuario.
 */
@HiltAndroidTest
public class DetectarCiudadSinRedUITest {

    /** Coordenadas de Reinosa, a mas de 20 km de cualquier red de networks.json */
    private static final double REINOSA_LATITUDE = 43.0018;
    private static final double REINOSA_LONGITUDE = -4.1376;

    /** Tiempo maximo de espera de la ubicacion simulada, en milisegundos */
    private static final long LOCATION_TIMEOUT_MS = 5_000;

    @Rule(order = 0)
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule(order = 1)
    public GrantPermissionRule permisos = GrantPermissionRule.grant(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION);

    private MockLocationManager mockLocationManager;
    private ActivityScenario<MainView> escenario;
    private View decorView;

    /**
     * Pone el GPS del dispositivo en Reinosa antes de abrir la aplicacion.
     * @throws InterruptedException si se interrumpe la espera
     */
    @Before
    public void setUp() throws InterruptedException {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        mockLocationManager = new MockLocationManager(context);
        mockLocationManager.setUp();
        mockLocationManager.setLocation(REINOSA_LATITUDE, REINOSA_LONGITUDE);
        assertNotNull("The application did not receive any GPS update",
                mockLocationManager.awaitMockedLocation(LOCATION_TIMEOUT_MS));
    }

    /**
     * Cierra la aplicacion y deja el GPS como estaba.
     */
    @After
    public void tearDown() {
        if (escenario != null) {
            escenario.close();
        }
        mockLocationManager.tearDown();
    }

    /**
     * Prueba 5: el usuario esta en Reinosa, sale el mensaje de que no hay red en su ciudad
     * y se muestra la lista con todas las ciudades.
     * @throws InterruptedException si se interrumpe la espera
     */
    @Test
    public void testNoHayRedEnLaCiudad() throws InterruptedException {
        escenario = ActivityScenario.launch(MainView.class);
        escenario.onActivity(activity -> decorView = activity.getWindow().getDecorView());

        // los Toast salen en cola, asi que se reintenta hasta que aparezca (max 10 s)
        for (int i = 0; i < 20; i++) {
            try {
                onView(withText(R.string.noNetworkNearby))
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