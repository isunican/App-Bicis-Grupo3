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

    @Before
    public void setUp() {
        s0 = createStation("Estacion 0", 0);
        s3 = createStation("Estacion 3", 3);
        s5 = createStation("Estacion 5", 5);
        sNull = createStation("Estacion sin dato", null);
        sNeg = createStation("Estacion negativa", -2);
        stations = Arrays.asList(s0, s3, s5, sNull, sNeg);
    }

    private Station createStation(String name, Integer freeBikes) {
        Station station = new Station();
        station.setName(name);
        station.setFreeBikes(freeBikes);
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
}
