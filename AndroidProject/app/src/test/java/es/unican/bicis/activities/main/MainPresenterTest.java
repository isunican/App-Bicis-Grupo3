package es.unican.bicis.activities.main;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import es.unican.bicis.location.ILocationProvider;
import es.unican.bicis.location.LocationResultCallback;
import es.unican.bicis.model.Location;
import es.unican.bicis.model.Network;
import es.unican.bicis.repository.INetworksRepository;
import es.unican.bicis.repository.NetworksCallback;

/**
 * Pruebas unitarias de MainPresenter con Mockito (HU 532319 Detectar la ciudad segun mi ubicacion).
 */
public class MainPresenterTest {

    private IMainContract.View mockView;
    private INetworksRepository mockRepository;
    private ILocationProvider mockLocation;

    private MainPresenter sut;

    private Network tuebici;
    private List<Network> redes;

    /**
     * Crea los mocks y las redes antes de cada test.
     */
    @Before
    public void setUp() {
        mockView = mock(IMainContract.View.class);
        mockRepository = mock(INetworksRepository.class);
        mockLocation = mock(ILocationProvider.class);

        when(mockView.getNetworksRepository()).thenReturn(mockRepository);
        when(mockView.getLocationProvider()).thenReturn(mockLocation);

        tuebici = crearRed("tuebici", 43.4623, -3.8099);
        redes = new ArrayList<>();
        redes.add(tuebici);
        redes.add(crearRed("bicimad", 40.4168, -3.7038));

        sut = new MainPresenter();
    }

    /**
     * Crea una red con las coordenadas indicadas.
     * @param id identificador de la red
     * @param lat latitud
     * @param lon longitud
     * @return la red creada
     */
    private Network crearRed(String id, Double lat, Double lon) {
        Location loc = new Location();
        loc.setLatitude(lat);
        loc.setLongitude(lon);

        Network red = new Network();
        red.setId(id);
        red.setLocation(loc);
        return red;
    }

    /**
     * Hace que el repositorio falso devuelva la lista de redes.
     */
    private void repositorioDevuelveRedes() {
        doAnswer(invocation -> {
            NetworksCallback cb = invocation.getArgument(0);
            cb.onSuccess(redes);
            return null;
        }).when(mockRepository).requestNetworks(any());
    }

    /**
     * UMP.1a: permiso concedido y el usuario esta en Santander, se muestran las estaciones de TUeBICI.
     */
    @Test
    public void testUbicacionEnSantander() {
        repositorioDevuelveRedes();
        doAnswer(invocation -> {
            LocationResultCallback cb = invocation.getArgument(0);
            cb.onSuccess(43.4623, -3.8099);
            return null;
        }).when(mockLocation).requestCurrentLocation(any());

        sut.init(mockView);
        sut.onLocationPermissionResult(true);

        verify(mockView).requestLocationPermission();
        verify(mockView).showNetworkStations(tuebici);
        verify(mockView, never()).showLocationError();
    }

    /**
     * UMP.1b: el usuario no da el permiso, se muestra el error y no se pide la ubicacion.
     */
    @Test
    public void testPermisoDenegado() {
        repositorioDevuelveRedes();

        sut.init(mockView);
        sut.onLocationPermissionResult(false);

        verify(mockView).showLocationError();
        verify(mockLocation, never()).requestCurrentLocation(any());
        verify(mockView, never()).showNetworkStations(any());
    }

    /**
     * UMP.1c: hay permiso pero falla la ubicacion, se muestra el error.
     */
    @Test
    public void testFallaLaUbicacion() {
        repositorioDevuelveRedes();
        doAnswer(invocation -> {
            LocationResultCallback cb = invocation.getArgument(0);
            cb.onFailure(new Exception("sin ubicacion"));
            return null;
        }).when(mockLocation).requestCurrentLocation(any());

        sut.init(mockView);
        sut.onLocationPermissionResult(true);

        verify(mockView).showLocationError();
        verify(mockView, never()).showNetworkStations(any());
    }

    /**
     * UMP.1d: falla la carga de redes, se muestra el error de carga y no se pide el permiso.
     */
    @Test
    public void testFallaLaCargaDeRedes() {
        doAnswer(invocation -> {
            NetworksCallback cb = invocation.getArgument(0);
            cb.onFailure(new Exception("sin redes"));
            return null;
        }).when(mockRepository).requestNetworks(any());

        sut.init(mockView);

        verify(mockView).showLoadError();
        verify(mockView, never()).requestLocationPermission();
    }
}