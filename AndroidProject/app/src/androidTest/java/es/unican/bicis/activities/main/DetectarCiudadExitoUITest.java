package es.unican.bicis.activities.main;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertNotNull;

import android.Manifest;
import android.content.Context;

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
 * Prueba 1 de la HU Detectar la ciudad segun mi ubicacion: caso de exito, se detecta la ciudad
 * y se abren directamente sus estaciones.
 */
@HiltAndroidTest
public class DetectarCiudadExitoUITest {

    /** Coordenadas de Santander, donde esta la red TUeBICI */
    private static final double SANTANDER_LATITUDE = 43.4623057;
    private static final double SANTANDER_LONGITUDE = -3.8099803;
    /** Tiempo maximo de espera de la ubicacion simulada, en milisegundos */
    private static final long LOCATION_TIMEOUT_MS = 15000;

    @Rule(order = 0)
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule(order = 1)
    public GrantPermissionRule permissions = GrantPermissionRule.grant(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION);

    private MockLocationManager mockLocationManager;
    private ActivityScenario<MainView> scenario;

    /**
     * Pone el GPS del dispositivo en Santander antes de abrir la aplicacion.
     * @throws InterruptedException si se interrumpe la espera
     */
    @Before
    public void setUp() throws InterruptedException {
        Context context = InstrumentationRegistry
                .getInstrumentation()
                .getTargetContext();

        mockLocationManager = new MockLocationManager(context);
        mockLocationManager.setUp();
        mockLocationManager.setLocation(
                SANTANDER_LATITUDE,
                SANTANDER_LONGITUDE
        );

        assertNotNull(
                "La aplicacion no ha recibido la ubicacion simulada",
                mockLocationManager.awaitMockedLocation(LOCATION_TIMEOUT_MS)
        );
    }

    /**
     * Cierra la aplicacion y deja el GPS como estaba.
     */
    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }

        if (mockLocationManager != null) {
            mockLocationManager.tearDown();
        }
    }

    /**
     * Prueba 1: el usuario esta en Santander, se abre directamente la lista de estaciones
     * de TUeBICI y sale el nombre de la ciudad.
     * @throws InterruptedException si se interrumpe la espera
     */
    @Test
    public void testDetectarCiudadCorrectamente() throws InterruptedException {
        scenario = ActivityScenario.launch(MainView.class);

        // la lista de estaciones se abre cuando llega la ubicacion, asi que se reintenta
        // hasta que aparezca (max 30 s)
        for (int i = 0; i < 60; i++) {
            try {
                onView(withId(R.id.tvNetworkName))
                        .check(matches(isDisplayed()))
                        .check(matches(withText("TUeBICI")));
                break;
            } catch (RuntimeException | AssertionError e) {
                if (i == 59) {
                    throw e;
                }
                Thread.sleep(500);
            }
        }

        // sale el nombre de la ciudad detectada
        onView(withId(R.id.tvNetworkLocation)).check(matches(withText(containsString("Santander"))));

        // se vuelve a la lista para que el siguiente test empiece limpio
        pressBack();
    }
}