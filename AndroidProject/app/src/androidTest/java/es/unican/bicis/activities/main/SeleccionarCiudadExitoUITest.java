package es.unican.bicis.activities.main;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static es.unican.bicis.utils.Matchers.listContainsStation;
import static es.unican.bicis.utils.Matchers.networkNamed;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

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
import es.unican.bicis.injection.LocationModule;
import es.unican.bicis.location.ILocationProvider;

/**
 * Prueba de interfaz UI.1 de la HU 532301 Seleccionar la ciudad (Prueba 1, caso de éxito).
 * El usuario pulsa la chincheta, elige «España» y «Madrid», y la lista muestra solo las dos
 * redes de Madrid (BiciMAD y Bici Metro). Pulsa BiciMAD, abre sus estaciones y, al volver
 * atrás, la lista sigue mostrando solo las redes de Madrid.
 * Se ejecuta sobre la versión de depuración con los datos de res/raw/networks.json.
 * Los números de los comentarios de la prueba son los pasos del caso UI.1 del plan de pruebas.
 */
@HiltAndroidTest
@UninstallModules(LocationModule.class)
public class SeleccionarCiudadExitoUITest {

    /** Una estación de BiciMAD (res/raw/network_bicimad.json) */
    private static final String ESTACION_BICIMAD = "377 - Metro Abrantes";

    /** Una estación de TUeBICI, que no debe aparecer entre las de BiciMAD */
    private static final String ESTACION_TUEBICI = "Perinés";

    /** Regla de Hilt para inyectar las dependencias de prueba */
    @Rule(order = 0)
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    /** Concede los permisos de ubicación para que no aparezca el diálogo del sistema */
    @Rule(order = 1)
    public GrantPermissionRule permisos = GrantPermissionRule.grant(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION);

    /** La ubicación falla, para que la app no abra la ciudad detectada (plan de pruebas) */
    @BindValue
    ILocationProvider ubicacionFalsa = cb -> cb.onFailure(new Exception("sin ubicacion"));

    /** Lanza la pantalla principal (lista de redes) antes de la prueba */
    @Rule(order = 2)
    public ActivityScenarioRule<MainView> activityRule =
            new ActivityScenarioRule<>(MainView.class);

    /**
     * UI.1: «España», «Madrid»; se pulsa BiciMAD y se vuelve atrás.
     */
    @Test
    public void testSeleccionarCiudadMuestraSusRedesYEstaciones() {

        // 1-2. Se pulsa la chincheta y aparecen los desplegables de país y ciudad
        onView(withId(R.id.menuItemSelectCity)).perform(click());
        onView(withId(R.id.spCountry)).check(matches(isDisplayed()));
        onView(withId(R.id.spCity)).check(matches(isDisplayed()));

        // 3-4. Se elige «España» en el desplegable de países
        seleccionarOpcion(R.id.spCountry, "España");
        onView(withId(R.id.spCountry)).check(matches(withSpinnerText("España")));

        // 5. Se elige «Madrid», que tiene dos redes
        seleccionarOpcion(R.id.spCity, "Madrid");
        onView(withId(R.id.spCity)).check(matches(withSpinnerText("Madrid")));

        // 6-8. La lista muestra BiciMAD y Bici Metro, y ninguna red de otra ciudad
        comprobarRedesDeMadrid();

        // 9-10. Se pulsa BiciMAD (abre su detalle) y después «Ver estaciones»
        onData(networkNamed("BiciMAD"))
                .inAdapterView(withId(R.id.lvNetworks))
                .perform(click());
        onView(withId(R.id.btnStations)).perform(click());

        // 11. Las estaciones mostradas son las de BiciMAD
        onView(withId(R.id.tvNetworkName)).check(matches(withText("BiciMAD")));
        onView(withId(R.id.lvStations)).check(matches(allOf(
                isDisplayed(),
                listContainsStation(ESTACION_BICIMAD),
                not(listContainsStation(ESTACION_TUEBICI)))));

        // 12-13. Se vuelve atrás (estaciones -> detalle -> lista principal de redes)
        pressBack();
        pressBack();
        onView(withId(R.id.lvNetworks)).check(matches(isDisplayed()));

        // 14. La lista sigue mostrando solo las redes de Madrid
        comprobarRedesDeMadrid();
    }

    /**
     * Abre un desplegable y pulsa la opción con el texto dado.
     * @param spinnerId id del desplegable (R.id.spCountry o R.id.spCity)
     * @param opcion texto de la opción que se elige
     */
    private void seleccionarOpcion(int spinnerId, String opcion) {
        onView(withId(spinnerId)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(opcion)))
                .inRoot(isPlatformPopup())
                .perform(click());
    }

    /**
     * Comprueba lo que se ve en pantalla: se muestran las dos redes de Madrid (BiciMAD y
     * Bici Metro) y no aparece ninguna red de otra ciudad, por ejemplo TUeBICI (Santander).
     */
    private void comprobarRedesDeMadrid() {
        onView(withText("BiciMAD")).check(matches(isDisplayed()));
        onView(withText("Bici Metro")).check(matches(isDisplayed()));
        onView(withText("TUeBICI")).check(doesNotExist());
    }
}
