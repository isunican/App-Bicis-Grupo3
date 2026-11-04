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

    /**
     * Crea una estación con el nombre y el número de huecos libres indicados (HU 532335).
     *
     * @param name nombre de la estación
     * @param emptySlots número de huecos libres (puede ser null o negativo para simular datos anómalos)
     * @return la estación creada
     */
    private Station createStationWithEmptySlots(String name, Integer emptySlots) {
        Station station = new Station();
        station.setName(name);
        station.setEmptySlots(emptySlots);
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
        assertEquals(Arrays.asList(sA, sB, sC, sE), result);
    }

    // HU 532335 - Filtrar las estaciones por huecos disponibles
    // filterByMinEmptySlots: la lista no es null y el mínimo es >= 0 (precondiciones,
    // las garantiza StationsPresenter)

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

    /** UFD.1b: maxDistKm=0.5 -> [A] (solo A a <= 0,5 km) */
    @Test
    public void testFilterByMaxDistanceSmallRadius() {
        setUpDistanceStations();
        List<Station> input = Arrays.asList(sA, sB, sC, sD, sE);
        List<Station> result = StationFilter.filterByMaxDistance(input, USER_LAT, USER_LON, 0.5);
        assertEquals(Arrays.asList(sA, sE), result);
    }

    /** UFD.1c: maxDistKm=2.0 -> [A, B, C, D, E] (E sin coordenadas va al final) */
    @Test
    public void testFilterByMaxDistanceLargeRadiusIncludesNoCoordsAtEnd() {
        setUpDistanceStations();
        List<Station> input = Arrays.asList(sA, sB, sC, sD, sE);
        List<Station> result = StationFilter.filterByMaxDistance(input, USER_LAT, USER_LON, 2.0);
        assertEquals(5, result.size());
        assertTrue(result.containsAll(Arrays.asList(sA, sB, sC, sD, sE)));
        assertEquals(sE, result.get(result.size() - 1));  // sE al final
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