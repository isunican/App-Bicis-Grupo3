package es.unican.bicis.model;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for {@link StationSearch#findMostSimilar(List, String)}.
 * Tabla 4 del plan de pruebas de la HU 532314 Buscar estación por nombre (UBE.2a a UBE.2g).
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
}

