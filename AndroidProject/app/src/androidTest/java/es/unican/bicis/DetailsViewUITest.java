package es.unican.bicis;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

import android.Manifest;

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

        /** Pulsa la red con ese nombre en la lista principal, sin depender de su posición. */
        private void openDetails(String networkName) {
            onData(networkNamed(networkName))
                    .inAdapterView(withId(R.id.lvNetworks))
                    .perform(click());
        }

        /** Comprueba los cinco datos detallados de la vista. */
        private void checkDetails(String latitude, String longitude, String ebikes,
                String operatorsCount, String operators) {
            onView(withId(R.id.tvLatitude)).check(matches(withText(latitude)));
            onView(withId(R.id.tvLongitude)).check(matches(withText(longitude)));
            onView(withId(R.id.tvEbikes)).check(matches(withText(ebikes)));
            onView(withId(R.id.tvOperatorsCount)).check(matches(withText(operatorsCount)));
            onView(withId(R.id.tvOperators)).check(matches(withText(operators)));
        }

        /** A1.a.1: Bicing, red con todos los datos (Prueba 1). */
        @Test
        public void testA1a1_Bicing() {
            openDetails("Bicing");

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
}