package es.unican.bicis.model;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Searches the stations of a bike sharing network by their name.
 * The search ignores case and accents ("plaza" finds "Plaza" and "perines" finds "Perinés").
 */
public class StationSearch {

    // Private constructor: this class only has static methods
    private StationSearch() {
    }

    /**
     * Returns the stations whose name contains the given text, ignoring case and accents.
     * Stations without name (null) never match a text.
     *
     * @param stations the stations to search in (may be null)
     * @param text the text introduced by the user (may be null)
     * @return a new list with the matching stations, in the same order as the given list.
     *         It contains all the stations if the text is null, empty or blank,
     *         and it is empty if no station matches or if the list is null.
     */
    public static List<Station> searchByName(List<Station> stations, String text) {
        List<Station> result = new ArrayList<>();
        if (stations == null) {
            return result;
        }
        if (text == null || text.trim().isEmpty()) {
            result.addAll(stations);
            return result;
        }
        String normalizedText = normalize(text.trim());
        for (Station station : stations) {
            if (station != null && station.getName() != null
                    && normalize(station.getName()).contains(normalizedText)) {
                result.add(station);
            }
        }
        return result;
    }

    /**
     * Converts a text to lower case and removes its accents, so that two texts
     * can be compared ignoring them (e.g. "Perinés" becomes "perines").
     *
     * @param text the text to normalize (not null)
     * @return the text in lower case and without accents
     */
    static String normalize(String text) {
        String withoutAccents = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return withoutAccents.toLowerCase(Locale.ROOT);
    }
}