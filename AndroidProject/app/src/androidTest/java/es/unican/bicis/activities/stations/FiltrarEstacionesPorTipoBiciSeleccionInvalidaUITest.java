
package es.unican.bicis.activities.stations;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static es.unican.bicis.utils.StationsUITestUtils.TUEBICI;
import static es.unican.bicis.utils.StationsUITestUtils.openFilterDialog;
import static es.unican.bicis.utils.StationsUITestUtils.openStations;

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
 * Prueba de interfaz UI.4 del plan US532305.
 *
 * Comprueba que la aplicación impide desmarcar
 * los dos tipos de bicicleta simultáneamente.
 */
@HiltAndroidTest
@UninstallModules(LocationModule.class)
public class FiltrarEstacionesPorTipoBiciSeleccionInvalidaUITest {

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
     * UI.4: intentar desmarcar el último tipo seleccionado.
     *
     * Resultado esperado:
     * - Ambas casillas aparecen marcadas inicialmente.
     * - Se permite desmarcar Eléctrica.
     * - No se permite desmarcar Mecánica si es la última marcada.
     * - Mecánica permanece marcada.
     * - Eléctrica permanece desmarcada.
     */
    @Test
    public void testUI4_NoPermiteDesmarcarAmbosTipos() {

        // Abrir las estaciones de TUeBICI.
        openStations(TUEBICI);

        // Abrir el diálogo de filtros.
        openFilterDialog();

        // Comprobar el estado inicial.
        onView(withId(R.id.cbElectricBike))
                .check(matches(isChecked()));

        onView(withId(R.id.cbMechanicalBike))
                .check(matches(isChecked()));

        // Desmarcar Eléctrica.
        onView(withId(R.id.cbElectricBike))
                .perform(click());

        // Verificar que Eléctrica está desmarcada.
        onView(withId(R.id.cbElectricBike))
                .check(matches(not(isChecked())));

        // Verificar que Mecánica continúa marcada.
        onView(withId(R.id.cbMechanicalBike))
                .check(matches(isChecked()));

        // Intentar desmarcar Mecánica, último tipo seleccionado.
        onView(withId(R.id.cbMechanicalBike))
                .perform(click());

        // La aplicación debe impedir que ambas queden desmarcadas.
        onView(withId(R.id.cbMechanicalBike))
                .check(matches(isChecked()));

        // Eléctrica debe continuar desmarcada.
        onView(withId(R.id.cbElectricBike))
                .check(matches(not(isChecked())));
    }
}
