package es.unican.bicis.activities.stations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
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
 * Pruebas de integracion de StationsPresenter con el modelo y el repositorio JSON reales.
 * Solo la vista es simulada (Mockito).
 * <ul>
 *     <li>ILE.6 del plan de pruebas de US532377 (Listar estaciones): carga de estaciones con StationUtils.</li>
 *     <li>IFH.1 del plan de pruebas de US532335 (Filtrar estaciones por huecos): filtro por huecos con
 *     StationFilter, sobre la red TUeBICI de res/raw/network_tuebici.json (Anexo 1 del plan).</li>
 * </ul>
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

    // =====================================================================
    // US532335 - Filtrar las estaciones por huecos disponibles
    // Pruebas de integracion IFH.1 del plan de pruebas (Tabla 6).
    // Datos: red TUeBICI (42 estaciones), con "Sector 3 (NE)" sin empty_slots,
    // "San Román Corbán (NE)" con -1 y "Alisal (NE)" con "sin dato" (Anexo 1).
    // =====================================================================

    /**
     * Carga la red TUeBICI en el presenter con el repositorio real y olvida las llamadas
     * que init hace a la vista, para que cada prueba verifique solo lo que pide el
     * presenter al aplicar el filtro por huecos.
     */
    private void initTuebici() {
        sut.init(mockView, network("tuebici", "TUeBICI", "Santander"));
        clearInvocations(mockView);
    }

    /**
     * IFH.1a: mínimo 15 (caso de éxito): 8 estaciones ordenadas de más a menos huecos y,
     * a igualdad de huecos, primero la que tiene más bicis. La primera es "Renfe" (20 huecos).
     */
    @Test
    public void onMinEmptySlotsFilterApplied_15_showsEightStationsSortedByEmptySlots() {
        initTuebici();

        sut.onMinEmptySlotsFilterApplied("15");

        List<String> names = names(shownStations());
        assertEquals(8, names.size());
        assertEquals(Arrays.asList(
                        "Renfe",                      // 20 huecos
                        "Castilla Hermida",           // 19 huecos, 1 bici (empate: más bicis primero)
                        "Correos",                    // 19 huecos, 0 bicis
                        "Tabacalera/Varadero",        // 18 huecos
                        "Ayuntamiento (NE)",          // 16 huecos
                        "Intercambiador Sardinero"),  // 15 huecos, 5 bicis
                names.subList(0, 6));
        assertFalse(names.contains("PTCAN 1"));   // 12 huecos, no llega al mínimo
        verify(mockView, never()).showNoStationsWithMinEmptySlots(anyInt());
    }


    /** IFH.1b: mínimo 20 (estación en el límite): solo "Renfe", que tiene exactamente 20 huecos. */
    @Test
    public void onMinEmptySlotsFilterApplied_20_showsOnlyStationOnTheLimit() {
        initTuebici();

        sut.onMinEmptySlotsFilterApplied("20");

        assertEquals(Arrays.asList("Renfe"), names(shownStations()));
    }

    /** IFH.1c: mínimo 21 (ninguna estación cumple): mensaje sin resultados y no se muestra la lista. */
    @Test
    public void onMinEmptySlotsFilterApplied_21_showsNoStationsMessage() {
        initTuebici();

        sut.onMinEmptySlotsFilterApplied("21");

        verify(mockView).showNoStationsWithMinEmptySlots(21);
        verify(mockView, never()).showStations(anyList());
    }

    /** IFH.1d: mínimo 8 y después 3 (cambio del número de huecos): 24 estaciones y después 36. */
    @Test
    public void onMinEmptySlotsFilterApplied_8Then3_updatesTheList() {
        initTuebici();

        sut.onMinEmptySlotsFilterApplied("8");
        assertEquals(24, shownStations().size());

        clearInvocations(mockView);
        sut.onMinEmptySlotsFilterApplied("3");

        List<String> names = names(shownStations());
        assertEquals(36, names.size());
        assertTrue(names.contains("Nueva Montaña"));   // 5 huecos, antes quedaba fuera
        assertTrue(names.contains("Magdalena"));       // 4 huecos, antes quedaba fuera
    }

    /**
     * IFH.1e: mínimo 1 (datos ausentes o anómalos): 36 estaciones. No aparecen las estaciones
     * sin dato de huecos ("Sector 3 (NE)"), con un valor negativo ("San Román Corbán (NE)")
     * ni con un texto ("Alisal (NE)").
     */
    @Test
    public void onMinEmptySlotsFilterApplied_1_excludesStationsWithoutValidData() {
        initTuebici();

        sut.onMinEmptySlotsFilterApplied("1");

        List<String> names = names(shownStations());
        assertEquals(36, names.size());
        assertFalse(names.contains("Sector 3 (NE)"));
        assertFalse(names.contains("San Román Corbán (NE)"));
        assertFalse(names.contains("Alisal (NE)"));
    }

    /** IFH.1f: mínimo 0: se muestran las 42 estaciones, también las que no tienen dato de huecos. */
    @Test
    public void onMinEmptySlotsFilterApplied_0_showsAllStations() {
        initTuebici();

        sut.onMinEmptySlotsFilterApplied("0");

        List<String> names = names(shownStations());
        assertEquals(42, names.size());
        assertTrue(names.contains("Sector 3 (NE)"));
        assertTrue(names.contains("San Román Corbán (NE)"));
        assertTrue(names.contains("Alisal (NE)"));
    }
}