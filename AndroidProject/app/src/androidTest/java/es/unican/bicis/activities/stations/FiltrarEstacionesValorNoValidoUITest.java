package es.unican.bicis.activities.stations;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static es.unican.bicis.utils.StationsUITestUtils.TUEBICI;
import static es.unican.bicis.utils.StationsUITestUtils.checkHeader;
import static es.unican.bicis.utils.StationsUITestUtils.openFilterDialog;
import static es.unican.bicis.utils.StationsUITestUtils.openStations;
import static es.unican.bicis.utils.StationsUITestUtils.typeInFilter;
import static org.hamcrest.CoreMatchers.containsString;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import es.unican.bicis.R;
import es.unican.bicis.activities.main.MainView;

/** UI.3 - Prueba 3. Valor no válido: filtro 0 sobre TUeBICI. */
@HiltAndroidTest
public class FiltrarEstacionesValorNoValidoUITest {

    @Rule(order = 0)
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule(order = 1)
    public ActivityScenarioRule<MainView> activityRule =
            new ActivityScenarioRule<>(MainView.class);

    @Test
    public void testUI3_FiltroCeroMuestraErrorYNoCambiaLaLista() {
        openStations(TUEBICI);
        openFilterDialog();
        typeInFilter("0");
        onView(withText(R.string.filterApply)).perform(click());

        // El diálogo sigue abierto y el campo muestra el error
        onView(withId(R.id.etMinFreeBikes)).check(matches(isDisplayed()));
        onView(withId(R.id.etMinFreeBikes))
                .check(matches(hasErrorText("Introduce un número entero mayor que 0")));

        // Al cancelar, la lista completa sigue sin filtrar
        onView(withText(R.string.filterCancel)).perform(click());
        checkHeader("Santander, ES · 42 estaciones");
        onView(withText(containsString("Con al menos"))).check(doesNotExist());
    }
}