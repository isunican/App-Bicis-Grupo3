package es.unican.bicis.utils;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static es.unican.bicis.utils.Matchers.networkNamed;

import android.app.Activity;
import android.os.SystemClock;
import android.view.View;
import android.widget.CompoundButton;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import org.hamcrest.Matcher;

import es.unican.bicis.R;
import es.unican.bicis.activities.details.DetailsView;
import es.unican.bicis.activities.main.MainView;

/**
 * Pasos comunes de las pruebas de interfaz de la pantalla de estaciones.
 */
public final class StationsUITestUtils {

    /** Red de los datos de prueba (res/raw/network_tuebici.json) */
    public static final String TUEBICI = "TUeBICI";

    /** Ubicación simulada del usuario: centro de Santander, coordenadas de la red TUeBICI (HU 532304) */
    public static final double SANTANDER_LATITUDE = 43.4623057;
    public static final double SANTANDER_LONGITUDE = -3.8099803;

    /** Número máximo de comprobaciones (cada 100 ms) mientras se espera el foco: 30 s */
    private static final int MAX_FOCUS_CHECKS = 300;
    private static final long FOCUS_CHECK_WAIT_MS = 100;

    /** Número máximo de reintentos (cada 500 ms) mientras se espera una pantalla: 30 s */
    private static final int MAX_RETRIES = 60;
    private static final long RETRY_WAIT_MS = 500;

    private StationsUITestUtils() {
    }

    /**
     * Abre la pantalla de estaciones de la red dada: se pulsa la red en la lista de redes,
     * que abre su detalle, y después el botón "Ver estaciones" del detalle.
     */
    public static void openStations(String networkName) {
        waitForWindowFocus(MainView.class);
        onData(networkNamed(networkName))
                .inAdapterView(withId(R.id.lvNetworks))
                .perform(click());
        waitForWindowFocus(DetailsView.class);
        onView(withId(R.id.btnStations)).perform(click());
    }

    /**
     * Espera (máximo 30 s) a que la pantalla dada esté delante y tenga el foco, es decir,
     * a que haya terminado de abrirse y ya pueda recibir toques. Si se pulsa antes,
     * el emulador puede descartar el toque ("no touchable window") o el toque puede
     * llegar a la pantalla anterior, y el test falla aunque la aplicación funcione bien.
     * @param screen la clase de la pantalla (actividad) que se espera
     */
    public static void waitForWindowFocus(Class<? extends Activity> screen) {
        for (int i = 0; i < MAX_FOCUS_CHECKS; i++) {
            boolean[] focused = {false};
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                for (Activity activity : ActivityLifecycleMonitorRegistry.getInstance()
                        .getActivitiesInStage(Stage.RESUMED)) {
                    if (screen.isInstance(activity) && activity.hasWindowFocus()) {
                        focused[0] = true;
                    }
                }
            });
            if (focused[0]) {
                return;
            }
            SystemClock.sleep(FOCUS_CHECK_WAIT_MS);
        }
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

    /**
     * Abre el diálogo, escribe el valor en el campo "Mínimo de huecos libres" (HU 532335),
     * deja vacío el de bicis y pulsa Aplicar.
     */
    public static void applyEmptySlotsFilter(String value) {
        openFilterDialog();
        onView(withId(R.id.etMinFreeBikes)).perform(replaceText(""), closeSoftKeyboard());
        onView(withId(R.id.etMinEmptySlots)).perform(replaceText(value), closeSoftKeyboard());
        onView(withText(R.string.filterApply)).perform(click());
    }

    /** Comprueba el texto de la cabecera (ciudad, país y número de estaciones). */
    public static void checkHeader(String expected) {
        onView(withId(R.id.tvNetworkLocation)).check(matches(withText(expected)));
    }

    /**
     * Abre el diálogo, deja vacíos los campos de bicis y huecos, enciende el interruptor
     * "Filtrar por distancia", escribe la distancia máxima en km y pulsa Aplicar (HU 532304).
     * @param maxDistKm la distancia máxima en km, tal y como la escribiría el usuario
     */
    public static void applyDistanceFilter(String maxDistKm) {
        openFilterDialog();
        onView(withId(R.id.etMinFreeBikes)).perform(replaceText(""), closeSoftKeyboard());
        onView(withId(R.id.etMinEmptySlots)).perform(replaceText(""), closeSoftKeyboard());
        onView(withId(R.id.swDistanceFilter)).perform(setChecked(true));
        onView(withId(R.id.etMaxDistance)).perform(replaceText(maxDistKm), closeSoftKeyboard());
        onView(withText(R.string.filterApply)).perform(click());
    }

    /**
     * Espera (máximo 30 s) a que la cabecera de la pantalla de estaciones muestre el texto dado.
     * Se usa cuando la pantalla de estaciones se abre sola al detectar la ciudad, porque
     * Espresso no sabe cuándo va a aparecer.
     * @param expected el texto esperado en la cabecera
     */
    public static void waitForHeader(String expected) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                checkHeader(expected);
                return;
            } catch (RuntimeException | AssertionError e) {
                if (i == MAX_RETRIES - 1) {
                    throw e;
                }
                SystemClock.sleep(RETRY_WAIT_MS);
            }
        }
    }

    /**
     * Acción que deja un interruptor (o checkbox) encendido o apagado, sin importar cómo
     * estuviera antes. Con click() se cambiaría de estado, y si ya estaba encendido se apagaría.
     * @param checked true para encenderlo, false para apagarlo
     * @return la acción
     */
    public static ViewAction setChecked(boolean checked) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(CompoundButton.class);
            }

            @Override
            public String getDescription() {
                return "poner el interruptor a " + checked;
            }

            @Override
            public void perform(UiController uiController, View view) {
                ((CompoundButton) view).setChecked(checked);
                uiController.loopMainThreadUntilIdle();
            }
        };
    }
}