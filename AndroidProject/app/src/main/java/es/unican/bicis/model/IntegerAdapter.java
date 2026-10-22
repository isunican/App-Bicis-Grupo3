package es.unican.bicis.model;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * GSON adapter that reads a count (bikes, slots...) in a tolerant way.
 *
 * Only whole JSON numbers greater than or equal to 0 are accepted. Any other value
 * (a negative number, a decimal, a text, a boolean...) is considered anomalous, it is
 * skipped and the field is set to null, meaning "not available". Without this adapter
 * a single invalid value would make the parsing of the whole network fail.
 */
public class IntegerAdapter extends TypeAdapter<Integer> {

    /**
     * Reads a count from the json.
     * @param in the json reader
     * @return the count, or null if the value is missing or is not a valid count
     * @throws IOException if the json cannot be read
     */
    @Override
    public Integer read(JsonReader in) throws IOException {
        if (in.peek() != JsonToken.NUMBER) {
            in.skipValue();
            return null;
        }
        // nextInt() would throw an exception with a decimal such as 7.5
        double value = in.nextDouble();
        if (value < 0 || value != Math.floor(value) || value > Integer.MAX_VALUE) {
            return null;
        }
        return (int) value;
    }

    /**
     * Writes a count to the json.
     * @param out the json writer
     * @param value the value to write (may be null)
     * @throws IOException if the json cannot be written
     */
    @Override
    public void write(JsonWriter out, Integer value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value);
        }
    }
}
