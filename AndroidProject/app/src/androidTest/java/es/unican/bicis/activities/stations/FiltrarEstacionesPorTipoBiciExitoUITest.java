
package es.unican.bicis.activities.stations;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static es.unican.bicis.utils.StationsUITestUtils.TUEBICI;
import static es.unican.bicis.utils.StationsUITestUtils.checkHeader;
import static es.unican.bicis.utils.StationsUITestUtils.openFilterDialog;
import static es.unican.bicis.utils.StationsUITestUtils.openStations;

import static org.hamcrest.CoreMatchers.anything;
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
import es.unican.bicis.activities.main.MainView;
import es.unican.bicis.injection.LocationModule;
import es.unican.bicis.location.ILocationProvider;

/**
 * Prueba de interfaz UI.1 del plan US532305.
 *
 * Comprueba el filtrado de estaciones por bicicletas eléctricas
 * utilizando los datos locales de TUeBICI.
 *
 * El JSON actual contiene 20 estaciones con bicicletas eléctricas
 * disponibles. El plan original indica 19 y debe actualizarse.
 */
@HiltAndroidTest
@UninstallModules(LocationModule.class)
public class FiltrarEstacionesPorTipoBiciExitoUITest {

    @Rule(order = 0)
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule(order = 1)
    public GrantPermissionRule permisos = GrantPermissionRule.grant(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    );

    @BindValue
    ILocationProvider ubicacionFalsa = cb -> { };

    @Rule(order = 2)
    public ActivityScenarioRule<MainView> activityRule =
            new ActivityScenarioRule<>(MainView.class);

    /**
     * UI.1: seleccionar solo bicicletas eléctricas.
     *
     * Resultado esperado:
     * - La lista inicial contiene 42 estaciones.
     * - El filtro deja 20 estaciones con bicicletas eléctricas.
     * - Los nombres mostrados coinciden con los datos esperados.
     * - La selección se mantiene al volver a abrir el diálogo.
     */
    @Test
    public void testUI1_SoloElectricasMuestraEstacionesEsperadas() {

        // Abrir las estaciones de TUeBICI.
        openStations(TUEBICI);

        // Comprobar la lista inicial.
        checkHeader("Santander, ES · 42 estaciones");

        // Abrir el diálogo de filtros.
        openFilterDialog();

        // Ambas casillas deben estar inicialmente marcadas.
        onView(withId(R.id.cbElectricBike))
                .check(matches(isChecked()));

        onView(withId(R.id.cbMechanicalBike))
                .check(matches(isChecked()));

        // Seleccionar únicamente bicicletas eléctricas.
        onView(withId(R.id.cbMechanicalBike))
                .perform(click());

        onView(withId(R.id.cbElectricBike))
                .check(matches(isChecked()));

        onView(withId(R.id.cbMechanicalBike))
                .check(matches(not(isChecked())));

        // Aplicar el filtro.
        onView(withText(R.string.filterApply))
                .perform(click());

        // Comprobar que aparecen 20 estaciones.
        checkHeader("Santander, ES · 20 estaciones");

        // Nombres esperados según el JSON local de TUeBICI.
        String[] estacionesEsperadas = {
                "Ernest Lluch II (NE)",
                "Magdalena",
                "Alto Miranda",
                "Intercambiador Sardinero",
                "Parque Llamas",
                "Perinés",
                "Plaza de Italia",
                "Ayuntamiento (NE)",
                "Instituto Pereda (NE)",
                "Monte",
                "Plaza Manuel Llano",
                "Alisal (NE)",
                "Complejo Deportivo  Ruth Beitia",
                "Montaña 2",
                "San Román Corbán (NE)",
                "Tetuan",
                "Valdecilla",
                "Centro Cívico",
                "Sector 3 (NE)",
                "TUS (NE)"
        };


        // Comprobar los nombres que muestra la interfaz.
        // Espresso navega por las filas, sin leer los datos del Adapter.
        for (int i = 0; i < estacionesEsperadas.length; i++) {
            onData(anything())
                    .inAdapterView(withId(R.id.lvStations))
                    .atPosition(i)
                    .onChildView(withId(R.id.tvStationName))
                    .check(matches(withText(estacionesEsperadas[i])));
        }

        // Comprobar que la selección se mantiene.
        openFilterDialog();

        onView(withId(R.id.cbElectricBike))
                .check(matches(isChecked()));

        onView(withId(R.id.cbMechanicalBike))
                .check(matches(not(isChecked())));
    }
}
