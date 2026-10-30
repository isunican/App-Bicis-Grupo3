package es.unican.bicis;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.os.SystemClock;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitor;
import java.util.ArrayList;
import java.util.List;
import es.unican.bicis.activities.details.DetailsView;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import org.junit.After;
import org.junit.Before;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.rule.GrantPermissionRule;


import static org.hamcrest.CoreMatchers.not;
import org.junit.Rule;
import org.junit.Test;

import dagger.hilt.android.testing.BindValue;
import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import dagger.hilt.android.testing.UninstallModules;

import es.unican.bicis.activities.main.MainView;
import es.unican.bicis.injection.LocationModule;
import es.unican.bicis.location.ILocationProvider;
import static es.unican.bicis.utils.Matchers.networkNamed;

@HiltAndroidTest
@UninstallModules(LocationModule.class)
public class DetailsViewUITest {

    @Rule(order = 0)
    public HiltAndroidRule hiltRule =
            new HiltAndroidRule(this);

    // para que no salga el dialogo del permiso de ubicacion al abrir la app
    @Rule(order = 1)
    public GrantPermissionRule permisos = GrantPermissionRule.grant(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION);

    // ubicacion que no responde, asi la app se queda en la lista de redes
    @BindValue
    ILocationProvider ubicacionFalsa = cb -> { };

    @Rule(order = 2)
    public ActivityScenarioRule<MainView> activityRule =
            new ActivityScenarioRule<>(MainView.class);

<<<<<<< Updated upstream
    /** Pulsa la red con ese nombre en la lista principal, sin depender de su posición. */
    private void openDetails(String networkName) {
        onData(networkNamed(networkName))
                .inAdapterView(withId(R.id.lvNetworks))
                .perform(click());
    }
=======
    /**
     * Cierra el DetailsView que abrió el test y espera a que se destruya,
     * para que el siguiente test empiece solo con su MainView.
     */
    @After
    public void closeDetails() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        instrumentation.runOnMainSync(() -> {
            for (Activity activity : openDetailsViews()) {
                activity.finish();
            }
        });
        // finish() es asincrono: esperar (maximo 5 s) a que no quede ningun DetailsView
        for (int i = 0; i < 50; i++) {
            instrumentation.waitForIdleSync();
            boolean[] stillOpen = {false};
            instrumentation.runOnMainSync(() -> stillOpen[0] = !openDetailsViews().isEmpty());
            if (!stillOpen[0]) {
                return;
            }
            SystemClock.sleep(100);
        }
    }

    /**
     * Espera (maximo 30 s) a que la MainView tenga el foco antes de empezar,
     * porque al principio de la ejecucion el emulador puede tardar mas de
     * los 10 s que espera Espresso.
     */
    @Before
    public void waitForWindowFocus() {
        for (int i = 0; i < 300; i++) {
            boolean[] focused = {false};
            activityRule.getScenario().onActivity(activity -> focused[0] = activity.hasWindowFocus());
            if (focused[0]) {
                return;
            }
            SystemClock.sleep(100);
        }
    }

    /** DetailsView que no estan destruidos. Hay que llamarlo en el hilo principal. */
    private static List<Activity> openDetailsViews() {
        List<Activity> result = new ArrayList<>();
        ActivityLifecycleMonitor monitor = ActivityLifecycleMonitorRegistry.getInstance();
        for (Stage stage : Stage.values()) {
            if (stage == Stage.DESTROYED) {
                continue;
            }
            for (Activity activity : monitor.getActivitiesInStage(stage)) {
                if (activity instanceof DetailsView) {
                    result.add(activity);
                }
            }
        }
        return result;
    }

        /** Pulsa la red con ese nombre en la lista principal, sin depender de su posición. */
        private void openDetails(String networkName) {
            onData(networkNamed(networkName))
                    .inAdapterView(withId(R.id.lvNetworks))
                    .perform(click());
        }
>>>>>>> Stashed changes

    /** Comprueba los cinco datos detallados de la vista. */
    private void checkDetails(String latitude, String longitude, String ebikes,
            String operatorsCount, String operators) {
        onView(withId(R.id.tvLatitude)).check(matches(withText(latitude)));
        onView(withId(R.id.tvLongitude)).check(matches(withText(longitude)));
        onView(withId(R.id.tvEbikes)).check(matches(withText(ebikes)));
        onView(withId(R.id.tvOperatorsCount)).check(matches(withText(operatorsCount)));
        onView(withId(R.id.tvOperators)).check(matches(withText(operators)));
    }

<<<<<<< Updated upstream
    /** A1.a.1: Bicing, red con todos los datos (Prueba 1). */
    @Test
    public void testA1a1_Bicing() {
        openDetails("Bicing");
=======


        /** A1.a.1: Bicing, red con todos los datos (Prueba 1). */
        @Test
        public void testA1a1_Bicing() {
            openDetails("Bicing");
>>>>>>> Stashed changes

        onView(withId(R.id.tvName)).check(matches(withText("Bicing")));
        onView(withId(R.id.tvCity)).check(matches(withText("Barcelona, ES")));
        checkDetails("41.3851", "2.1734", "Sí", "3",
                "• Barcelona de Serveis Municipals, S.A. (BSM)\n• CESPA\n• PBSC");
        onView(withId(R.id.tvNoDetails)).check(matches(not(isDisplayed())));
    }

    /** A1.b.1: BiciMAD, sin ebikes. Debe salir "-" y no "No" (Prueba 2). */
    @Test
    public void testA1b1_BiciMadSinEbikes() {
        openDetails("BiciMAD");

        onView(withId(R.id.tvCity)).check(matches(withText("Madrid, ES")));
        checkDetails("40.4168", "-3.7038", "-", "1",
                "• Empresa Municipal de Transportes de Madrid, S.A.");
    }

    /** A1.b.2: TUeBICI, sin ebikes (Prueba 2). */
    @Test
    public void testA1b2_TuebiciSinEbikes() {
        openDetails("TUeBICI");

        onView(withId(R.id.tvCity)).check(matches(withText("Santander, ES")));
        checkDetails("43.4623", "-3.8100", "-", "1", "• UTE BICIS SANTANDER");
    }

    /** A1.b.3: Bici Norte, sin longitude, ebikes ni company (Prueba 2). */
    @Test
    public void testA1b3_BiciNorteDatosAusentes() {
        openDetails("Bici Norte");

        onView(withId(R.id.tvCity)).check(matches(withText("Bilbao, ES")));
        checkDetails("43.2630", "-", "-", "-", "-");
        onView(withId(R.id.layoutDetails)).check(matches(isDisplayed()));
    }

    /** A1.a.2: Bici Oeste, ebikes false. Debe salir "No" (Prueba 1). */
    @Test
    public void testA1a2_BiciOesteEbikesFalse() {
        openDetails("Bici Oeste");

        onView(withId(R.id.tvCity)).check(matches(withText("Oviedo, ES")));
        checkDetails("43.3614", "-5.8494", "No", "2", "• Empresa A\n• Empresa B");
    }

    /** A1.c.1: Bici Coordenadas, latitud y longitud fuera de rango (Prueba 3). */
    @Test
    public void testA1c1_BiciCoordenadasFueraDeRango() {
        openDetails("Bici Coordenadas");

        checkDetails("-", "-", "Sí", "1", "• Empresa A");
    }

    /** A1.c.2: Bici Límites, coordenadas en los límites válidos (Prueba 3, AVL). */
    @Test
    public void testA1c2_BiciLimitesValoresLimite() {
        openDetails("Bici Límites");

        onView(withId(R.id.tvCity)).check(matches(withText("Ushuaia, AR")));
        checkDetails("-90.0000", "180.0000", "Sí", "1", "• Empresa A");
    }

    /** A1.c.3: Bici Anómala, ebikes "si" y company con un elemento vacío (Prueba 3). */
    @Test
    public void testA1c3_BiciAnomalaEbikesYCompanyAnomalos() {
        openDetails("Bici Anómala");

        checkDetails("42.5987", "-5.5671", "-", "-", "-");
    }

    /** A1.c.4: Bici Texto, company como texto y ebikes 1. La app no se cierra (Prueba 3). */
    @Test
    public void testA1c4_BiciTextoTiposIncorrectos() {
        openDetails("Bici Texto");

        onView(withId(R.id.tvName)).check(matches(withText("Bici Texto")));
        checkDetails("42.3439", "-3.6969", "-", "-", "-");
    }

    /** A1.d.1: Bici Sur, sin información detallada (Prueba 4). */
    @Test
    public void testA1d1_BiciSurSinInformacion() {
        openDetails("Bici Sur");

        onView(withId(R.id.tvName)).check(matches(withText("Bici Sur")));
        onView(withId(R.id.tvCity)).check(matches(withText("Cádiz, ES")));
        onView(withId(R.id.tvNoDetails)).check(matches(isDisplayed()));
        onView(withId(R.id.tvNoDetails))
                .check(matches(withText("No hay información detallada disponible para esta red")));
        onView(withId(R.id.layoutDetails)).check(matches(not(isDisplayed())));
    }
}