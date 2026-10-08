package es.unican.bicis.model;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * GSON adapter that reads a decimal number in a tolerant way.
 *
 * Only real JSON numbers are accepted. Any other value (a text, a boolean,
 * an object...) is considered anomalous, it is skipped and the field is set
 * to null, meaning "not available". Without this adapter a single invalid
 * coordinate would make the parsing of the whole response fail.
 */
public class DoubleAdapter extends TypeAdapter<Double> {

    /**
     * Reads a decimal number from the json.
     * @param in the json reader
     * @return the number, or null if the value is missing or is not a number
     * @throws IOException if the json cannot be read
     */
    @Override
    public Double read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NUMBER) {
            return in.nextDouble();
        }
        in.skipValue();
        return null;
    }

    /**
     * Writes a decimal number to the json.
     * @param out the json writer
     * @param value the value to write (may be null)
     * @throws IOException if the json cannot be written
     */
    @Override
    public void write(JsonWriter out, Double value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value);
        }
    }
}
