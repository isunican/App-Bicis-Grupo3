package es.unican.bicis.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import es.unican.bicis.model.Station;

/**
 * Pruebas de integracion ILE.1 del plan de pruebas de US532377:
 * JsonNetworksRepository con Utils.parseNetwork y el modelo (Network, Station, StationExtra),
 * usando los ficheros network_<id>.json de res/raw.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 35)
public class JsonNetworksRepositoryITest {

    private JsonNetworksRepository sut;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        sut = new JsonNetworksRepository(context);
    }

    /**
     * Pide las estaciones de una red y las devuelve.
     * El repositorio responde en el momento, asi que el resultado ya esta
     * disponible cuando termina la llamada.
     */
    private List<Station> loadStations(String networkId) {
        final Object[] result = new Object[1];
        sut.requestStations(new StationsCallback() {
            @Override
            public void onSuccess(List<Station> stations) {
                result[0] = stations;
            }

            @Override
            public void onFailure(Throwable e) {
                fail("No se pudieron cargar las estaciones de " + networkId + ": " + e.getMessage());
            }
        }, networkId);
        assertNotNull("Estaciones no cargadas: " + networkId, result[0]);
        @SuppressWarnings("unchecked")
        List<Station> stations = (List<Station>) result[0];
        return stations;
    }

    /** Busca una estacion por su id */
    private Station byId(List<Station> stations, String id) {
        for (Station s : stations) {
            if (id.equals(s.getId())) {
                return s;
            }
        }
        fail("No existe la estacion " + id);
        return null;
    }

    /** ILE.1a: Bici Centro, todas las estaciones con sus datos correctos. */
    @Test
    public void requestStations_biciCentro_returnsAllStationsWithTheirData() {
        List<Station> stations = loadStations("bicicentro");

        assertEquals(6, stations.size());
        Station plazaMayor = byId(stations, "c1");
        assertEquals("Plaza Mayor", plazaMayor.getName());
        assertEquals(Integer.valueOf(8), plazaMayor.getFreeBikes());
        assertEquals(Integer.valueOf(4), plazaMayor.getEmptySlots());
        assertEquals(Integer.valueOf(2), plazaMayor.getEbikes());

        Station universidad = byId(stations, "c4");
        assertEquals(Integer.valueOf(0), universidad.getFreeBikes());
        assertEquals(Integer.valueOf(0), universidad.getEbikes());
    }

    /** ILE.1b: Bici Parcial, los datos ausentes llegan como null. */
    @Test
    public void requestStations_biciParcial_missingDataIsNull() {
        List<Station> stations = loadStations("biciparcial");

        assertEquals(5, stations.size());
        // Mercado: sin extra, no se sabe si tiene bicis electricas
        assertNull(byId(stations, "p1").getEbikes());
        // Puerto: sin free_bikes
        assertNull(byId(stations, "p2").getFreeBikes());
        assertEquals(Integer.valueOf(4), byId(stations, "p2").getEmptySlots());
        // Iglesia: sin empty_slots
        assertEquals(Integer.valueOf(5), byId(stations, "p3").getFreeBikes());
        assertNull(byId(stations, "p3").getEmptySlots());
        // Playa: sin bicis ni huecos
        assertNull(byId(stations, "p4").getFreeBikes());
        assertNull(byId(stations, "p4").getEmptySlots());
        // Estacion sin nombre
        assertNull(byId(stations, "p5").getName());
    }

    /**
     * ILE.1c: Bici Erronea. Los datos anomalos llegan como null y no hacen
     * fallar la carga del resto de estaciones (no se llama a onFailure).
     */
    @Test
    public void requestStations_biciErronea_anomalousDataIsNullAndDoesNotFail() {
        List<Station> stations = loadStations("bicierronea");

        assertEquals(6, stations.size());
        // Centro: todo correcto
        assertEquals(Integer.valueOf(4), byId(stations, "e1").getFreeBikes());
        assertEquals(Integer.valueOf(8), byId(stations, "e1").getEmptySlots());
        // Hospital: free_bikes -3
        assertNull(byId(stations, "e2").getFreeBikes());
        assertEquals(Integer.valueOf(10), byId(stations, "e2").getEmptySlots());
        // Estacion: free_bikes "cinco"
        assertNull(byId(stations, "e3").getFreeBikes());
        // Parque: empty_slots -1
        assertEquals(Integer.valueOf(2), byId(stations, "e4").getFreeBikes());
        assertNull(byId(stations, "e4").getEmptySlots());
        // Colegio: ebikes "muchas"
        assertNull(byId(stations, "e5").getEbikes());
        // Ria: free_bikes 7.5 y ebikes -2
        assertNull(byId(stations, "e6").getFreeBikes());
        assertNull(byId(stations, "e6").getEbikes());
    }

    /** ILE.1d: Bici Sur, red sin estaciones: lista vacia, no null ni error. */
    @Test
    public void requestStations_biciSur_returnsEmptyList() {
        List<Station> stations = loadStations("bicisur");

        assertTrue(stations.isEmpty());
    }

    /** ILE.1e: Bici Caida no tiene fichero de detalle: se llama a onFailure. */
    @Test
    public void requestStations_biciCaida_fails() {
        final Throwable[] error = new Throwable[1];
        sut.requestStations(new StationsCallback() {
            @Override
            public void onSuccess(List<Station> stations) {
                fail("No deberia cargar estaciones de una red sin fichero");
            }

            @Override
            public void onFailure(Throwable e) {
                error[0] = e;
            }
        }, "bicicaida");

        assertNotNull(error[0]);
    }
}