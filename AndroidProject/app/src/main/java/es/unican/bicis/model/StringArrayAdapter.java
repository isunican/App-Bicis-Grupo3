package es.unican.bicis.model;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * GSON adapter that reads an array of texts in a tolerant way.
 *
 * The value is only accepted if it is a JSON array whose elements are all texts.
 * If the value is not an array, or any element is not a text (a number, a boolean,
 * null, an object...), the whole value is considered anomalous and the field
 * is set to null, meaning "not available".
 * Note: empty texts are accepted here; checking their content is a validation
 * responsibility of the model, not of the json reading.
 */
public class StringArrayAdapter extends TypeAdapter<String[]> {

    /**
     * Reads an array of texts from the json.
     * @param in the json reader
     * @return the array of texts, or null if the value is missing or has an invalid format
     * @throws IOException if the json cannot be read
     */
    @Override
    public String[] read(JsonReader in) throws IOException {
        if (in.peek() != JsonToken.BEGIN_ARRAY) {
            in.skipValue();
            return null;
        }

        List<String> values = new ArrayList<>();
        boolean valid = true;

        in.beginArray();
        while (in.hasNext()) {
            if (in.peek() == JsonToken.STRING) {
                values.add(in.nextString());
            } else {
                // invalid element: skip it, but keep reading to consume the whole array
                valid = false;
                in.skipValue();
            }
        }
        in.endArray();

        return valid ? values.toArray(new String[0]) : null;
    }

    /**
     * Writes an array of texts to the json.
     * @param out the json writer
     * @param value the array to write (may be null)
     * @throws IOException if the json cannot be written
     */
    @Override
    public void write(JsonWriter out, String[] value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.beginArray();
        for (String s : value) {
            out.value(s);
        }
        out.endArray();
    }
}