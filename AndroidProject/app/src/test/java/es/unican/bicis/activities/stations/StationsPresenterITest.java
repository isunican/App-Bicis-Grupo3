package es.unican.bicis.activities.stations;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
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
 * Pruebas de integracion ILE.6 del plan de pruebas de US532377:
 * StationsPresenter con StationUtils, el modelo y el repositorio JSON reales.
 * Solo la vista es simulada.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 35)
public class StationsPresenterITest {

    private IStationsContract.View mockView;
    private StationsPresenter sut;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        mockView = mock(IStationsContract.View.class);
        when(mockView.getNetworksRepository()).thenReturn(new JsonNetworksRepository(context));
        sut = new StationsPresenter();
    }

    /** La red tal y como llega desde la lista principal (sin estaciones) */
    private Network network(String id, String name, String city) {
        Location location = new Location();
        location.setCity(city);
        location.setCountry("ES");

        Network network = new Network();
        network.setId(id);
        network.setName(name);
        network.setLocation(location);
        return network;
    }

    /** Devuelve las estaciones que el presenter ha mandado mostrar a la vista */
    @SuppressWarnings("unchecked")
    private List<Station> shownStations() {
        ArgumentCaptor<List<Station>> captor = ArgumentCaptor.forClass(List.class);
        verify(mockView).showStations(captor.capture());
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

    /**
     * ILE.6a: Bici Centro. Orden de mas a menos bicis disponibles y,
     * con las mismas bicis, por nombre (sin distinguir tildes).
     */
    @Test
    public void init_biciCentro_showsStationsSortedWithTieByName() {
        sut.init(mockView, network("bicicentro", "Bici Centro", "Torrelavega"));

        verify(mockView).showNetworkName("Bici Centro");
        verify(mockView).showNetworkLocation("Torrelavega, ES · 6 estaciones");
        assertEquals(Arrays.asList("Bulevar", "Plaza Mayor", "Árboles", "Ayuntamiento",
                "Estación Norte", "Universidad"), names(shownStations()));
        verify(mockView, never()).showNoStations();
        verify(mockView, never()).showLoadError();
    }

    /** ILE.6a (datos reales): Bicing, 545 estaciones y empate a 36 bicis resuelto por nombre. */
    @Test
    public void init_bicing_showsAllStationsSorted() {
        sut.init(mockView, network("bicing", "Bicing", "Barcelona"));

        List<String> names = names(shownStations());
        assertEquals(545, names.size());
        assertEquals("MOLL ORIENTAL", names.get(0));
        assertEquals("AV. LITORAL, 40 (D)", names.get(2));
        assertEquals("PL. PAU VILA (D)", names.get(3));
        verify(mockView).showNetworkLocation("Barcelona, ES · 545 estaciones");
    }

    /**
     * ILE.6b: Bici Parcial. Las estaciones sin dato de bicis disponibles van
     * al final, ordenadas por nombre.
     */
    @Test
    public void init_biciParcial_stationsWithoutFreeBikesAtTheEnd() {
        sut.init(mockView, network("biciparcial", "Bici Parcial", "Laredo"));

        assertEquals(Arrays.asList("Mercado", "Iglesia", null, "Playa", "Puerto"),
                names(shownStations()));
        verify(mockView).showNetworkLocation("Laredo, ES · 5 estaciones");
        verify(mockView, never()).showLoadError();
    }

    /** ILE.6b (datos de la rama): Bici Norte, las estaciones sin nombre van las ultimas de su grupo. */
    @Test
    public void init_biciNorte_stationsWithoutNameLastInTheirGroup() {
        sut.init(mockView, network("bicinorte", "Bici Norte", "Bilbao"));

        assertEquals(Arrays.asList("Moyua", null, "Guggenheim", "Abando", "  "),
                names(shownStations()));
    }

    /**
     * ILE.6 (Prueba 3): Bici Erronea. Los datos anomalos cuentan como ausentes:
     * se muestra la lista (no el error) y esas estaciones van al final.
     */
    @Test
    public void init_biciErronea_anomalousDataDoesNotPreventTheList() {
        sut.init(mockView, network("bicierronea", "Bici Errónea", "Reinosa"));

        assertEquals(Arrays.asList("Centro", "Parque", "Colegio", "Estación", "Hospital", "Ría"),
                names(shownStations()));
        verify(mockView).showNetworkLocation("Reinosa, ES · 6 estaciones");
        verify(mockView, never()).showLoadError();
    }

    /** ILE.6c: Bici Sur, red sin estaciones. */
    @Test
    public void init_biciSur_showsNoStations() {
        sut.init(mockView, network("bicisur", "Bici Sur", "Cádiz"));

        verify(mockView).showNetworkName("Bici Sur");
        verify(mockView).showNetworkLocation("Cádiz, ES");
        verify(mockView).showNoStations();
        verify(mockView, never()).showStations(anyList());
        verify(mockView, never()).showLoadError();
    }

    /** ILE.6e: Bici Caida, no se pueden cargar las estaciones. La cabecera si se muestra. */
    @Test
    public void init_biciCaida_showsLoadError() {
        sut.init(mockView, network("bicicaida", "Bici Caída", "Castro Urdiales"));

        verify(mockView).showNetworkName("Bici Caída");
        verify(mockView).showNetworkLocation("Castro Urdiales, ES");
        verify(mockView).showLoadError();
        verify(mockView, never()).showStations(anyList());
        verify(mockView, never()).showNoStations();
    }

    /** ILE.6f: no llega la red. Error y no se pide nada al repositorio. */
    @Test
    public void init_nullNetwork_showsLoadErrorWithoutRequest() {
        sut.init(mockView, null);

        verify(mockView).showLoadError();
        verify(mockView, never()).getNetworksRepository();
        verify(mockView, never()).showStations(anyList());
    }
}
