package es.unican.bicis.activities.stations;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.unican.bicis.common.StationUtils;
import es.unican.bicis.model.Location;
import es.unican.bicis.model.Network;
import es.unican.bicis.model.Station;
import es.unican.bicis.repository.INetworksRepository;
import es.unican.bicis.repository.StationsCallback;

/**
 * Unit tests for {@link StationsPresenter}.
 * The view and the repository are replaced by Mockito mocks.
 * Loading of the stations (UPR.2): HU 532377 Listar estaciones.
 * Filter by free bikes and access to the details (UPR.1, UPR.3, UPR.4): HU 532376 Filtrar estaciones.
 */
public class StationsPresenterTest {

    private IStationsContract.View mockView;
    private INetworksRepository mockRepository;
    private StationsPresenter sut;

    private Network network;
    private Station s0;
    private Station s3;
    private Station s5;
    private Station sNull;

    @Before
    public void setUp() {
        mockView = mock(IStationsContract.View.class);
        mockRepository = mock(INetworksRepository.class);
        when(mockView.getNetworksRepository()).thenReturn(mockRepository);
        sut = new StationsPresenter();

        network = createNetwork("tuebici", "TUeBICI", "Santander", "ES");
        s0 = createStation("Estacion 0", 0);
        s3 = createStation("Estacion 3", 3);
        s5 = createStation("Estacion 5", 5);
        sNull = createStation("Estacion sin dato", null);
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

    private Station createStation(String name, Integer freeBikes) {
        Station station = new Station();
        station.setName(name);
        station.setFreeBikes(freeBikes);
        return station;
    }

    /** The repository answers the request with the given stations */
    private void repositoryReturns(List<Station> stations) {
        doAnswer(invocation -> {
            StationsCallback cb = invocation.getArgument(0);
            cb.onSuccess(stations);
            return null;
        }).when(mockRepository).requestStations(any(StationsCallback.class), anyString());
    }

    /** The repository fails when requesting the stations */
    private void repositoryFails() {
        doAnswer(invocation -> {
            StationsCallback cb = invocation.getArgument(0);
            cb.onFailure(new Throwable("Sin conexión"));
            return null;
        }).when(mockRepository).requestStations(any(StationsCallback.class), anyString());
    }

    /**
     * Loads the given stations in the presenter and forgets the calls made during
     * the initial load, so that the filter tests only check what happens afterwards.
     */
    private void initPresenterWith(List<Station> stations) {
        repositoryReturns(stations);
        sut.init(mockView, network);
        clearInvocations(mockView);
    }

    // =====================================================================
    // Carga de estaciones (HU 532377 Listar estaciones)
    // =====================================================================

    /** UPR.2a: se muestran la cabecera y las estaciones ordenadas de más a menos bicis. */
    @Test
    @SuppressWarnings("unchecked")
    public void testInitShowsStationsSortedByFreeBikes() {
        Station a = createStation("Alisal", 3);
        Station b = createStation("Correos", 5);
        Station c = createStation("Castilla", null);
        repositoryReturns(Arrays.asList(a, b, c));

        sut.init(mockView, createNetwork("tuebici", "TUeBICI", "Santander", "España"));

        verify(mockRepository).requestStations(any(StationsCallback.class), eq("tuebici"));
        verify(mockView).showNetworkName("TUeBICI");
        verify(mockView).showNetworkLocation("Santander, España");
        verify(mockView).showNetworkLocation("Santander, España · 3 estaciones");

        ArgumentCaptor<List<Station>> captor = ArgumentCaptor.forClass(List.class);
        verify(mockView).showStations(captor.capture());
        assertEquals(Arrays.asList(b, a, c), captor.getValue());

        verify(mockView, never()).showNoStations();
        verify(mockView, never()).showLoadError();
    }

    /** UPR.2b: se piden las estaciones de cualquier red, no solo de TUeBICI. */
    @Test
    public void testInitAnyNetworkRequestsItsStations() {
        repositoryReturns(Arrays.asList(createStation("Estación", 1)));

        sut.init(mockView, createNetwork("bicing", "Bicing", "Barcelona", "ES"));

        verify(mockRepository).requestStations(any(StationsCallback.class), eq("bicing"));
        verify(mockView).showStations(anyList());
    }

    /** UPR.2c: red sin nombre ni país: se muestra "-" y solo la ciudad. */
    @Test
    public void testInitMissingNameAndCountry() {
        repositoryReturns(Arrays.asList(createStation("Estación", 1)));

        sut.init(mockView, createNetwork("id", " ", "Santander", null));

        verify(mockView).showNetworkName(StationUtils.NO_DATA);
        verify(mockView).showNetworkLocation("Santander");
    }

    /** UPR.2d: red sin ubicación: se muestra "-". */
    @Test
    public void testInitNullLocationShowsDash() {
        Network n = createNetwork("id", "Red", null, null);
        n.setLocation(null);
        repositoryReturns(new ArrayList<>());

        sut.init(mockView, n);

        verify(mockView).showNetworkLocation(StationUtils.NO_DATA);
    }

    /** UPR.2e: red sin estaciones (lista vacía): mensaje de lista vacía. */
    @Test
    public void testInitEmptyListShowsNoStations() {
        repositoryReturns(new ArrayList<>());

        sut.init(mockView, createNetwork("id", "Red", "Ciudad", "ES"));

        verify(mockView).showNoStations();
        verify(mockView, never()).showStations(anyList());
        verify(mockView, never()).showLoadError();
    }

    /** UPR.2f: el repositorio devuelve null: mensaje de lista vacía. */
    @Test
    public void testInitNullListShowsNoStations() {
        repositoryReturns(null);

        sut.init(mockView, createNetwork("id", "Red", "Ciudad", "ES"));

        verify(mockView).showNoStations();
        verify(mockView, never()).showStations(anyList());
    }

    /** UPR.2g: falla la carga de estaciones: mensaje de error y se mantiene la cabecera. */
    @Test
    public void testInitRepositoryFailureShowsLoadError() {
        repositoryFails();

        sut.init(mockView, createNetwork("id", "Red", "Ciudad", "ES"));

        verify(mockView).showNetworkName("Red");
        verify(mockView).showLoadError();
        verify(mockView, never()).showStations(anyList());
        verify(mockView, never()).showNoStations();
    }

    /** UPR.2h: no llega ninguna red: mensaje de error y no se piden estaciones. */
    @Test
    public void testInitNullNetworkShowsLoadError() {
        sut.init(mockView, null);

        verify(mockView).showLoadError();
        verify(mockView, never()).showNetworkName(anyString());
        verify(mockRepository, never()).requestStations(any(StationsCallback.class), anyString());
    }

    /** UPR.2i: red sin id: mensaje de error y no se piden estaciones. */
    @Test
    public void testInitBlankIdShowsLoadErrorWithoutRequest() {
        sut.init(mockView, createNetwork("  ", "Red", "Ciudad", "ES"));

        verify(mockView).showNetworkName("Red");
        verify(mockView).showLoadError();
        verify(mockRepository, never()).requestStations(any(StationsCallback.class), anyString());
    }

    // =====================================================================
    // Filtro por bicis libres (HU 532376 Filtrar estaciones)
    // =====================================================================

    /** UPR.1a: filtro "3": solo las estaciones con 3 o más bicis, en orden. */
    @Test
    public void testFilterShowsOnlyStationsWithAtLeastMinimum() {
        initPresenterWith(Arrays.asList(s0, s3, s5, sNull));

        sut.onFilterApplied("3");

        verify(mockView).showFilterActive(3);
        verify(mockView).showStations(Arrays.asList(s5, s3));
        verify(mockView).showNetworkLocation("Santander, ES · 2 estaciones");
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

    /** UPR.1e: filtro "6", mayor que todas: mensaje de sin resultados. */
    @Test
    public void testFilterWithoutResultsShowsMessage() {
        initPresenterWith(Arrays.asList(s0, s3, s5, sNull));

        sut.onFilterApplied("6");

        verify(mockView).showFilterActive(6);
        verify(mockView).showNoStationsWithMinFreeBikes(6);
        verify(mockView).showNetworkLocation("Santander, ES");
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

    /** UPR.3a: al quitar el filtro se vuelven a mostrar todas las estaciones. */
    @Test
    public void testClearFilterShowsAllStations() {
        initPresenterWith(Arrays.asList(s0, s3, s5, sNull));
        sut.onFilterApplied("3");
        clearInvocations(mockView);

        sut.onFilterCleared();

        verify(mockView).showFilterInactive();
        verify(mockView).showStations(Arrays.asList(s5, s3, s0, sNull));
        verify(mockView).showNetworkLocation("Santander, ES · 4 estaciones");
    }

    /** UPR.3b: quitar el filtro tras un error de carga: se mantiene el error. */
    @Test
    public void testClearFilterAfterLoadErrorShowsLoadError() {
        repositoryFails();
        sut.init(mockView, network);
        sut.onFilterApplied("1");
        clearInvocations(mockView);

        sut.onFilterCleared();

        verify(mockView).showFilterInactive();
        verify(mockView).showLoadError();
        verify(mockView, never()).showStations(anyList());
    }

    // =====================================================================
    // Acceso al detalle de la red
    // =====================================================================

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
