package es.unican.bicis.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Pruebas unitarias de NetworkLocator (HU 532319 Detectar la ciudad segun mi ubicacion).
 */
public class NetworkLocatorTest {

    private Network tuebici;
    private Network bicimad;
    private Network bicing;
    private List<Network> redes;

    /**
     * Crea las tres redes de networks.json antes de cada test.
     */
    @Before
    public void setUp() {
        tuebici = crearRed("tuebici", 43.4623, -3.8099);
        bicimad = crearRed("bicimad", 40.4168, -3.7038);
        bicing = crearRed("bicing", 41.3850, 2.1734);

        redes = new ArrayList<>();
        redes.add(tuebici);
        redes.add(bicimad);
        redes.add(bicing);
    }

    /**
     * Crea una red con las coordenadas indicadas (pueden ser null).
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
     * UNL.1a: el usuario esta en Santander, devuelve TUeBICI.
     */
    @Test
    public void testUsuarioEnSantander() {
        Network res = NetworkLocator.findNearestNetwork(redes, 43.4600, -3.8000, NetworkLocator.MAX_DISTANCE_KM);
        assertEquals(tuebici, res);
    }

    /**
     * UNL.1b: el usuario esta a unos 5 km del centro de Madrid, devuelve BiciMAD.
     */
    @Test
    public void testUsuarioCercaDeMadrid() {
        Network res = NetworkLocator.findNearestNetwork(redes, 40.4618, -3.7038, NetworkLocator.MAX_DISTANCE_KM);
        assertEquals(bicimad, res);
    }

    /**
     * UNL.1c: el usuario esta en Paris, no hay ninguna red cerca y devuelve null.
     */
    @Test
    public void testUsuarioLejos() {
        Network res = NetworkLocator.findNearestNetwork(redes, 48.8566, 2.3522, NetworkLocator.MAX_DISTANCE_KM);
        assertNull(res);
    }

    /**
     * UNL.1d: hay dos redes en Madrid, devuelve la mas cercana aunque no sea la primera de la lista.
     */
    @Test
    public void testDosRedesMismaCiudad() {
        Network otraMadrid = crearRed("otra-madrid", 40.4500, -3.7038);
        redes.add(otraMadrid);

        Network res = NetworkLocator.findNearestNetwork(redes, 40.4550, -3.7038, NetworkLocator.MAX_DISTANCE_KM);
        assertEquals(otraMadrid, res);
    }

    /**
     * UNL.1e: las redes con datos incorrectos se ignoran y devuelve la valida.
     */
    @Test
    public void testRedesConDatosMal() {
        List<Network> lista = new ArrayList<>();
        lista.add(new Network()); // no tiene location
        lista.add(crearRed("a", null, -3.8099));
        lista.add(crearRed("b", 43.4623, null));
        lista.add(crearRed("c", 120.0, -3.8099)); // latitud maxima es 90
        lista.add(crearRed("d", 43.4623, 200.0)); // y longitud 180
        lista.add(null);
        lista.add(tuebici);

        Network res = NetworkLocator.findNearestNetwork(lista, 43.4623, -3.8099, NetworkLocator.MAX_DISTANCE_KM);
        assertEquals(tuebici, res);
    }

    /**
     * UNL.1f: con la lista vacia devuelve null.
     */
    @Test
    public void testListaVacia() {
        assertNull(NetworkLocator.findNearestNetwork(new ArrayList<>(), 43.4623, -3.8099, NetworkLocator.MAX_DISTANCE_KM));
    }

    /**
     * UNL.1g: con la lista null devuelve null sin lanzar excepcion.
     */
    @Test
    public void testListaNull() {
        assertNull(NetworkLocator.findNearestNetwork(null, 43.4623, -3.8099, NetworkLocator.MAX_DISTANCE_KM));
    }

    /**
     * UNL.1h: valor limite, una red justo a la distancia maxima se acepta y un poco mas lejos no.
     */
    @Test
    public void testLimiteDistancia() {
        double d = NetworkLocator.distanceKm(43.3000, -3.8099, 43.4623, -3.8099);

        assertEquals(tuebici, NetworkLocator.findNearestNetwork(redes, 43.3000, -3.8099, d));
        assertNull(NetworkLocator.findNearestNetwork(redes, 43.3000, -3.8099, d - 0.01));
    }

    /**
     * UNL.2a: la distancia en linea recta entre Santander y Madrid es de unos 339 km.
     */
    @Test
    public void testDistanciaSantanderMadrid() {
        double d = NetworkLocator.distanceKm(43.4623, -3.8099, 40.4168, -3.7038);
        assertEquals(338.8, d, 1.0);
    }

    /**
     * UNL.2b: la distancia de un punto a si mismo es 0.
     */
    @Test
    public void testDistanciaMismoPunto() {
        assertEquals(0.0, NetworkLocator.distanceKm(40.4168, -3.7038, 40.4168, -3.7038), 0.001);
    }
}