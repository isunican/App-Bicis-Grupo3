package es.unican.bicis.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class NetworkLocatorTest {

    private Network tuebici;
    private Network bicimad;
    private Network bicing;
    private List<Network> redes;

    @Before
    public void setUp() {
        // las mismas tres redes que tenemos en networks.json
        tuebici = crearRed("tuebici", 43.4623, -3.8099);
        bicimad = crearRed("bicimad", 40.4168, -3.7038);
        bicing = crearRed("bicing", 41.3850, 2.1734);

        redes = new ArrayList<>();
        redes.add(tuebici);
        redes.add(bicimad);
        redes.add(bicing);
    }

    // para no repetir lo mismo en cada test
    private Network crearRed(String id, Double lat, Double lon) {
        Location loc = new Location();
        loc.setLatitude(lat);
        loc.setLongitude(lon);

        Network red = new Network();
        red.setId(id);
        red.setLocation(loc);
        return red;
    }

    // UT1
    @Test
    public void testUsuarioEnSantander() {
        Network res = NetworkLocator.findNearestNetwork(redes, 43.4600, -3.8000, NetworkLocator.MAX_DISTANCE_KM);
        assertEquals(tuebici, res);
    }

    // UT2 - no hace falta estar justo en el centro, a 5km tambien vale
    @Test
    public void testUsuarioCercaDeMadrid() {
        Network res = NetworkLocator.findNearestNetwork(redes, 40.4618, -3.7038, NetworkLocator.MAX_DISTANCE_KM);
        assertEquals(bicimad, res);
    }

    // UT3 - Paris, no hay ninguna red nuestra cerca
    @Test
    public void testUsuarioLejos() {
        Network res = NetworkLocator.findNearestNetwork(redes, 48.8566, 2.3522, NetworkLocator.MAX_DISTANCE_KM);
        assertNull(res);
    }

    // UT4 - meto otra red en Madrid un poco mas al norte y me pongo al lado,
    // tiene que devolver esa y no bicimad aunque bicimad este antes en la lista
    @Test
    public void testDosRedesMismaCiudad() {
        Network otraMadrid = crearRed("otra-madrid", 40.4500, -3.7038);
        redes.add(otraMadrid);

        Network res = NetworkLocator.findNearestNetwork(redes, 40.4550, -3.7038, NetworkLocator.MAX_DISTANCE_KM);
        assertEquals(otraMadrid, res);
    }

    // UT5 - todas estas tienen algo mal y se las tiene que saltar
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

    // UT6
    @Test
    public void testListaVacia() {
        assertNull(NetworkLocator.findNearestNetwork(new ArrayList<>(), 43.4623, -3.8099, NetworkLocator.MAX_DISTANCE_KM));
    }

    // UT7 - si llega null no deberia dar NullPointerException
    @Test
    public void testListaNull() {
        assertNull(NetworkLocator.findNearestNetwork(null, 43.4623, -3.8099, NetworkLocator.MAX_DISTANCE_KM));
    }

    // UT8 - el limite. Calculo la distancia exacta desde un punto al sur de Santander
    // y la uso como maximo: con eso tiene que entrar, y con un poco menos ya no
    @Test
    public void testLimiteDistancia() {
        double d = NetworkLocator.distanceKm(43.3000, -3.8099, 43.4623, -3.8099);

        assertEquals(tuebici, NetworkLocator.findNearestNetwork(redes, 43.3000, -3.8099, d));
        assertNull(NetworkLocator.findNearestNetwork(redes, 43.3000, -3.8099, d - 0.01));
    }

    // UT9 - en linea recta salen unos 339km, dejo 1km de margen
    @Test
    public void testDistanciaSantanderMadrid() {
        double d = NetworkLocator.distanceKm(43.4623, -3.8099, 40.4168, -3.7038);
        assertEquals(338.8, d, 1.0);
    }

    // UT10
    @Test
    public void testDistanciaMismoPunto() {
        assertEquals(0.0, NetworkLocator.distanceKm(40.4168, -3.7038, 40.4168, -3.7038), 0.001);
    }
}