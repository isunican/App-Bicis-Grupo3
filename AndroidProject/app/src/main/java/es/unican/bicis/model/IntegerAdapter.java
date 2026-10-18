package es.unican.bicis.model;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * GSON adapter that reads an integer value in a tolerant way.
 *
 * Only JSON numbers without decimals are accepted. Any other value (a text, a boolean,
 * a decimal number, an object...) is considered anomalous, it is skipped and the field
 * is set to null, meaning "not available". Without this adapter a single invalid value
 * would make the parsing of the whole response fail.
 */
public class IntegerAdapter extends TypeAdapter<Integer> {

    /**
     * Reads an integer from the json.
     * @param in the json reader
     * @return the integer, or null if the value is missing or is not an integer
     * @throws IOException if the json cannot be read
     */
    @Override
    public Integer read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NUMBER) {
            double value = in.nextDouble();
            if (value == Math.rint(value) && value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
                return (int) value;
            }
            return null;
        }
        in.skipValue();
        return null;
    }

    /**
     * Writes an integer to the json.
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
