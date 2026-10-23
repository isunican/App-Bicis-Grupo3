package es.unican.bicis;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.Manifest;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.rule.GrantPermissionRule;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;

import dagger.hilt.android.testing.BindValue;
import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import dagger.hilt.android.testing.UninstallModules;

import es.unican.bicis.activities.main.MainView;
import es.unican.bicis.injection.LocationModule;
import es.unican.bicis.location.ILocationProvider;
import es.unican.bicis.model.Network;

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

    @Test
    public void testA1a1_BicingDatosBasicos() {

        // Seleccionar Bicing por nombre, sin depender de su posición
        onData(new TypeSafeMatcher<Network>() {

            @Override
            protected boolean matchesSafely(Network network) {
                return "Bicing".equals(network.getName());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Red de bicicletas Bicing");
            }
        })
                .inAdapterView(withId(R.id.lvNetworks))
                .perform(click());

        // Comprobar el nombre de la red
        onView(withId(R.id.tvName))
                .check(matches(withText("Bicing")));

        // Comprobar la ciudad
        onView(withId(R.id.tvCity))
                .check(matches(withText("Barcelona, ES")));
    }
}