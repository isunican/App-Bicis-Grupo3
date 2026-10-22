package es.unican.bicis.utils;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static es.unican.bicis.utils.Matchers.networkNamed;

import es.unican.bicis.R;

/**
 * Pasos comunes de las pruebas de interfaz de la pantalla de estaciones.
 */
public final class StationsUITestUtils {

    /** Red de los datos de prueba (res/raw/network_tuebici.json) */
    public static final String TUEBICI = "TUeBICI";

    private StationsUITestUtils() {
    }

    /**
     * Abre la pantalla de estaciones de la red dada: se pulsa la red en la lista de redes,
     * que abre su detalle, y después el botón "Ver estaciones" del detalle.
     */
    public static void openStations(String networkName) {
        onData(networkNamed(networkName))
                .inAdapterView(withId(R.id.lvNetworks))
                .perform(click());
        onView(withId(R.id.btnStations)).perform(click());
    }

    /** Pulsa el icono del embudo de la barra superior. */
    public static void openFilterDialog() {
        onView(withId(R.id.menuItemFilter)).perform(click());
    }

    /** Escribe el valor en el campo del diálogo (replaceText admite también el vacío). */
    public static void typeInFilter(String value) {
        onView(withId(R.id.etMinFreeBikes)).perform(replaceText(value), closeSoftKeyboard());
    }

    /** Abre el diálogo, escribe el valor y pulsa Aplicar. */
    public static void applyFilter(String value) {
        openFilterDialog();
        typeInFilter(value);
        onView(withText(R.string.filterApply)).perform(click());
    }

    /** Comprueba el texto de la cabecera (ciudad, país y número de estaciones). */
    public static void checkHeader(String expected) {
        onView(withId(R.id.tvNetworkLocation)).check(matches(withText(expected)));
    }
}