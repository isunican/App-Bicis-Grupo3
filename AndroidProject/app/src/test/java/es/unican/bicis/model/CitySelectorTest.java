package es.unican.bicis.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Pruebas unitarias de CitySelector (HU 532301 Seleccionar la ciudad).
 * Casos UCS.1 - UCS.3 del plan de pruebas.
 */
public class CitySelectorTest {

    private Network biciMad;
    private Network biciMetro;
    private Network bicing;
    private Network biciAvila;
    private Network biciLisboa;
    private Network biciBerlin;
    private Network biciLimites;
    private Network sinLocation;
    private Network ciudadNull;
    private Network paisNull;
    private Network ciudadBlanco;

    /** Lista L del plan de pruebas */
    private List<Network> l;

    /**
     * Crea las redes de la lista L antes de cada test.
     */
    @Before
    public void setUp() {
        biciMad = crearRed("BiciMAD", "Madrid", "ES");
        biciMetro = crearRed("Bici Metro", "Madrid", "ES");
        bicing = crearRed("Bicing", "Barcelona", "ES");
        biciAvila = crearRed("Bici Ávila", "Ávila", "ES");
        biciLisboa = crearRed("Bici Lisboa", "Lisboa", "PT");
        biciBerlin = crearRed("Bici Berlín", "Berlín", "DE");
        biciLimites = crearRed("Bici Límites", "Ushuaia", "AR");
        sinLocation = new Network();
        sinLocation.setName("Sin location");
        ciudadNull = crearRed("Ciudad null", null, "ES");
        paisNull = crearRed("País null", "Toledo", null);
        ciudadBlanco = crearRed("Ciudad en blanco", " ", "ES");

        l = Arrays.asList(biciMad, biciMetro, bicing, biciAvila, biciLisboa, biciBerlin,
                biciLimites, sinLocation, ciudadNull, paisNull, ciudadBlanco);
    }

    // ---------------------------------------------------------------- getCountries

    @Test
    public void testGetCountries() {
        // UCS.1a: varios países con varias redes, ordenados por su nombre en español
        assertEquals(Arrays.asList("DE", "AR", "ES", "PT"), CitySelector.getCountries(l));

        // UCS.1b: dos redes del mismo país, sin repetidos
        assertEquals(List.of("ES"), CitySelector.getCountries(Arrays.asList(biciMad, biciMetro)));

        // UCS.1c: solo redes sin datos válidos
        assertTrue(CitySelector.getCountries(
                Arrays.asList(sinLocation, ciudadNull, paisNull, ciudadBlanco)).isEmpty());

        // UCS.1d: lista vacía
        assertTrue(CitySelector.getCountries(new ArrayList<>()).isEmpty());

        // UCS.1e: red null
        assertEquals(List.of("ES"), CitySelector.getCountries(Arrays.asList(biciMad, null)));
    }

    // ---------------------------------------------------------------- getCountryName

    @Test
    public void testGetCountryName() {
        assertEquals("España", CitySelector.getCountryName("ES"));
        assertEquals("Alemania", CitySelector.getCountryName("DE"));
        assertEquals("Portugal", CitySelector.getCountryName("pt"));
        assertNull(CitySelector.getCountryName(null));
    }

    // ---------------------------------------------------------------- getCities

    @Test
    public void testGetCities() {
        // UCS.2a: orden español, Madrid una vez, sin la ciudad null ni la vacía
        assertEquals(Arrays.asList("Ávila", "Barcelona", "Madrid"), CitySelector.getCities(l, "ES"));

        // UCS.2b: país con una ciudad
        assertEquals(List.of("Lisboa"), CitySelector.getCities(l, "PT"));

        // UCS.2c: país sin redes
        assertTrue(CitySelector.getCities(l, "FR").isEmpty());

        // UCS.2d: «Todos»
        assertTrue(CitySelector.getCities(l, null).isEmpty());

        // UCS.2e: lista vacía
        assertTrue(CitySelector.getCities(new ArrayList<>(), "ES").isEmpty());
    }

    // ---------------------------------------------------------------- filterByCity

    @Test
    public void testFilterByCity() {
        // UCS.3a: ciudad con dos redes, en el mismo orden
        assertEquals(Arrays.asList(biciMad, biciMetro), CitySelector.filterByCity(l, "ES", "Madrid"));

        // UCS.3b: ciudad con una red
        assertEquals(List.of(biciLisboa), CitySelector.filterByCity(l, "PT", "Lisboa"));

        // UCS.3c: solo país
        assertEquals(Arrays.asList(biciMad, biciMetro, bicing, biciAvila, ciudadNull, ciudadBlanco),
                CitySelector.filterByCity(l, "ES", null));

        // UCS.3d: ciudad de otro país
        assertTrue(CitySelector.filterByCity(l, "PT", "Madrid").isEmpty());

        // UCS.3e: «Todos», la lista completa en el mismo orden
        assertEquals(l, CitySelector.filterByCity(l, null, null));

        // UCS.3f: lista vacía
        assertTrue(CitySelector.filterByCity(new ArrayList<>(), "ES", "Madrid").isEmpty());
    }

    /**
     * Crea una red con la ciudad y el país indicados (pueden ser null).
     * @param nombre nombre de la red
     * @param ciudad ciudad de la red
     * @param pais código del país de la red
     * @return la red creada
     */
    private Network crearRed(String nombre, String ciudad, String pais) {
        Location location = new Location();
        location.setCity(ciudad);
        location.setCountry(pais);
        Network red = new Network();
        red.setName(nombre);
        red.setLocation(location);
        return red;
    }
}

