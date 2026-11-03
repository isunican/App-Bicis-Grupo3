package es.unican.bicis.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for {@link StationSearch}.
 * Plan de pruebas de la HU 532314 Buscar estación por nombre:
 * Tabla 3 ({@link StationSearch#searchByName(List, String)}, UBE.1a a UBE.1i) y
 * Tabla 4 ({@link StationSearch#findMostSimilar(List, String)}, UBE.2a a UBE.2g).
 * Los casos COV.x no están en el plan: completan la cobertura de condición/decisión.
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
    private List<Station> stations;

    @Before
    public void setUp() {
        plazaItalia = createStation("Plaza de Italia");
        perines = createStation("Perinés");
        plazaPombo = createStation("Plaza Pombo (NE)");
        magdalena = createStation("Magdalena");
        monte = createStation("Monte");
        montana2 = createStation("Montaña 2");
        sinNombre = createStation(null);

        stations = Arrays.asList(plazaItalia, perines, plazaPombo,
                magdalena, monte, montana2, sinNombre);
    }

    private Station createStation(String name) {
        Station station = new Station();
        station.setName(name);
        return station;
    }

    // ---------------------------------------------------------------------------------
    // Tabla 3. searchByName(stations, text)
    // ---------------------------------------------------------------------------------

    /** UBE.1a: varias coincidencias, en el orden de L; la estación sin nombre no aparece */
    @Test
    public void testSearchSeveralMatches() {
        List<Station> result = StationSearch.searchByName(stations, "Plaza");
        assertEquals(Arrays.asList(plazaItalia, plazaPombo), result);
    }

    /** UBE.1b: texto en minúsculas */
    @Test
    public void testSearchIgnoresCase() {
        List<Station> result = StationSearch.searchByName(stations, "plaza");
        assertEquals(Arrays.asList(plazaItalia, plazaPombo), result);
    }

    /** UBE.1c: texto sin tilde */
    @Test
    public void testSearchIgnoresAccents() {
        List<Station> result = StationSearch.searchByName(stations, "perines");
        assertEquals(Arrays.asList(perines), result);
    }

    /** UBE.1d: sin coincidencias */
    @Test
    public void testSearchNoMatches() {
        assertTrue(StationSearch.searchByName(stations, "Puerto").isEmpty());
    }

    /** UBE.1e: texto vacío, devuelve L completa en el mismo orden */
    @Test
    public void testSearchEmptyText() {
        assertEquals(stations, StationSearch.searchByName(stations, ""));
    }

    /** UBE.1f: texto con solo espacios, devuelve L completa en el mismo orden */
    @Test
    public void testSearchBlankText() {
        assertEquals(stations, StationSearch.searchByName(stations, " "));
    }

    /** UBE.1g: texto null, devuelve L completa en el mismo orden */
    @Test
    public void testSearchNullText() {
        assertEquals(stations, StationSearch.searchByName(stations, null));
    }

    /** UBE.1h: lista vacía */
    @Test
    public void testSearchEmptyList() {
        assertTrue(StationSearch.searchByName(new ArrayList<>(), "Plaza").isEmpty());
    }

    /** UBE.1i: lista null, devuelve lista vacía sin excepción */
    @Test
    public void testSearchNullList() {
        assertTrue(StationSearch.searchByName(null, "Plaza").isEmpty());
    }

    /** COV.1: un elemento null dentro de la lista se ignora sin excepción */
    @Test
    public void testSearchNullStationInList() {
        List<Station> withNull = Arrays.asList(plazaItalia, null, plazaPombo);
        assertEquals(Arrays.asList(plazaItalia, plazaPombo),
                StationSearch.searchByName(withNull, "Plaza"));
    }

    // ---------------------------------------------------------------------------------
    // Tabla 4. findMostSimilar(stations, text)
    // ---------------------------------------------------------------------------------

    /** UBE.2a: una letra de menos */
    @Test
    public void testOneLetterMissing() {
        assertSame(magdalena, StationSearch.findMostSimilar(stations, "Magdalna"));
    }

    /** UBE.2b: dos cambios (límite, AVL) */
    @Test
    public void testTwoChangesLimit() {
        assertSame(magdalena, StationSearch.findMostSimilar(stations, "Magdelana"));
    }

    /** UBE.2c: tres cambios (justo por encima del límite, AVL) */
    @Test
    public void testThreeChangesNoSuggestion() {
        assertNull(StationSearch.findMostSimilar(stations, "Mgdlen"));
    }

    /** UBE.2d: Montaña 2 a dos cambios y Monte a tres: se sugiere la de menos cambios */
    @Test
    public void testFewestChangesWins() {
        assertSame(montana2, StationSearch.findMostSimilar(stations, "Monta 2"));
    }

    /** UBE.2e: mayúsculas y sin tilde, un cambio */
    @Test
    public void testIgnoresCaseAndAccents() {
        assertSame(perines, StationSearch.findMostSimilar(stations, "PERINEZ"));
    }

    /** UBE.2f: lista vacía */
    @Test
    public void testEmptyList() {
        assertNull(StationSearch.findMostSimilar(new ArrayList<>(), "Magdalna"));
    }

    /** UBE.2g: texto null, sin excepción */
    @Test
    public void testNullText() {
        assertNull(StationSearch.findMostSimilar(stations, null));
    }

    /** COV.2: lista null, sin excepción */
    @Test
    public void testSimilarNullList() {
        assertNull(StationSearch.findMostSimilar(null, "Magdalna"));
    }

    /** COV.3: texto con solo espacios */
    @Test
    public void testSimilarBlankText() {
        assertNull(StationSearch.findMostSimilar(stations, "  "));
    }

    /** COV.4: un elemento null dentro de la lista se ignora sin excepción */
    @Test
    public void testSimilarNullStationInList() {
        List<Station> withNull = Arrays.asList(null, magdalena);
        assertSame(magdalena, StationSearch.findMostSimilar(withNull, "Magdalna"));
    }
}
