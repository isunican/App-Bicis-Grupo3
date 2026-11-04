package es.unican.bicis.activities.stations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.unican.bicis.model.Location;
import es.unican.bicis.model.Network;
import es.unican.bicis.model.Station;
import es.unican.bicis.repository.JsonNetworksRepository;

/**
 * Pruebas de integracion IBE.1 (Tabla 6) del plan de pruebas de US532314
 * Buscar estacion por nombre: StationsPresenter.onSearchTextChanged con el
 * modelo (StationSearch, Station) y el repositorio JSON reales.
 * Solo la vista es simulada.
 *
 * Antes de cada caso se llama a init con la red TUeBICI, que carga sus
 * 42 estaciones de res/raw/network_tuebici.json (Anexo 1 del plan).
 *
 * @author Angel Gonzalez
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 35)
public class StationsPresenterSearchITest {

    /** Numero de estaciones de la red TUeBICI en network_tuebici.json */
    private static final int TUEBICI_STATIONS = 42;

    private IStationsContract.View mockView;
    private StationsPresenter sut;

    /** Estaciones que muestra la vista tras init, en el orden en que se muestran */
    private List<Station> initialStations;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        mockView = mock(IStationsContract.View.class);
        when(mockView.getNetworksRepository()).thenReturn(new JsonNetworksRepository(context));
        sut = new StationsPresenter();

        sut.init(mockView, tuebici());

        // Comprobamos que la red se ha cargado entera y guardamos la lista inicial
        verify(mockView).showNetworkName("TUeBICI");
        verify(mockView).showNetworkLocation("Santander, ES · 42 estaciones");
        initialStations = shownStations();
        assertEquals(TUEBICI_STATIONS, initialStations.size());

        // Solo interesa lo que pide el presenter a partir de aqui
        clearInvocations(mockView);
    }

    /** La red TUeBICI tal y como llega desde la lista principal (sin estaciones) */
    private Network tuebici() {
        Location location = new Location();
        location.setCity("Santander");
        location.setCountry("ES");

        Network network = new Network();
        network.setId("tuebici");
        network.setName("TUeBICI");
        network.setLocation(location);
        return network;
    }

    /** Devuelve las estaciones que el presenter ha mandado mostrar (una sola vez) a la vista */
    @SuppressWarnings("unchecked")
    private List<Station> shownStations() {
        ArgumentCaptor<List<Station>> captor = ArgumentCaptor.forClass(List.class);
        verify(mockView).showStations(captor.capture());
        return captor.getValue();
    }

    /** Devuelve las estaciones que el presenter ha mandado mostrar como sugerencias del buscador */
    @SuppressWarnings("unchecked")
    private List<Station> shownSearchSuggestions() {
        ArgumentCaptor<List<Station>> captor = ArgumentCaptor.forClass(List.class);
        verify(mockView).showSearchSuggestions(captor.capture());
        return captor.getValue();
    }

    /** Nombres de las estaciones, en el orden en que se muestran */
    private List<String> names(List<Station> stations) {
        List<String> names = new ArrayList<>();
        for (Station s : stations) {
            names.add(s.getName());
        }
        return names;
    }

    /** Busca en la lista inicial la estacion con el nombre dado */
    private Station byName(String name) {
        for (Station s : initialStations) {
            if (name.equals(s.getName())) {
                return s;
            }
        }
        throw new AssertionError("No existe la estacion " + name);
    }

    /** Comprueba que no se ha mostrado ninguno de los mensajes de error de la lista */
    private void verifyNoListErrors() {
        verify(mockView, never()).showNoStations();
        verify(mockView, never()).showLoadError();
    }

    /**
     * IBE.1a: "Pla" (hay coincidencias). Se muestran las tres estaciones cuyo
     * nombre contiene el texto, en el mismo orden que en la lista, tambien como
     * sugerencias del buscador. No se muestra el mensaje de estacion inexistente.
     */
    @Test
    public void ibe1a_textWithMatches_showsMatchingStations() {
        sut.onSearchTextChanged("Pla");

        List<String> expected = Arrays.asList(
                "Plaza de Italia", "Plaza Manuel Llano", "Plaza Pombo (NE)");
        assertEquals(expected, names(shownStations()));
        assertEquals(expected, names(shownSearchSuggestions()));
        verify(mockView).showNetworkLocation("Santander, ES · 3 estaciones");
        verify(mockView, never()).showStationNotFound();
        verify(mockView, never()).showSuggestion(any());
        verifyNoListErrors();
    }

    /**
     * IBE.1b: "" despues de "Pla". Se muestran de nuevo las 42 estaciones,
     * en el mismo orden que tras init, y se ocultan las sugerencias del buscador.
     */
    @Test
    public void ibe1b_emptyTextAfterSearch_showsAllStationsInSameOrder() {
        sut.onSearchTextChanged("Pla");
        clearInvocations(mockView);

        sut.onSearchTextChanged("");

        assertEquals(initialStations, shownStations());
        assertTrue(shownSearchSuggestions().isEmpty());
        verify(mockView).showNetworkLocation("Santander, ES · 42 estaciones");
        verify(mockView, never()).showStationNotFound();
        verify(mockView, never()).showSuggestion(any());
        verifyNoListErrors();
    }

    /**
     * IBE.1c: "Magdalna" (sin coincidencias, con nombre parecido). Se muestra
     * el mensaje de estacion inexistente y se sugiere la estacion Magdalena
     * cargada del repositorio. No se muestra ninguna lista.
     */
    @Test
    public void ibe1c_misspelledName_showsNotFoundAndSuggestion() {
        sut.onSearchTextChanged("Magdalna");

        verify(mockView).showStationNotFound();
        ArgumentCaptor<Station> captor = ArgumentCaptor.forClass(Station.class);
        verify(mockView).showSuggestion(captor.capture());
        assertSame(byName("Magdalena"), captor.getValue());
        assertTrue(shownSearchSuggestions().isEmpty());
        verify(mockView).showNetworkLocation("Santander, ES · 0 estaciones");
        verify(mockView, never()).showStations(anyList());
        verifyNoListErrors();
    }

    /**
     * IBE.1d: "Zzzz" (sin coincidencias ni nombre parecido). Se muestra el
     * mensaje de estacion inexistente, sin sugerencia y sin lista.
     */
    @Test
    public void ibe1d_unknownName_showsNotFoundWithoutSuggestion() {
        sut.onSearchTextChanged("Zzzz");

        verify(mockView).showStationNotFound();
        verify(mockView, never()).showSuggestion(any());
        assertTrue(shownSearchSuggestions().isEmpty());
        verify(mockView).showNetworkLocation("Santander, ES · 0 estaciones");
        verify(mockView, never()).showStations(anyList());
        verifyNoListErrors();
    }
}
