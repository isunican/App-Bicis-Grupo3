package es.unican.bicis.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Pruebas unitarias del modelo de la HU 532314 Buscar una estación por su nombre.
 * Cada método de prueba lleva en su nombre el identificador del caso en el plan de pruebas:
 * Tabla 3 ({@link StationSearch#searchByName(List, String)}, UBE.1a a UBE.1j) y
 * Tabla 4 ({@link StationSearch#findMostSimilar(List, String)}, UBE.2a a UBE.2j).
 * Todos los casos usan la lista L del plan, construida en {@link #setUp()}.
 *
 * @author Ángel González
 */
public class StationSearchTest {

    private Station plazaItalia;
    private Station perines;
    private Station plazaPombo;
    private Station magdalena;
    private Station monte;
    private Station montana2;
    private Station sinNombre;

    /** Lista L del plan de pruebas, en este orden */
    private List<Station> listaL;

    /** Crea las estaciones de la lista L del plan de pruebas (Anexo 1), incluida una sin nombre */
    @Before
    public void setUp() {
        plazaItalia = createStation("Plaza de Italia");
        perines = createStation("Perinés");
        plazaPombo = createStation("Plaza Pombo (NE)");
        magdalena = createStation("Magdalena");
        monte = createStation("Monte");
        montana2 = createStation("Montaña 2");
        sinNombre = createStation(null);

        listaL = Arrays.asList(plazaItalia, perines, plazaPombo,
                magdalena, monte, montana2, sinNombre);
    }

    /**
     * Crea una estación con solo el nombre, que es el único dato que usa la búsqueda.
     * @param name el nombre de la estación (puede ser null)
     * @return la estación creada
     */
    private Station createStation(String name) {
        Station station = new Station();
        station.setName(name);
        return station;
    }

    // ---------------------------------------------------------------------------------
    // Tabla 3. searchByName(stations, text)
    // ---------------------------------------------------------------------------------

    /** UBE.1a: L, "Plaza". Varias coincidencias, en el orden de L; la estación sin nombre no aparece */
    @Test
    public void testUBE1a_variasCoincidencias() {
        List<Station> result = StationSearch.searchByName(listaL, "Plaza");

        assertEquals(Arrays.asList(plazaItalia, plazaPombo), result);
    }

    /** UBE.1b: L, "plaza". El texto en minúsculas encuentra los nombres con mayúscula */
    @Test
    public void testUBE1b_ignoraMayusculas() {
        List<Station> result = StationSearch.searchByName(listaL, "plaza");

        assertEquals(Arrays.asList(plazaItalia, plazaPombo), result);
    }

    /** UBE.1c: L, "perines". El texto sin tilde encuentra "Perinés" */
    @Test
    public void testUBE1c_ignoraTildes() {
        List<Station> result = StationSearch.searchByName(listaL, "perines");

        assertEquals(Collections.singletonList(perines), result);
    }

    /** UBE.1d: L, "Puerto". Sin coincidencias: lista vacía */
    @Test
    public void testUBE1d_sinCoincidencias() {
        List<Station> result = StationSearch.searchByName(listaL, "Puerto");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /** UBE.1e: L, "". Texto vacío: L completa, en el mismo orden y en una lista nueva */
    @Test
    public void testUBE1e_textoVacio() {
        List<Station> result = StationSearch.searchByName(listaL, "");

        assertEquals(listaL, result);
        assertNotSame(listaL, result);
    }

    /** UBE.1f: L, " ". Texto con solo espacios: L completa, en el mismo orden */
    @Test
    public void testUBE1f_textoSoloEspacios() {
        List<Station> result = StationSearch.searchByName(listaL, " ");

        assertEquals(listaL, result);
    }

    /** UBE.1g: L, null. Texto null: L completa, en el mismo orden */
    @Test
    public void testUBE1g_textoNull() {
        List<Station> result = StationSearch.searchByName(listaL, null);

        assertEquals(listaL, result);
    }

    /** UBE.1h: lista vacía, "Plaza". Lista vacía */
    @Test
    public void testUBE1h_listaVacia() {
        List<Station> result = StationSearch.searchByName(new ArrayList<>(), "Plaza");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /** UBE.1i: null, "Plaza". Lista vacía, sin excepción */
    @Test
    public void testUBE1i_listaNull() {
        List<Station> result = StationSearch.searchByName(null, "Plaza");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /** UBE.1j: [Plaza de Italia, null, Plaza Pombo (NE)], "Plaza". El elemento null se ignora, sin excepción */
    @Test
    public void testUBE1j_estacionNullEnLista() {
        List<Station> conNull = Arrays.asList(plazaItalia, null, plazaPombo);

        List<Station> result = StationSearch.searchByName(conNull, "Plaza");

        assertEquals(Arrays.asList(plazaItalia, plazaPombo), result);
    }

    // ---------------------------------------------------------------------------------
    // Tabla 4. findMostSimilar(stations, text)
    // ---------------------------------------------------------------------------------

    /** UBE.2a: L, "Magdalna". Una letra de menos: Magdalena */
    @Test
    public void testUBE2a_unaLetraDeMenos() {
        assertSame(magdalena, StationSearch.findMostSimilar(listaL, "Magdalna"));
    }

    /** UBE.2b: L, "Magdelana". Dos cambios (límite, AVL): Magdalena */
    @Test
    public void testUBE2b_dosCambiosLimite() {
        assertSame(magdalena, StationSearch.findMostSimilar(listaL, "Magdelana"));
    }

    /** UBE.2c: L, "Mgdlen". Tres cambios (justo por encima del límite, AVL): null */
    @Test
    public void testUBE2c_tresCambiosSinSugerencia() {
        assertNull(StationSearch.findMostSimilar(listaL, "Mgdlen"));
    }

    /** UBE.2d: L, "Monta 2". Montaña 2 a dos cambios y Monte a tres: Montaña 2, la de menos cambios */
    @Test
    public void testUBE2d_ganaLaDeMenosCambios() {
        assertSame(montana2, StationSearch.findMostSimilar(listaL, "Monta 2"));
    }

    /** UBE.2e: L, "PERINEZ". Mayúsculas y sin tilde, un cambio: Perinés */
    @Test
    public void testUBE2e_ignoraMayusculasYTildes() {
        assertSame(perines, StationSearch.findMostSimilar(listaL, "PERINEZ"));
    }

    /** UBE.2f: lista vacía, "Magdalna". null */
    @Test
    public void testUBE2f_listaVacia() {
        assertNull(StationSearch.findMostSimilar(new ArrayList<>(), "Magdalna"));
    }

    /** UBE.2g: L, null. null, sin excepción */
    @Test
    public void testUBE2g_textoNull() {
        assertNull(StationSearch.findMostSimilar(listaL, null));
    }

    /** UBE.2h: null, "Magdalna". null, sin excepción */
    @Test
    public void testUBE2h_listaNull() {
        assertNull(StationSearch.findMostSimilar(null, "Magdalna"));
    }

    /** UBE.2i: L, "  ". Texto con solo espacios: null */
    @Test
    public void testUBE2i_textoSoloEspacios() {
        assertNull(StationSearch.findMostSimilar(listaL, "  "));
    }

    /** UBE.2j: [null, Magdalena], "Magdalna". El elemento null se ignora, sin excepción: Magdalena */
    @Test
    public void testUBE2j_estacionNullEnLista() {
        List<Station> conNull = Arrays.asList(null, magdalena);

        assertSame(magdalena, StationSearch.findMostSimilar(conNull, "Magdalna"));
    }
}
