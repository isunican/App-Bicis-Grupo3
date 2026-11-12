package es.unican.bicis.activities.main;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import es.unican.bicis.location.ILocationProvider;
import es.unican.bicis.location.LocationResultCallback;
import es.unican.bicis.model.Location;
import es.unican.bicis.model.Network;
import es.unican.bicis.repository.INetworksRepository;
import es.unican.bicis.repository.NetworksCallback;

/**
 * Pruebas unitarias de MainPresenter con Mockito.
 * UMP.1: HU 532319 Detectar la ciudad segun mi ubicacion.
 * UMP.2a a UMP.4e: HU 532301 Seleccionar la ciudad (Tablas 2, 3 y 4 del plan de pruebas).
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
     * Hace que el proveedor de ubicacion falso devuelva las coordenadas indicadas.
     * @param lat latitud
     * @param lon longitud
     */
    private void ubicacionEn(double lat, double lon) {
        doAnswer(invocation -> {
            LocationResultCallback cb = invocation.getArgument(0);
            cb.onSuccess(lat, lon);
            return null;
        }).when(mockLocation).requestCurrentLocation(any());
    }

    /**
     * Comprueba que no se ha mostrado ningun mensaje de error de ubicacion.
     */
    private void comprobarQueNoHayErroresDeUbicacion() {
        verify(mockView, never()).showLocationPermissionDenied();
        verify(mockView, never()).showLocationUnavailable();
        verify(mockView, never()).showNoNetworkNearby();
    }

    /**
     * UMP.1a: permiso concedido y el usuario esta en Santander, se muestran las estaciones de TUeBICI.
     */
    @Test
    public void testUbicacionEnSantander() {
        repositorioDevuelveRedes();
        ubicacionEn(43.4623, -3.8099);

        sut.init(mockView);
        sut.onLocationPermissionResult(true);

        verify(mockView).requestLocationPermission();
        verify(mockView).showNetworkStations(tuebici);
        comprobarQueNoHayErroresDeUbicacion();
    }

    /**
     * UMP.1b: el usuario no da el permiso, se muestra su mensaje y no se pide la ubicacion.
     */
    @Test
    public void testPermisoDenegado() {
        repositorioDevuelveRedes();

        sut.init(mockView);
        sut.onLocationPermissionResult(false);

        verify(mockView).showLocationPermissionDenied();
        verify(mockLocation, never()).requestCurrentLocation(any());
        verify(mockView, never()).showNetworkStations(any());
    }

    /**
     * UMP.1c: hay permiso pero no se puede obtener la ubicacion, se muestra su mensaje.
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

        verify(mockView).showLocationUnavailable();
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

    /**
     * UMP.1e: el usuario esta en Reinosa, no hay ninguna red a 20 km o menos y se muestra su mensaje.
     */
    @Test
    public void testNoHayRedCerca() {
        repositorioDevuelveRedes();
        ubicacionEn(43.0018, -4.1376);

        sut.init(mockView);
        sut.onLocationPermissionResult(true);

        verify(mockView).showNoNetworkNearby();
        verify(mockView, never()).showNetworkStations(any());
    }

    /**
     * UMP.1f: hay dos redes cerca del usuario, se muestran las estaciones de la mas cercana.
     */
    @Test
    public void testVariasRedesCerca() {
        Network otraSantander = crearRed("otra-santander", 43.4500, -3.8099);
        redes.add(otraSantander);
        repositorioDevuelveRedes();
        ubicacionEn(43.4520, -3.8099);

        sut.init(mockView);
        sut.onLocationPermissionResult(true);

        verify(mockView).showNetworkStations(otraSantander);
        verify(mockView, never()).showNetworkStations(tuebici);
        comprobarQueNoHayErroresDeUbicacion();
    }

    // ==========================================================================================
    // HU 532301 Seleccionar la ciudad (UMP.2)
    // Redes de partida, en este orden: BiciMAD (Madrid, ES), Bici Metro (Madrid, ES),
    // TUeBICI (Santander, ES) y Bici Lisboa (Lisboa, PT).
    // ==========================================================================================

    private Network bicimad;
    private Network bicimetro;
    private Network tuebiciSantander;
    private Network bicilisboa;
    private List<Network> redesCiudades;

    /**
     * Crea una red con sus coordenadas, su ciudad y su pais.
     * @param id identificador de la red
     * @param lat latitud
     * @param lon longitud
     * @param city ciudad
     * @param country codigo ISO del pais
     * @return la red creada
     */
    private Network crearRed(String id, Double lat, Double lon, String city, String country) {
        Network red = crearRed(id, lat, lon);
        red.getLocation().setCity(city);
        red.getLocation().setCountry(country);
        return red;
    }

    /**
     * Crea las cuatro redes de las pruebas UMP.2 y hace que el repositorio falso las devuelva.
     */
    private void repositorioDevuelveRedesConCiudad() {
        bicimad = crearRed("bicimad", 40.4168, -3.7038, "Madrid", "ES");
        bicimetro = crearRed("bicimetro", 40.3700, -3.7038, "Madrid", "ES");
        tuebiciSantander = crearRed("tuebici", 43.4623, -3.8099, "Santander", "ES");
        bicilisboa = crearRed("bicilisboa", 38.7223, -9.1393, "Lisboa", "PT");
        redesCiudades = Arrays.asList(bicimad, bicimetro, tuebiciSantander, bicilisboa);

        doAnswer(invocation -> {
            NetworksCallback cb = invocation.getArgument(0);
            cb.onSuccess(redesCiudades);
            return null;
        }).when(mockRepository).requestNetworks(any());
    }

    /**
     * Hace que el proveedor de ubicacion falso falle, como si no hubiera ubicacion.
     */
    private void ubicacionFalla() {
        doAnswer(invocation -> {
            LocationResultCallback cb = invocation.getArgument(0);
            cb.onFailure(new Exception("sin ubicacion"));
            return null;
        }).when(mockLocation).requestCurrentLocation(any());
    }


    /**
     * Hace que el repositorio falso falle al cargar las redes.
     */
    private void repositorioFalla() {
        doAnswer(invocation -> {
            NetworksCallback cb = invocation.getArgument(0);
            cb.onFailure(new Exception("sin redes"));
            return null;
        }).when(mockRepository).requestNetworks(any());
    }


    /**
     * Crea una red de la que solo se conocen la ciudad y el pais (pueden ser null).
     * @param id identificador de la red
     * @param city ciudad
     * @param country codigo ISO del pais
     * @return la red creada
     */
    private Network crearRedSinCoordenadas(String id, String city, String country) {
        Location loc = new Location();
        loc.setCity(city);
        loc.setCountry(country);
        Network red = new Network();
        red.setId(id);
        red.setLocation(loc);
        return red;
    }

    // ---------------------------------------------------------------- onSelectCityClicked (Tabla 2)

    /**
     * UMP.2a: sin ciudad detectada ni seleccion previa, se muestran los paises sin seleccion y el
     * mensaje para elegir la ciudad. La lista no cambia.
     */
    @Test
    public void testOnSelectCityClickedSinCiudadDetectada() {
        repositorioDevuelveRedesConCiudad();
        ubicacionFalla();

        sut.init(mockView);
        sut.onLocationPermissionResult(true);
        sut.onSelectCityClicked();

        verify(mockView).showCitySelector(Arrays.asList("ES", "PT"), null, null);
        verify(mockView).showChooseCityHint();
        // solo se ha mostrado la lista completa al cargar las redes
        verify(mockView, times(1)).showNetworks(any());
    }

    /**
     * UMP.2b: con la ciudad detectada (Santander) salen ya seleccionados su pais y su ciudad, y la
     * lista muestra solo sus redes.
     */
    @Test
    public void testOnSelectCityClickedConCiudadDetectada() {
        repositorioDevuelveRedesConCiudad();
        ubicacionEn(43.4623, -3.8099);

        sut.init(mockView);
        sut.onLocationPermissionResult(true);
        sut.onSelectCityClicked();

        verify(mockView).showCitySelector(Arrays.asList("ES", "PT"), "ES", "Santander");
        verify(mockView).showCities(Arrays.asList("Madrid", "Santander"), "Santander");
        verify(mockView).showNetworks(Collections.singletonList(tuebiciSantander));
        verify(mockView, never()).showChooseCityHint();
    }

    /**
     * UMP.2c: aunque se haya detectado Santander, si el usuario ya habia elegido Espana y Madrid
     * se mantiene su eleccion.
     */
    @Test
    public void testOnSelectCityClickedMantieneLaSeleccion() {
        repositorioDevuelveRedesConCiudad();
        ubicacionEn(43.4623, -3.8099);

        sut.init(mockView);
        sut.onLocationPermissionResult(true);
        sut.onCountrySelected("ES");
        sut.onCitySelected("Madrid");
        sut.onSelectCityClicked();

        verify(mockView).showCitySelector(Arrays.asList("ES", "PT"), "ES", "Madrid");
        // una vez al elegir Madrid y otra al pulsar la chincheta
        verify(mockView, times(2)).showNetworks(Arrays.asList(bicimad, bicimetro));
        verify(mockView, never()).showChooseCityHint();
    }

    /**
     * UMP.2d: si la red detectada no tiene pais ni ciudad, se actua como si no se hubiera
     * detectado ninguna ciudad.
     */
    @Test
    public void testOnSelectCityClickedRedDetectadaSinCiudad() {
        repositorioDevuelveRedesConCiudad();
        // red cercana al usuario pero sin pais ni ciudad
        Network sinCiudad = crearRed("sinciudad", 42.0000, -1.0000);
        redesCiudades = Arrays.asList(bicimad, bicimetro, tuebiciSantander, bicilisboa, sinCiudad);
        ubicacionEn(42.0000, -1.0000);

        sut.init(mockView);
        sut.onLocationPermissionResult(true);
        sut.onSelectCityClicked();

        verify(mockView).showNetworkStations(sinCiudad);
        verify(mockView).showCitySelector(Arrays.asList("ES", "PT"), null, null);
        verify(mockView).showChooseCityHint();
    }

    /**
     * UMP.2e: si las redes no se han podido cargar se muestra el error de carga de ciudades y no
     * se muestran los desplegables.
     */
    @Test
    public void testOnSelectCityClickedSinRedesCargadas() {
        repositorioFalla();

        sut.init(mockView);
        sut.onSelectCityClicked();

        verify(mockView).showCitiesLoadError();
        verify(mockView, never()).showCitySelector(any(), any(), any());
        verify(mockView, never()).showCities(any(), any());
    }

    /**
     * UMP.2f: las redes sin ciudad o sin pais no anaden ningun pais al desplegable
     * (decision del equipo, Prueba 7).
     */
    @Test
    public void testOnSelectCityClickedRedesSinPaisOCiudad() {
        repositorioDevuelveRedesConCiudad();
        redesCiudades = Arrays.asList(bicimad, bicimetro, tuebiciSantander, bicilisboa,
                crearRedSinCoordenadas("bicisinciudad", null, "ES"),
                crearRedSinCoordenadas("bicisinpais", "Toledo", null));

        sut.init(mockView);
        sut.onSelectCityClicked();

        verify(mockView).showCitySelector(Arrays.asList("ES", "PT"), null, null);
    }

    // ---------------------------------------------------------------- onCountrySelected (Tabla 3)

    /**
     * UMP.3a: al elegir un pais se muestran sus ciudades sin seleccion y todas sus redes.
     */
    @Test
    public void testOnCountrySelectedPaisConVariasCiudades() {
        repositorioDevuelveRedesConCiudad();

        sut.init(mockView);
        sut.onCountrySelected("ES");

        verify(mockView).showCities(Arrays.asList("Madrid", "Santander"), null);
        verify(mockView).showNetworks(Arrays.asList(bicimad, bicimetro, tuebiciSantander));
    }

    /**
     * UMP.3b: al cambiar de pais se actualizan las ciudades, la ciudad anterior deja de estar
     * seleccionada y la lista muestra las redes del nuevo pais.
     */
    @Test
    public void testOnCountrySelectedCambioDePais() {
        repositorioDevuelveRedesConCiudad();

        sut.init(mockView);
        sut.onCountrySelected("ES");
        sut.onCitySelected("Madrid");
        sut.onCountrySelected("PT");

        verify(mockView).showCities(Collections.singletonList("Lisboa"), null);
        verify(mockView).showNetworks(Collections.singletonList(bicilisboa));
    }

    /**
     * UMP.3c: al elegir "Todos" (pais null) el desplegable de ciudades se vacia y la lista vuelve
     * a mostrar todas las redes.
     */
    @Test
    public void testOnCountrySelectedTodos() {
        repositorioDevuelveRedesConCiudad();

        sut.init(mockView);
        sut.onCountrySelected("ES");
        sut.onCitySelected("Madrid");
        sut.onCountrySelected(null);

        verify(mockView).showCities(Collections.emptyList(), null);
        // una vez al cargar las redes y otra al elegir "Todos"
        verify(mockView, times(2)).showNetworks(redesCiudades);
    }

    /**
     * UMP.3d: un pais sin redes no tiene ciudades y la lista queda vacia.
     */
    @Test
    public void testOnCountrySelectedPaisSinRedes() {
        repositorioDevuelveRedesConCiudad();

        sut.init(mockView);
        sut.onCountrySelected("FR");

        verify(mockView).showCities(Collections.emptyList(), null);
        verify(mockView).showNetworks(Collections.emptyList());
    }

    /**
     * UMP.3e: si las redes no se han podido cargar se muestra el error de carga de ciudades.
     */
    @Test
    public void testOnCountrySelectedSinRedesCargadas() {
        repositorioFalla();

        sut.init(mockView);
        sut.onCountrySelected("ES");

        verify(mockView).showCitiesLoadError();
        verify(mockView, never()).showCities(any(), any());
        verify(mockView, never()).showNetworks(any());
    }

    /**
     * UMP.3f: una red del pais sin ciudad aparece en la lista del pais, pero no en el desplegable
     * de ciudades (decision del equipo, Prueba 7).
     */
    @Test
    public void testOnCountrySelectedRedSinCiudad() {
        repositorioDevuelveRedesConCiudad();
        Network sinCiudad = crearRedSinCoordenadas("bicisinciudad", null, "ES");
        redesCiudades = Arrays.asList(bicimad, bicimetro, tuebiciSantander, bicilisboa, sinCiudad);

        sut.init(mockView);
        sut.onCountrySelected("ES");

        verify(mockView).showCities(Arrays.asList("Madrid", "Santander"), null);
        verify(mockView).showNetworks(Arrays.asList(bicimad, bicimetro, tuebiciSantander, sinCiudad));
    }

    // ---------------------------------------------------------------- onCitySelected (Tabla 4)

    /**
     * UMP.4a: al elegir una ciudad con dos redes se muestran solo esas dos.
     */
    @Test
    public void testOnCitySelectedCiudadConDosRedes() {
        repositorioDevuelveRedesConCiudad();

        sut.init(mockView);
        sut.onCountrySelected("ES");
        sut.onCitySelected("Madrid");

        verify(mockView).showNetworks(Arrays.asList(bicimad, bicimetro));
    }

    /**
     * UMP.4b: al elegir una ciudad con una red se muestra solo esa.
     */
    @Test
    public void testOnCitySelectedCiudadConUnaRed() {
        repositorioDevuelveRedesConCiudad();

        sut.init(mockView);
        sut.onCountrySelected("ES");
        sut.onCitySelected("Santander");

        verify(mockView).showNetworks(Collections.singletonList(tuebiciSantander));
    }

    /**
     * UMP.4c: al elegir "Todas" (ciudad null) se muestran todas las redes del pais.
     */
    @Test
    public void testOnCitySelectedTodas() {
        repositorioDevuelveRedesConCiudad();

        sut.init(mockView);
        sut.onCountrySelected("ES");
        sut.onCitySelected("Madrid");
        sut.onCitySelected(null);

        // una vez al elegir Espana y otra al elegir "Todas"
        verify(mockView, times(2)).showNetworks(Arrays.asList(bicimad, bicimetro, tuebiciSantander));
    }

    /**
     * UMP.4d: una ciudad de otro pais no tiene redes en el pais elegido.
     */
    @Test
    public void testOnCitySelectedCiudadDeOtroPais() {
        repositorioDevuelveRedesConCiudad();

        sut.init(mockView);
        sut.onCountrySelected("ES");
        sut.onCitySelected("Lisboa");

        verify(mockView).showNetworks(Collections.emptyList());
    }

    /**
     * UMP.4e: si las redes no se han podido cargar se muestra el error de carga de ciudades.
     */
    @Test
    public void testOnCitySelectedSinRedesCargadas() {
        repositorioFalla();

        sut.init(mockView);
        sut.onCitySelected("Madrid");

        verify(mockView).showCitiesLoadError();
        verify(mockView, never()).showNetworks(any());
    }
}