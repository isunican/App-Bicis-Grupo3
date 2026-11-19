package es.unican.bicis.activities.main;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;

import dagger.hilt.android.testing.BindValue;
import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import dagger.hilt.android.testing.UninstallModules;
import es.unican.bicis.R;
import es.unican.bicis.injection.RepositoriesModule;
import es.unican.bicis.repository.INetworksRepository;
import es.unican.bicis.repository.NetworkDetailsCallback;
import es.unican.bicis.repository.NetworksCallback;
import es.unican.bicis.repository.StationsCallback;

/**
 * Prueba de interfaz UI.5 de la HU 532301 Seleccionar la ciudad (Prueba 5, error al cargar
 * las redes). Se sustituye el repositorio de depuración por uno que siempre falla, así que
 * la app no tiene redes. Al pulsar la chincheta se muestran los desplegables de país y ciudad
 * sin ninguna opción. El mensaje «No se han podido cargar las ciudades» es un Toast y,
 * como indica el plan de pruebas, no se comprueba.
 * Los números de los comentarios de la prueba son los pasos del caso UI.5 del plan de pruebas.
 */
@HiltAndroidTest
@UninstallModules(RepositoriesModule.class)
public class SeleccionarCiudadErrorCargaUITest {

    /** Regla de Hilt para inyectar las dependencias de prueba */
    @Rule(order = 0)
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    /**
     * Repositorio que falla al cargar las redes, como si no hubiera conexión.
     * Como no hay redes, la app no llega a pedir el permiso ni la ubicación.
     */
    @BindValue
    INetworksRepository repositorioSinRedes = new INetworksRepository() {
        /** Falla siempre, como si no hubiera conexión */
        @Override
        public void requestNetworks(NetworksCallback cb) {
            cb.onFailure(new Exception("sin conexion"));
        }

        /** No se usa en esta prueba; también falla */
        @Override
        public void requestNetwork(NetworkDetailsCallback cb, String networkId) {
            cb.onFailure(new Exception("sin conexion"));
        }

        /** No se usa en esta prueba; también falla */
        @Override
        public void requestStations(StationsCallback cb, String networkId) {
            cb.onFailure(new Exception("sin conexion"));
        }
    };

    /** Lanza la pantalla principal (lista de redes) antes de la prueba */
    @Rule(order = 1)
    public ActivityScenarioRule<MainView> activityRule =
            new ActivityScenarioRule<>(MainView.class);

    /**
     * UI.5: con un repositorio que falla, los dos desplegables aparecen sin ninguna opción.
     */
    @Test
    public void testErrorCargaLosDesplegablesNoTienenOpciones() {

        // 3. Se pulsa la chincheta de la lista principal de redes
        onView(withId(R.id.menuItemSelectCity)).perform(click());

        // 4. Se muestran los desplegables de país y ciudad
        onView(withId(R.id.llCitySelector)).check(matches(isDisplayed()));

        // 6. Ninguno de los dos desplegables tiene opciones (y no se pueden abrir)
        comprobarDesplegableDesactivado(R.id.spCountry);
        comprobarDesplegableDesactivado(R.id.spCity);
    }

    /**
     * Comprueba lo que se ve en pantalla: el desplegable se muestra, pero no está habilitado.
     * @param spinnerId id del desplegable (R.id.spCountry o R.id.spCity)
     */
    private void comprobarDesplegableDesactivado(int spinnerId) {
        onView(withId(spinnerId)).check(matches(allOf(isDisplayed(), not(isEnabled()))));
    }
}

