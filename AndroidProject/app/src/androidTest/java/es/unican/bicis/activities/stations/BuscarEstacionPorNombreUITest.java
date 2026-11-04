
package es.unican.bicis.activities.stations;


import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static es.unican.bicis.utils.StationsUITestUtils.TUEBICI;
import static es.unican.bicis.utils.StationsUITestUtils.openStations;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.Manifest;
import android.widget.ListAdapter;
import android.widget.ListView;

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
import es.unican.bicis.model.Station;



/**
 * UI tests for HU 532314 - Buscar estación por nombre.
 */
@HiltAndroidTest
@UninstallModules(LocationModule.class)
public class BuscarEstacionPorNombreUITest {

    @Rule(order = 0)
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule(order = 1)
    public GrantPermissionRule permissionRule =
            GrantPermissionRule.grant(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            );

    /** La app no recibe ubicación para evitar la navegación automática. */
    @BindValue
    ILocationProvider ubicacionFalsa = cb -> { };

    @Rule(order = 2)
    public ActivityScenarioRule<MainView> activityRule =
            new ActivityScenarioRule<>(MainView.class);

    /**
     * UI.1a - HU 532314.
     * Comprueba que buscar Magdalena muestra solo esa estación
     * y permite consultar su ubicación.
     */
    @Test
    public void testUI1a_BuscarMagdalenaMuestraUbicacion() {

        // Abrir la pantalla de estaciones de TUeBICI.
        openStations(TUEBICI);

        onView(withId(R.id.lvStations))
                .check(matches(isDisplayed()));

        // Abrir el buscador.
        onView(withContentDescription(R.string.menuSearch))
                .perform(click());

        // Escribir el nombre de la estación.
        onView(withId(androidx.appcompat.R.id.search_src_text))
                .perform(typeText("Magdalena"));

        // Comprobar que solo queda Magdalena en la lista.
        onView(withId(R.id.lvStations))
                .check((view, exception) -> {
                    if (exception != null) {
                        throw exception;
                    }

                    ListView listView = (ListView) view;
                    ListAdapter adapter = listView.getAdapter();

                    assertNotNull(adapter);
                    assertEquals(1, adapter.getCount());

                    Station station = (Station) adapter.getItem(0);
                    assertEquals("Magdalena", station.getName());
                });

        // Seleccionar Magdalena desde el desplegable del buscador.
        onData(anything())
                .inRoot(isPlatformPopup())
                .atPosition(0)
                .perform(click());

        // Comprobar que se muestra la ubicación de Magdalena.
        onView(allOf(
                withText(containsString("43.4686")),
                withText(containsString("-3.7759"))
        )).check(matches(isDisplayed()));
    }


    /**
     * UI.2a - HU 532314.
     * Comprueba que las sugerencias se actualizan mientras se escribe
     * y permite seleccionar la estación encontrada.
     */
    @Test
    public void testUI2a_BusquedaActualizaResultadosMientrasSeEscribe() {

        openStations(TUEBICI);

        // Abrir el buscador.
        onView(withContentDescription(R.string.menuSearch))
                .perform(click());

        // Escribir las primeras letras.
        onView(withId(androidx.appcompat.R.id.search_src_text))
                .perform(typeText("Pla"));

        // Comprobar las tres estaciones encontradas y su orden.
        onView(withId(R.id.lvStations))
                .check((view, exception) -> {
                    if (exception != null) {
                        throw exception;
                    }

                    ListAdapter adapter = ((ListView) view).getAdapter();

                    assertNotNull(adapter);
                    assertEquals(3, adapter.getCount());

                    assertEquals("Plaza de Italia",
                            ((Station) adapter.getItem(0)).getName());
                    assertEquals("Plaza Manuel Llano",
                            ((Station) adapter.getItem(1)).getName());
                    assertEquals("Plaza Pombo (NE)",
                            ((Station) adapter.getItem(2)).getName());
                });

        // Completar la búsqueda.
        onView(withId(androidx.appcompat.R.id.search_src_text))
                .perform(replaceText("Plaza P"));

        // Comprobar que solo queda Plaza Pombo (NE).
        onView(withId(R.id.lvStations))
                .check((view, exception) -> {
                    if (exception != null) {
                        throw exception;
                    }

                    ListAdapter adapter = ((ListView) view).getAdapter();

                    assertNotNull(adapter);
                    assertEquals(1, adapter.getCount());
                    assertEquals("Plaza Pombo (NE)",
                            ((Station) adapter.getItem(0)).getName());
                });

        // Seleccionar la sugerencia del desplegable.
        onData(anything())
                .inRoot(isPlatformPopup())
                .atPosition(0)
                .perform(click());

        // Comprobar las coordenadas de Plaza Pombo (NE).
        onView(allOf(
                withText(containsString("43.4627")),
                withText(containsString("-3.8013"))
        )).check(matches(isDisplayed()));
    }


    /**
     * UI.2b - HU 532314.
     * Comprueba que la busqueda no distingue entre mayusculas y minusculas.
     */
    @Test
    public void testUI2b_BusquedaIgnoraMayusculas() {

        openStations(TUEBICI);

        // Abrir el buscador.
        onView(withContentDescription(R.string.menuSearch))
                .perform(click());

        // Buscar usando solo minusculas.
        onView(withId(androidx.appcompat.R.id.search_src_text))
                .perform(typeText("universidad"));

        // Comprobar las dos estaciones encontradas y su orden.
        onView(withId(R.id.lvStations))
                .check((view, exception) -> {
                    if (exception != null) {
                        throw exception;
                    }

                    ListAdapter adapter = ((ListView) view).getAdapter();

                    assertNotNull(adapter);
                    assertEquals(2, adapter.getCount());

                    assertEquals("Universidad de Cantabria",
                            ((Station) adapter.getItem(0)).getName());
                    assertEquals("Universidad Rectorado (NE)",
                            ((Station) adapter.getItem(1)).getName());
                });
    }

    /**
     * UI.2c - HU 532314.
     * Comprueba que la busqueda encuentra estaciones aunque no se escriban tildes.
     */
    @Test
    public void testUI2c_BusquedaIgnoraTildes() {

        openStations(TUEBICI);

        // Abrir el buscador.
        onView(withContentDescription(R.string.menuSearch))
                .perform(click());

        // Buscar Perines sin tilde.
        onView(withId(androidx.appcompat.R.id.search_src_text))
                .perform(typeText("perines"));

        // Comprobar que solo aparece Perines con tilde.
        onView(withId(R.id.lvStations))
                .check((view, exception) -> {
                    if (exception != null) {
                        throw exception;
                    }

                    ListAdapter adapter = ((ListView) view).getAdapter();

                    assertNotNull(adapter);
                    assertEquals(1, adapter.getCount());

                    assertEquals("Perinés",
                            ((Station) adapter.getItem(0)).getName());
                });
    }


    /**
     * UI.3a - HU 532314.
     * Comprueba que al borrar la busqueda vuelven a mostrarse
     * todas las estaciones de la red en su orden original.
     */
    @Test
    public void testUI3a_BorrarBusquedaRestauraEstaciones() {

        openStations(TUEBICI);

        // Abrir el buscador.
        onView(withContentDescription(R.string.menuSearch))
                .perform(click());

        // Escribir un texto que coincide con varias estaciones.
        onView(withId(androidx.appcompat.R.id.search_src_text))
                .perform(typeText("Pla"));

        // Comprobar que quedan tres estaciones.
        onView(withId(R.id.lvStations))
                .check((view, exception) -> {
                    if (exception != null) {
                        throw exception;
                    }

                    ListAdapter adapter = ((ListView) view).getAdapter();

                    assertNotNull(adapter);
                    assertEquals(3, adapter.getCount());
                });

        // Borrar todo el texto del buscador.
        onView(withId(androidx.appcompat.R.id.search_src_text))
                .perform(replaceText(""));

        // Comprobar que vuelven las 42 estaciones.
        onView(withId(R.id.lvStations))
                .check((view, exception) -> {
                    if (exception != null) {
                        throw exception;
                    }

                    ListAdapter adapter = ((ListView) view).getAdapter();

                    assertNotNull(adapter);
                    assertEquals(42, adapter.getCount());

                    // La primera estacion vuelve a ser Nueva Montaña.
                    Station firstStation = (Station) adapter.getItem(0);
                    assertEquals("Nueva Montaña", firstStation.getName());
                });

        // Comprobar la cabecera original.
        onView(withId(R.id.tvNetworkLocation))
                .check(matches(withText(
                        "Santander, ES \u00b7 42 estaciones")));
    }



    /**
     * UI.4a - HU 532314.
     * Comprueba que al buscar una estación con el nombre mal escrito
     * se sugiere la estación más parecida y se puede consultar su ubicación.
     */
    @Test
    public void testUI4a_BusquedaSugiereEstacionParecida() {

        openStations(TUEBICI);

        // Abrir el buscador.
        onView(withContentDescription(R.string.menuSearch))
                .perform(click());

        // Escribir el nombre de Magdalena con una letra de menos.
        onView(withId(androidx.appcompat.R.id.search_src_text))
                .perform(typeText("Magdalna"));

        // Comprobar que no se muestra la lista de estaciones.
        onView(withId(R.id.lvStations))
                .check(matches(withEffectiveVisibility(Visibility.GONE)));

        // Comprobar que aparece el mensaje de estación inexistente.
        onView(withText(R.string.searchNotFound))
                .check(matches(isDisplayed()));

        // Comprobar que aparece el título de la sugerencia.
        onView(withText(R.string.searchSuggestionTitle))
                .check(matches(isDisplayed()));

        // Comprobar que se sugiere Magdalena.
        onView(withId(R.id.tvSuggestion))
                .check(matches(allOf(
                        isDisplayed(),
                        withText("Magdalena"))));

        // Pulsar la sugerencia.
        onView(withId(R.id.tvSuggestion))
                .perform(click());

        // Comprobar la ubicación de Magdalena.
        onView(allOf(
                withText(containsString("43.4686")),
                withText(containsString("-3.7759"))
        )).check(matches(isDisplayed()));
    }


    /**
     * UI.5a - HU 532314.
     * Comprueba que una busqueda sin coincidencias ni nombres parecidos
     * muestra el mensaje correspondiente y permite realizar otra busqueda.
     */
    @Test
    public void testUI5a_BusquedaSinCoincidenciasNiSugerencias() {

        openStations(TUEBICI);

        // Abrir el buscador.
        onView(withContentDescription(R.string.menuSearch))
                .perform(click());

        // Escribir un nombre que no existe ni se parece a ninguna estacion.
        onView(withId(androidx.appcompat.R.id.search_src_text))
                .perform(typeText("Zzzz"));

        // Comprobar que no se muestra la lista de estaciones.
        onView(withId(R.id.lvStations))
                .check(matches(withEffectiveVisibility(Visibility.GONE)));

        // Comprobar que aparece el mensaje de estacion inexistente.
        onView(withText(R.string.searchNotFound))
                .check(matches(isDisplayed()));

        // Comprobar que no aparece ninguna sugerencia similar.
        onView(withId(R.id.tvNoSuggestion))
                .check(matches(isDisplayed()));

        onView(withId(R.id.suggestionGroup))
                .check(matches(withEffectiveVisibility(Visibility.GONE)));

        // Borrar el texto y escribir el nombre de una estacion existente.
        onView(withId(androidx.appcompat.R.id.search_src_text))
                .perform(replaceText("Magdalena"));

        // Comprobar que desaparece el mensaje de estacion inexistente.
        onView(withId(R.id.searchNotFoundPanel))
                .check(matches(withEffectiveVisibility(Visibility.GONE)));

        // Comprobar que solo aparece Magdalena.
        onView(withId(R.id.lvStations))
                .check((view, exception) -> {
                    if (exception != null) {
                        throw exception;
                    }

                    ListAdapter adapter = ((ListView) view).getAdapter();

                    assertNotNull(adapter);
                    assertEquals(1, adapter.getCount());

                    assertEquals("Magdalena",
                            ((Station) adapter.getItem(0)).getName());
                });
    }
}
