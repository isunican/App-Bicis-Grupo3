package es.unican.bicis.model;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * GSON adapter that reads a boolean value in a tolerant way.
 *
 * Only real JSON booleans (true / false) are accepted. Any other value
 * (a text such as "yes", a number, an object...) is considered anomalous,
 * it is skipped and the field is set to null, meaning "not available".
 * This avoids GSON's default behaviour, which silently converts some
 * invalid values into false or fails to parse the whole response.
 */
public class BooleanAdapter extends TypeAdapter<Boolean> {

    /**
     * Reads a boolean from the json.
     * @param in the json reader
     * @return the boolean value, or null if the value is missing or is not a boolean
     * @throws IOException if the json cannot be read
     */
    @Override
    public Boolean read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.BOOLEAN) {
            return in.nextBoolean();
        }
        in.skipValue();
        return null;
    }

    /**
     * Writes a boolean to the json.
     * @param out the json writer
     * @param value the value to write (may be null)
     * @throws IOException if the json cannot be written
     */
    @Override
    public void write(JsonWriter out, Boolean value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value);
        }
    }
}