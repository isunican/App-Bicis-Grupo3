package es.unican.bicis.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for {@link StationFilter}.
 */
public class StationFilterTest {

    private Station s0;       // 0 free bikes
    private Station s3;       // 3 free bikes
    private Station s5;       // 5 free bikes
    private Station sNull;    // does not report free bikes
    private Station sNeg;     // invalid value (negative)
    private List<Station> stations;

    // HU 532335: estaciones para el filtro por huecos (lista L del plan de pruebas)
    private Station h0;       // 0 empty slots
    private Station h4;       // 4 empty slots
    private Station h7;       // 7 empty slots
    private Station hNull;    // does not report empty slots
    private Station hNeg;     // invalid value (negative)
    private List<Station> stationsSlots;

    @Before
    public void setUp() {
        s0 = createStation("Estacion 0", 0);
        s3 = createStation("Estacion 3", 3);
        s5 = createStation("Estacion 5", 5);
        sNull = createStation("Estacion sin dato", null);
        sNeg = createStation("Estacion negativa", -2);
        stations = Arrays.asList(s0, s3, s5, sNull, sNeg);

        h0 = createStationWithEmptySlots("Estacion 0 huecos", 0);
        h4 = createStationWithEmptySlots("Estacion 4 huecos", 4);
        h7 = createStationWithEmptySlots("Estacion 7 huecos", 7);
        hNull = createStationWithEmptySlots("Estacion sin dato de huecos", null);
        hNeg = createStationWithEmptySlots("Estacion huecos negativos", -3);
        stationsSlots = Arrays.asList(h0, h4, h7, hNull, hNeg);
    }

    private Station createStation(String name, Integer freeBikes) {
        Station station = new Station();
        station.setName(name);
        station.setFreeBikes(freeBikes);
        return station;
    }

    private Station createStationWithEmptySlots(String name, Integer emptySlots) {
        Station station = new Station();
        station.setName(name);
        station.setEmptySlots(emptySlots);
        return station;
    }

    // ---------- Éxito ----------

    /** UFE.1a: mínimo 3, se devuelven las estaciones con 3 o más bicis libres. */
    @Test
    public void testFilterReturnsStationsWithAtLeastMinimum() {
        List<Station> result = StationFilter.filterByMinFreeBikes(stations, 3);

        assertEquals(Arrays.asList(s3, s5), result);
    }

    /** UFE.1b: mínimo 5, la estación con exactamente 5 bicis se incluye (valor límite). */
    @Test
    public void testFilterIncludesStationsEqualToMinimum() {
        List<Station> result = StationFilter.filterByMinFreeBikes(stations, 5);

        assertEquals(Arrays.asList(s5), result);
    }

    /** UFE.1c: mínimo 0, se devuelven todas las estaciones con dato válido. */
    @Test
    public void testFilterWithMinimumZeroReturnsAllStationsWithValidData() {
        List<Station> result = StationFilter.filterByMinFreeBikes(stations, 0);

        assertEquals(Arrays.asList(s0, s3, s5), result);
    }

    /** UFE.1i: la lista original no se modifica al filtrar. */
    @Test
    public void testFilterDoesNotModifyOriginalList() {
        List<Station> original = new ArrayList<>(stations);

        StationFilter.filterByMinFreeBikes(stations, 3);

        assertEquals(original, stations);
    }

    // ---------- Sin resultados ----------

    /** UFE.1d: mínimo 6, mayor que todas las estaciones: lista vacía. */
    @Test
    public void testFilterWithMinimumAboveAllReturnsEmpty() {
        List<Station> result = StationFilter.filterByMinFreeBikes(stations, 6);

        assertTrue(result.isEmpty());
    }

    /** UFE.1e: lista de estaciones vacía: lista vacía. */
    @Test
    public void testFilterEmptyListReturnsEmpty() {
        List<Station> result = StationFilter.filterByMinFreeBikes(new ArrayList<>(), 1);

        assertTrue(result.isEmpty());
    }

    // ---------- Valores no válidos ----------

    /** UFE.1f: lista de estaciones null: lista vacía. */
    @Test
    public void testFilterNullListReturnsEmpty() {
        List<Station> result = StationFilter.filterByMinFreeBikes(null, 1);

        assertTrue(result.isEmpty());
    }

    /** UFE.1g: mínimo negativo: lista vacía. */
    @Test
    public void testFilterNegativeMinimumReturnsEmpty() {
        List<Station> result = StationFilter.filterByMinFreeBikes(stations, -1);

        assertTrue(result.isEmpty());
    }

    // ---------- Estaciones sin dato de bicis ----------

    /** UFE.1h: estaciones sin dato, con dato negativo o null: nunca pasan el filtro. */
    @Test
    public void testFilterExcludesStationsWithoutDataOrInvalidData() {
        List<Station> onlyBadStations = Arrays.asList(sNull, sNeg, null);

        List<Station> result = StationFilter.filterByMinFreeBikes(onlyBadStations, 0);

        assertTrue(result.isEmpty());
    }

    /** UFE.2a: hasValidFreeBikes con dato válido, null, negativo y estación null. */
    @Test
    public void testHasValidFreeBikes() {
        assertTrue(StationFilter.hasValidFreeBikes(s0));
        assertTrue(StationFilter.hasValidFreeBikes(s5));
        assertFalse(StationFilter.hasValidFreeBikes(sNull));
        assertFalse(StationFilter.hasValidFreeBikes(sNeg));
        assertFalse(StationFilter.hasValidFreeBikes(null));
    }

    // =====================================================================
    // HU 532335 - Filtrar las estaciones por huecos disponibles
    // filterByMinEmptySlots: la lista no es null y el mínimo es >= 0 (precondiciones,
    // las garantiza StationsPresenter), por eso no se prueban esos casos.
    // =====================================================================

    /** UFH.1a: mínimo 4 (valor intermedio, igual al de una estación): [4, 7]. */
    @Test
    public void testFilterByMinEmptySlotsIntermediateValue() {
        List<Station> result = StationFilter.filterByMinEmptySlots(stationsSlots, 4);

        assertEquals(Arrays.asList(h4, h7), result);
    }

    /** UFH.1b: mínimo 7 (igual al máximo, valor límite): [7]. */
    @Test
    public void testFilterByMinEmptySlotsEqualToMaximum() {
        List<Station> result = StationFilter.filterByMinEmptySlots(stationsSlots, 7);

        assertEquals(Arrays.asList(h7), result);
    }

    /** UFH.1c: mínimo 8 (justo por encima del máximo, valor límite): lista vacía. */
    @Test
    public void testFilterByMinEmptySlotsAboveMaximumReturnsEmpty() {
        List<Station> result = StationFilter.filterByMinEmptySlots(stationsSlots, 8);

        assertTrue(result.isEmpty());
    }

    /** UFH.1d: mínimo 1 (menor valor que filtra): [4, 7], la estación con 0 huecos no aparece. */
    @Test
    public void testFilterByMinEmptySlotsMinimumOne() {
        List<Station> result = StationFilter.filterByMinEmptySlots(stationsSlots, 1);

        assertEquals(Arrays.asList(h4, h7), result);
    }

    /** UFH.1e: mínimo 0: se devuelven todas las estaciones, también las que no tienen dato. */
    @Test
    public void testFilterByMinEmptySlotsZeroReturnsAll() {
        List<Station> result = StationFilter.filterByMinEmptySlots(stationsSlots, 0);

        assertEquals(Arrays.asList(h0, h4, h7, hNull, hNeg), result);
    }

    /** UFH.1g: lista de estaciones vacía: lista vacía. */
    @Test
    public void testFilterByMinEmptySlotsEmptyListReturnsEmpty() {
        List<Station> result = StationFilter.filterByMinEmptySlots(new ArrayList<>(), 1);

        assertTrue(result.isEmpty());
    }

    /** UFH.1i: solo estaciones sin dato válido (null, negativo, estación null) y mínimo 1: lista vacía. */
    @Test
    public void testFilterByMinEmptySlotsExcludesInvalidData() {
        List<Station> onlyBadStations = Arrays.asList(hNull, hNeg, null);

        List<Station> result = StationFilter.filterByMinEmptySlots(onlyBadStations, 1);

        assertTrue(result.isEmpty());
    }

    /** UFH.1j: se mantiene el orden de entrada y la lista original no se modifica. */
    @Test
    public void testFilterByMinEmptySlotsKeepsOrderAndDoesNotModifyList() {
        List<Station> input = Arrays.asList(h7, h0, h4);
        List<Station> original = new ArrayList<>(input);

        List<Station> result = StationFilter.filterByMinEmptySlots(input, 4);

        assertEquals(Arrays.asList(h7, h4), result);
        assertEquals(original, input);
    }

    /** UFH.2a-2e: hasValidEmptySlots con 0, 7, sin dato, negativo y estación null. */
    @Test
    public void testHasValidEmptySlots() {
        assertTrue(StationFilter.hasValidEmptySlots(h0));       // UFH.2a (valor límite)
        assertTrue(StationFilter.hasValidEmptySlots(h7));       // UFH.2b
        assertFalse(StationFilter.hasValidEmptySlots(hNull));   // UFH.2c
        assertFalse(StationFilter.hasValidEmptySlots(hNeg));    // UFH.2d
        assertFalse(StationFilter.hasValidEmptySlots(null));    // UFH.2e
    }
}