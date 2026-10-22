package es.unican.bicis.activities.stations;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.unican.bicis.model.Location;
import es.unican.bicis.model.Network;
import es.unican.bicis.model.Station;
import es.unican.bicis.repository.INetworksRepository;
import es.unican.bicis.repository.NetworkDetailsCallback;
import es.unican.bicis.repository.NetworksCallback;
import es.unican.bicis.repository.StationsCallback;

/**
 * Unit tests for {@link StationsPresenter}.
 * The view is replaced by a Mockito mock and the repository by a fake one,
 * so no Android dependencies are needed.
 */
public class StationsPresenterTest {

    private IStationsContract.View mockView;
    private FakeRepository repository;
    private StationsPresenter sut;

    private Network network;
    private Station s0;
    private Station s3;
    private Station s5;
    private Station sNull;

    /**
     * Fake repository that returns the given stations (or fails) synchronously.
     */
    private static class FakeRepository implements INetworksRepository {
        List<Station> stations = new ArrayList<>();
        boolean fail = false;
        String requestedId;

        @Override
        public void requestNetworks(NetworksCallback cb) {
            // Not used in these tests
        }

        @Override
        public void requestNetwork(NetworkDetailsCallback cb, String networkId) {
            // Not used in these tests
        }

        @Override
        public void requestStations(StationsCallback cb, String networkId) {
            requestedId = networkId;
            if (fail) {
                cb.onFailure(new Throwable("fallo simulado"));
            } else {
                cb.onSuccess(stations);
            }
        }
    }

    @Before
    public void setUp() {
        mockView = mock(IStationsContract.View.class);
        repository = new FakeRepository();
        when(mockView.getNetworksRepository()).thenReturn(repository);
        sut = new StationsPresenter();

        network = createNetwork("tuebici", "TUeBICI", "Santander", "ES");
        s0 = createStation("Estacion 0", 0);
        s3 = createStation("Estacion 3", 3);
        s5 = createStation("Estacion 5", 5);
        sNull = createStation("Estacion sin dato", null);
    }

    private Station createStation(String name, Integer freeBikes) {
        Station station = new Station();
        station.setName(name);
        station.setFreeBikes(freeBikes);
        return station;
    }

    private Network createNetwork(String id, String name, String city, String country) {
        Location location = new Location();
        location.setCity(city);
        location.setCountry(country);
        Network n = new Network();
        n.setId(id);
        n.setName(name);
        n.setLocation(location);
        return n;
    }

    /**
     * Loads the given stations in the presenter and forgets the calls made during
     * the initial load, so that the tests only check what happens afterwards.
     */
    private void initPresenterWith(List<Station> stations) {
        repository.stations = stations;
        sut.init(mockView, network);
        clearInvocations(mockView);
    }

    // ---------- Carga inicial ----------

    /** UPR.2a: se muestran la cabecera y las estaciones ordenadas de más a menos bicis. */
    @Test
    public void testInitShowsHeaderAndStationsSortedByFreeBikes() {
        repository.stations = Arrays.asList(s0, s3, sNull, s5);

        sut.init(mockView, network);

        assertEquals("tuebici", repository.requestedId);
        verify(mockView).showNetworkInfo("TUeBICI", "Santander, ES");
        verify(mockView).showStations(Arrays.asList(s5, s3, s0, sNull));
        verify(mockView, never()).showLoadError();
    }

    /** UPR.2b: red sin estaciones: mensaje de lista vacía. */
    @Test
    public void testInitNetworkWithoutStationsShowsMessage() {
        repository.stations = new ArrayList<>();

        sut.init(mockView, network);

        verify(mockView).showNoStations();
        verify(mockView, never()).showStations(anyList());
    }

    /** UPR.2c: falla la carga de estaciones: mensaje de error. */
    @Test
    public void testInitLoadErrorShowsMessage() {
        repository.fail = true;

        sut.init(mockView, network);

        verify(mockView).showLoadError();
        verify(mockView, never()).showStations(anyList());
    }

    /** UPR.2d: no llega ninguna red: mensaje de error. */
    @Test
    public void testInitNullNetworkShowsLoadError() {
        sut.init(mockView, null);

        verify(mockView).showLoadError();
        verify(mockView, never()).showNetworkInfo(anyString(), anyString());
    }

    /** UPR.2e: red sin id: mensaje de error. */
    @Test
    public void testInitNetworkWithoutIdShowsLoadError() {
        network.setId("  ");

        sut.init(mockView, network);

        verify(mockView).showNetworkInfo("TUeBICI", "Santander, ES");
        verify(mockView).showLoadError();
    }

    /** UPR.2f: red sin nombre ni ubicación: se muestra "-". */
    @Test
    public void testInitNetworkWithoutNameNorLocationShowsDash() {
        network.setName(null);
        network.setLocation(null);
        repository.stations = Arrays.asList(s3);

        sut.init(mockView, network);

        verify(mockView).showNetworkInfo("-", "-");
    }

    // ---------- Filtro: éxito ----------

    /** UPR.1a: filtro "3": solo las estaciones con 3 o más bicis, en orden. */
    @Test
    public void testFilterShowsOnlyStationsWithAtLeastMinimum() {
        initPresenterWith(Arrays.asList(s0, s3, s5, sNull));

        sut.onFilterApplied("3");

        verify(mockView).showFilterActive(3);
        verify(mockView).showStations(Arrays.asList(s5, s3));
        verify(mockView, never()).showNoStationsWithMinFreeBikes(anyInt());
        verify(mockView, never()).showInvalidMinFreeBikes();
    }

    /** UPR.1b: filtro "5": se incluye la estación con exactamente 5 bicis (valor límite). */
    @Test
    public void testFilterIncludesStationsEqualToMinimum() {
        initPresenterWith(Arrays.asList(s0, s3, s5, sNull));

        sut.onFilterApplied("5");

        verify(mockView).showStations(Arrays.asList(s5));
    }

    /** UPR.1c: filtro "1", el menor valor válido (valor límite). */
    @Test
    public void testFilterMinimumValueOne() {
        initPresenterWith(Arrays.asList(s0, s3, s5, sNull));

        sut.onFilterApplied("1");

        verify(mockView).showStations(Arrays.asList(s5, s3));
    }

    /** UPR.1d: filtro " 5 " con espacios: se acepta. */
    @Test
    public void testFilterAcceptsValueWithSpaces() {
        initPresenterWith(Arrays.asList(s0, s3, s5, sNull));

        sut.onFilterApplied(" 5 ");

        verify(mockView).showStations(Arrays.asList(s5));
    }

    /** UPR.3a: al quitar el filtro se vuelven a mostrar todas las estaciones. */
    @Test
    public void testClearFilterShowsAllStations() {
        initPresenterWith(Arrays.asList(s0, s3, s5, sNull));
        sut.onFilterApplied("3");
        clearInvocations(mockView);

        sut.onFilterCleared();

        verify(mockView).showFilterInactive();
        verify(mockView).showStations(Arrays.asList(s5, s3, s0, sNull));
    }

    // ---------- Filtro: sin resultados ----------

    /** UPR.1e: filtro "6", mayor que todas: mensaje de sin resultados. */
    @Test
    public void testFilterWithoutResultsShowsMessage() {
        initPresenterWith(Arrays.asList(s0, s3, s5, sNull));

        sut.onFilterApplied("6");

        verify(mockView).showFilterActive(6);
        verify(mockView).showNoStationsWithMinFreeBikes(6);
        verify(mockView, never()).showStations(anyList());
        verify(mockView, never()).showInvalidMinFreeBikes();
    }

    /** UPR.1f: red sin estaciones y filtro "1": mensaje de sin resultados. */
    @Test
    public void testFilterWithoutStationsShowsMessage() {
        initPresenterWith(new ArrayList<>());

        sut.onFilterApplied("1");

        verify(mockView).showNoStationsWithMinFreeBikes(1);
    }

    // ---------- Filtro: valor no válido ----------

    /** UPR.1g: filtro "0": valor no válido (valor límite). */
    @Test
    public void testFilterZeroIsInvalid() {
        assertInvalid("0");
    }

    /** UPR.1h: filtro "-1": valor no válido. */
    @Test
    public void testFilterNegativeIsInvalid() {
        assertInvalid("-1");
    }

    /** UPR.1i: filtro "abc": valor no válido. */
    @Test
    public void testFilterTextIsInvalid() {
        assertInvalid("abc");
    }

    /** UPR.1j: filtro "2.5": valor no válido. */
    @Test
    public void testFilterDecimalIsInvalid() {
        assertInvalid("2.5");
    }

    /** UPR.1k: filtro vacío: valor no válido. */
    @Test
    public void testFilterEmptyIsInvalid() {
        assertInvalid("");
    }

    /** UPR.1l: filtro null: valor no válido. */
    @Test
    public void testFilterNullIsInvalid() {
        assertInvalid(null);
    }

    /** UPR.1m: filtro "99999999999", fuera del rango de int: valor no válido. */
    @Test
    public void testFilterTooBigNumberIsInvalid() {
        assertInvalid("99999999999");
    }

    private void assertInvalid(String input) {
        initPresenterWith(Arrays.asList(s0, s3, s5, sNull));

        sut.onFilterApplied(input);

        verify(mockView).showInvalidMinFreeBikes();
        verify(mockView, never()).showFilterActive(anyInt());
        verify(mockView, never()).showStations(anyList());
        verify(mockView, never()).showNoStationsWithMinFreeBikes(anyInt());
    }

    // ---------- Filtro: estaciones sin dato de bicis ----------

    /** UPR.1n: las estaciones sin dato de bicis no se muestran al filtrar. */
    @Test
    public void testStationsWithoutDataAreNotShownWhenFiltering() {
        initPresenterWith(Arrays.asList(sNull, s3));

        sut.onFilterApplied("1");

        verify(mockView).showStations(Arrays.asList(s3));
    }

    /** UPR.1o: solo hay estaciones sin dato: mensaje de sin resultados. */
    @Test
    public void testOnlyStationsWithoutDataShowsNoResults() {
        initPresenterWith(Arrays.asList(sNull));

        sut.onFilterApplied("1");

        verify(mockView).showNoStationsWithMinFreeBikes(1);
    }

    /** UPR.3b: quitar el filtro tras un error de carga: se mantiene el error. */
    @Test
    public void testClearFilterAfterLoadErrorShowsLoadError() {
        repository.fail = true;
        sut.init(mockView, network);
        sut.onFilterApplied("1");
        clearInvocations(mockView);

        sut.onFilterCleared();

        verify(mockView).showFilterInactive();
        verify(mockView).showLoadError();
        verify(mockView, never()).showStations(anyList());
    }

    // ---------- Acceso al detalle de la red ----------

    /** UPR.4a: botón de información: se abre el detalle de la red. */
    @Test
    public void testInfoClickedOpensNetworkDetails() {
        initPresenterWith(Arrays.asList(s3));

        sut.onInfoClicked();

        verify(mockView).showNetworkDetails(network);
    }

    /** UPR.4b: botón de información sin red: no se abre nada. */
    @Test
    public void testInfoClickedWithoutNetworkDoesNothing() {
        sut.init(mockView, null);

        sut.onInfoClicked();

        verify(mockView, never()).showNetworkDetails(any());
    }
}
