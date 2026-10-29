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

    // --- Datos para pruebas de bicis libres ---
    private Station s0;       // 0 free bikes
    private Station s3;       // 3 free bikes
    private Station s5;       // 5 free bikes
    private Station sNull;    // does not report free bikes
    private Station sNeg;     // invalid value (negative)
    private List<Station> stations;

    // --- Datos para pruebas de distancia ---
    private static final double USER_LAT = 43.4623;
    private static final double USER_LON = -3.8099;

    private Station sA; // ~0.3 km
    private Station sB; // ~0.8 km
    private Station sC; // ~1.0 km (ajustado para no pasarse por decimales)
    private Station sD; // ~1.5 km
    private Station sE; // Sin coordenadas

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

    private void setUpDistanceStations() {
        sA = createStationWithCoords("A", 43.4649, -3.8099);
        sB = createStationWithCoords("B", 43.4695, -3.8099);
        sC = createStationWithCoords("C", 43.47129, -3.8099); // Ajustado para dar < 1.0 km
        sD = createStationWithCoords("D", 43.4758, -3.8099);
        sE = new Station();
        sE.setName("E sin coordenadas");
    }

    private Station createStationWithCoords(String name, Double lat, Double lon) {
        Station station = new Station();
        station.setName(name);
        station.setLatitude(lat);
        station.setLongitude(lon);
        return station;
    }

    // =====================================================================
    // Pruebas de filterByMinFreeBikes
    // =====================================================================

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
    // HU 532304 — Pruebas de filterByMaxDistance
    // =====================================================================

    /** UFD.1a: maxDistKm=1.0 -> [A, B, C] (A, B y C a <= 1 km) */
    @Test
    public void testFilterByMaxDistanceSuccess() {
        setUpDistanceStations();
        List<Station> input = Arrays.asList(sA, sB, sC, sD, sE);
        List<Station> result = StationFilter.filterByMaxDistance(input, USER_LAT, USER_LON, 1.0);
        assertEquals(Arrays.asList(sA, sB, sC), result);
    }

    /** UFD.1b: maxDistKm=0.5 -> [A] (solo A a <= 0,5 km) */
    @Test
    public void testFilterByMaxDistanceSmallRadius() {
        setUpDistanceStations();
        List<Station> input = Arrays.asList(sA, sB, sC, sD, sE);
        List<Station> result = StationFilter.filterByMaxDistance(input, USER_LAT, USER_LON, 0.5);
        assertEquals(Arrays.asList(sA), result);
    }

    /** UFD.1c: maxDistKm=2.0 -> [A, B, C, D] (E excluida por no tener coordenadas) */
    @Test
    public void testFilterByMaxDistanceLargeRadiusExcludesNoCoords() {
        setUpDistanceStations();
        List<Station> input = Arrays.asList(sA, sB, sC, sD, sE);
        List<Station> result = StationFilter.filterByMaxDistance(input, USER_LAT, USER_LON, 2.0);
        assertEquals(Arrays.asList(sA, sB, sC, sD), result);
    }

    /** UFD.1d: maxDistKm=0.05 -> [] (ninguna estación a <= 0,05 km) */
    @Test
    public void testFilterByMaxDistanceNoResults() {
        setUpDistanceStations();
        List<Station> input = Arrays.asList(sA, sB, sC, sD, sE);
        List<Station> result = StationFilter.filterByMaxDistance(input, USER_LAT, USER_LON, 0.05);
        assertTrue(result.isEmpty());
    }

    /** UFD.1e: lista null -> [] */
    @Test
    public void testFilterByMaxDistanceNullList() {
        List<Station> result = StationFilter.filterByMaxDistance(null, USER_LAT, USER_LON, 1.0);
        assertTrue(result.isEmpty());
    }

    /** UFD.1f: maxDistKm=0.0 -> [] (distancia no positiva) */
    @Test
    public void testFilterByMaxDistanceZero() {
        setUpDistanceStations();
        List<Station> input = Arrays.asList(sA, sB);
        List<Station> result = StationFilter.filterByMaxDistance(input, USER_LAT, USER_LON, 0.0);
        assertTrue(result.isEmpty());
    }

    /** UFD.1g: maxDistKm=-1.0 -> [] (distancia negativa) */
    @Test
    public void testFilterByMaxDistanceNegative() {
        setUpDistanceStations();
        List<Station> input = Arrays.asList(sA, sB);
        List<Station> result = StationFilter.filterByMaxDistance(input, USER_LAT, USER_LON, -1.0);
        assertTrue(result.isEmpty());
    }

    /** UFD.1h: lista vacía -> [] */
    @Test
    public void testFilterByMaxDistanceEmptyList() {
        List<Station> result = StationFilter.filterByMaxDistance(new ArrayList<>(), USER_LAT, USER_LON, 1.0);
        assertTrue(result.isEmpty());
    }

    /** UFD.1i: Solo estación E (sin coords) -> [] */
    @Test
    public void testFilterByMaxDistanceOnlyNoCoords() {
        setUpDistanceStations();
        List<Station> input = Arrays.asList(sE);
        List<Station> result = StationFilter.filterByMaxDistance(input, USER_LAT, USER_LON, 1.0);
        assertTrue(result.isEmpty());
    }

    // =====================================================================
    // HU 532304 — Pruebas de hasValidCoordinates
    // =====================================================================

    /** UHC.1a: Coordenadas válidas -> true */
    @Test
    public void testHasValidCoordinatesValid() {
        Station s = createStationWithCoords("Valida", 43.46, -3.81);
        assertTrue(StationFilter.hasValidCoordinates(s));
    }

    /** UHC.1b: Estación null -> false */
    @Test
    public void testHasValidCoordinatesNullStation() {
        assertFalse(StationFilter.hasValidCoordinates(null));
    }

    /** UHC.1c: Latitud null -> false */
    @Test
    public void testHasValidCoordinatesNullLat() {
        Station s = createStationWithCoords("Lat null", null, -3.81);
        assertFalse(StationFilter.hasValidCoordinates(s));
    }

    /** UHC.1d: Longitud null -> false */
    @Test
    public void testHasValidCoordinatesNullLon() {
        Station s = createStationWithCoords("Lon null", 43.46, null);
        assertFalse(StationFilter.hasValidCoordinates(s));
    }

    /** UHC.1e: Valores límite máximos (90.0, 180.0) -> true */
    @Test
    public void testHasValidCoordinatesMaxLimits() {
        Station s = createStationWithCoords("Max limits", 90.0, 180.0);
        assertTrue(StationFilter.hasValidCoordinates(s));
    }

    /** UHC.1f: Valores límite mínimos (-90.0, -180.0) -> true */
    @Test
    public void testHasValidCoordinatesMinLimits() {
        Station s = createStationWithCoords("Min limits", -90.0, -180.0);
        assertTrue(StationFilter.hasValidCoordinates(s));
    }

    /** UHC.1g: Fuera de rango (91.0) -> false */
    @Test
    public void testHasValidCoordinatesOutOfRange() {
        Station s = createStationWithCoords("Out of range", 91.0, -3.81);
        assertFalse(StationFilter.hasValidCoordinates(s));
    }
}