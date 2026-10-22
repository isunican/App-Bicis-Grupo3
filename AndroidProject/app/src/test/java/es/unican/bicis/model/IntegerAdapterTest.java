package es.unican.bicis.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.gson.Gson;

import org.junit.Test;

/**
 * Unit tests for {@link IntegerAdapter}, used by the numeric fields of {@link Station}.
 * Invalid or missing values must be read as null, without making the parsing fail.
 */
public class IntegerAdapterTest {

    private final Gson gson = new Gson();

    private Station parse(String json) {
        return gson.fromJson(json, Station.class);
    }

    /** UIA.1a: número entero válido. */
    @Test
    public void testValidIntegerIsRead() {
        Station station = parse("{\"name\": \"A\", \"free_bikes\": 5, \"empty_slots\": 0}");

        assertEquals(Integer.valueOf(5), station.getFreeBikes());
        assertEquals(Integer.valueOf(0), station.getEmptySlots());
    }

    /** UIA.1b: número entero negativo. */
    @Test
    public void testNegativeIntegerIsReadAsItIs() {
        // The value is read; it is the filter who decides that it is not valid
        Station station = parse("{\"free_bikes\": -2}");

        assertEquals(Integer.valueOf(-2), station.getFreeBikes());
    }

    /** UIA.1c: campo ausente. */
    @Test
    public void testMissingValueIsNull() {
        Station station = parse("{\"name\": \"A\"}");

        assertNull(station.getFreeBikes());
    }

    /** UIA.1d: valor null. */
    @Test
    public void testNullValueIsNull() {
        Station station = parse("{\"free_bikes\": null}");

        assertNull(station.getFreeBikes());
    }

    /** UIA.1e: texto en lugar de número. */
    @Test
    public void testTextValueIsNull() {
        Station station = parse("{\"free_bikes\": \"abc\"}");

        assertNull(station.getFreeBikes());
    }

    /** UIA.1f: número decimal. */
    @Test
    public void testDecimalValueIsNull() {
        Station station = parse("{\"free_bikes\": 2.5}");

        assertNull(station.getFreeBikes());
    }

    /** UIA.1g: booleano u objeto en lugar de número. */
    @Test
    public void testBooleanAndObjectValuesAreNull() {
        assertNull(parse("{\"free_bikes\": true}").getFreeBikes());
        assertNull(parse("{\"free_bikes\": {\"a\": 1}}").getFreeBikes());
    }

    /** UIA.1h: un valor no válido no impide leer el resto de campos. */
    @Test
    public void testInvalidValueDoesNotBreakTheOtherFields() {
        Station station = parse("{\"free_bikes\": \"abc\", \"name\": \"Plaza\", \"empty_slots\": 7}");

        assertNull(station.getFreeBikes());
        assertEquals("Plaza", station.getName());
        assertEquals(Integer.valueOf(7), station.getEmptySlots());
    }

    /** UIA.1i: las bicis eléctricas (extra.ebikes) se leen de la misma forma. */
    @Test
    public void testElectricBikesUseTheSameTolerantReading() {
        assertEquals(Integer.valueOf(3), parse("{\"extra\": {\"ebikes\": 3}}").getEbikes());
        assertNull(parse("{\"extra\": {\"ebikes\": \"x\"}}").getEbikes());
    }
}
