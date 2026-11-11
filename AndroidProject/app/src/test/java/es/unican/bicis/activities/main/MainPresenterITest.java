package es.unican.bicis.activities.main;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
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
import java.util.Collections;
import java.util.List;

import es.unican.bicis.location.ILocationProvider;
import es.unican.bicis.location.LocationResultCallback;
import es.unican.bicis.model.Network;
import es.unican.bicis.repository.JsonNetworksRepository;

/**
 * Pruebas de integracion de MainPresenter con el modelo (CitySelector, Network, Location) y el
 * repositorio JSON reales (res/raw/networks.json, Anexo 1 del plan de pruebas de US532301).
 * Solo la vista y el proveedor de ubicacion son simulados (Mockito).
 * IMS.1 del plan de pruebas de US532301 (Seleccionar la ciudad), Tabla 7.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 35)
public class MainPresenterITest {

    /** Paises de networks.json ordenados por su nombre en espanol: Alemania, Argentina, Espana, Portugal */
    private static final List<String> PAISES = Arrays.asList("DE", "AR", "ES", "PT");

    private IMainContract.View mockView;
    private ILocationProvider mockLocation;
    private MainPresenter sut;

    /**
     * Crea la vista simulada con el repositorio JSON real y un proveedor de ubicacion que falla.
     * Despues inicia el presenter, que carga las 13 redes de networks.json.
     */
    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        mockView = mock(IMainContract.View.class);
        mockLocation = mock(ILocationProvider.class);
        when(mockView.getNetworksRepository()).thenReturn(new JsonNetworksRepository(context));
        when(mockView.getLocationProvider()).thenReturn(mockLocation);
        ubicacionFalla();

        sut = new MainPresenter();
        sut.init(mockView);
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
     * Devuelve los identificadores de las redes de la ultima lista que el presenter ha mandado
     * mostrar a la vista, en el mismo orden.
     * @return los identificadores de las redes mostradas
     */
    @SuppressWarnings("unchecked")
    private List<String> idsUltimaListaMostrada() {
        ArgumentCaptor<List<Network>> captor = ArgumentCaptor.forClass(List.class);
        verify(mockView, atLeastOnce()).showNetworks(captor.capture());
        List<String> ids = new ArrayList<>();
        for (Network n : captor.getValue()) {
            ids.add(n.getId());
        }
        return ids;
    }

    /**
     * IMS.1a: sin ciudad detectada, al pulsar la chincheta se muestran los cuatro paises de
     * networks.json ordenados por su nombre en espanol, sin ninguno seleccionado.
     */
    @Test
    public void testPaisesDeLasRedes() {
        sut.onLocationPermissionResult(true);
        sut.onSelectCityClicked();

        verify(mockView).showCitySelector(PAISES, null, null);
    }

    /**
     * IMS.1b: al elegir Espana se muestran sus nueve ciudades con red, en orden alfabetico y sin
     * repetir Madrid.
     */
    @Test
    public void testCiudadesDeEspana() {
        sut.onCountrySelected("ES");

        verify(mockView).showCities(Arrays.asList("Barcelona", "Bilbao", "Burgos", "Cádiz", "Gijón",
                "León", "Madrid", "Oviedo", "Santander"), null);
    }

    /**
     * IMS.1c (caso de exito): al elegir Espana y Madrid la lista muestra BiciMAD y Bici Metro.
     */
    @Test
    public void testRedesDeMadrid() {
        sut.onCountrySelected("ES");
        sut.onCitySelected("Madrid");

        assertEquals(Arrays.asList("bicimad", "bicimetro"), idsUltimaListaMostrada());
    }

    /**
     * IMS.1d: al cambiar de Espana a Portugal se actualizan las ciudades y la lista muestra solo
     * Bici Lisboa.
     */
    @Test
    public void testCambiarDePais() {
        sut.onCountrySelected("ES");
        sut.onCountrySelected("PT");

        verify(mockView).showCities(Collections.singletonList("Lisboa"), null);
        assertEquals(Collections.singletonList("bicilisboa"), idsUltimaListaMostrada());
    }

    /**
     * IMS.1e: al elegir "Todos" despues de filtrar por Madrid, la lista vuelve a mostrar las
     * 13 redes de networks.json.
     */
    @Test
    public void testSeleccionarTodos() {
        sut.onCountrySelected("ES");
        sut.onCitySelected("Madrid");
        sut.onCountrySelected(null);

        assertEquals(13, idsUltimaListaMostrada().size());
    }

    /**
     * IMS.1f: con la ubicacion en Santander, al pulsar la chincheta salen ya seleccionados
     * Espana y Santander, y la lista muestra solo TUeBICI.
     */
    @Test
    public void testUbicacionPorDefecto() {
        ubicacionEn(43.4623, -3.8099);
        sut.onLocationPermissionResult(true);
        sut.onSelectCityClicked();

        verify(mockView).showCitySelector(PAISES, "ES", "Santander");
        assertEquals(Collections.singletonList("tuebici"), idsUltimaListaMostrada());
    }
}