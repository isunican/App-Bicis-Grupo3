package es.unican.bicis.activities.stations;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
 */
public class StationsPresenterTest {

    private IStationsContract.View mockView;
    private INetworksRepository mockRepository;
    private StationsPresenter sut;

    @Before
    public void setUp() {
        mockView = mock(IStationsContract.View.class);
        mockRepository = mock(INetworksRepository.class);
        when(mockView.getNetworksRepository()).thenReturn(mockRepository);
        sut = new StationsPresenter();
    }

    private Network createNetwork(String id, String name, String city, String country) {
        Location location = new Location();
        location.setCity(city);
        location.setCountry(country);

        Network network = new Network();
        network.setId(id);
        network.setName(name);
        network.setLocation(location);
        return network;
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

    // ---------- Caso de éxito ----------

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

    @Test
    public void testInitAnyNetworkRequestsItsStations() {
        repositoryReturns(Arrays.asList(createStation("Estación", 1)));

        sut.init(mockView, createNetwork("bicing", "Bicing", "Barcelona", "ES"));

        verify(mockRepository).requestStations(any(StationsCallback.class), eq("bicing"));
        verify(mockView).showStations(anyList());
    }

    // ---------- Datos ausentes en la cabecera ----------

    @Test
    public void testInitMissingNameAndCountry() {
        repositoryReturns(Arrays.asList(createStation("Estación", 1)));

        sut.init(mockView, createNetwork("id", " ", "Santander", null));

        verify(mockView).showNetworkName(StationUtils.NO_DATA);
        verify(mockView).showNetworkLocation("Santander");
    }

    @Test
    public void testInitNullLocationShowsDash() {
        Network network = createNetwork("id", "Red", null, null);
        network.setLocation(null);
        repositoryReturns(new ArrayList<>());

        sut.init(mockView, network);

        verify(mockView).showNetworkLocation(StationUtils.NO_DATA);
    }

    // ---------- Red sin estaciones ----------

    @Test
    public void testInitEmptyListShowsNoStations() {
        repositoryReturns(new ArrayList<>());

        sut.init(mockView, createNetwork("id", "Red", "Ciudad", "ES"));

        verify(mockView).showNoStations();
        verify(mockView, never()).showStations(anyList());
        verify(mockView, never()).showLoadError();
    }

    @Test
    public void testInitNullListShowsNoStations() {
        repositoryReturns(null);

        sut.init(mockView, createNetwork("id", "Red", "Ciudad", "ES"));

        verify(mockView).showNoStations();
        verify(mockView, never()).showStations(anyList());
    }

    // ---------- Error de carga ----------

    @Test
    public void testInitRepositoryFailureShowsLoadError() {
        repositoryFails();

        sut.init(mockView, createNetwork("id", "Red", "Ciudad", "ES"));

        verify(mockView).showNetworkName("Red");
        verify(mockView).showLoadError();
        verify(mockView, never()).showStations(anyList());
        verify(mockView, never()).showNoStations();
    }

    @Test
    public void testInitNullNetworkShowsLoadError() {
        sut.init(mockView, null);

        verify(mockView).showLoadError();
        verify(mockView, never()).showNetworkName(anyString());
        verify(mockRepository, never()).requestStations(any(StationsCallback.class), anyString());
    }

    @Test
    public void testInitBlankIdShowsLoadErrorWithoutRequest() {
        sut.init(mockView, createNetwork("  ", "Red", "Ciudad", "ES"));

        verify(mockView).showNetworkName("Red");
        verify(mockView).showLoadError();
        verify(mockRepository, never()).requestStations(any(StationsCallback.class), anyString());
    }

}
