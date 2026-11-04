package es.unican.bicis.model;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Searches the stations of a bike sharing network by their name
 * (HU 532314 Buscar estación por nombre).
 * The search ignores case and accents ("plaza" finds "Plaza" and "perines" finds "Perinés").
 * It is used by {@link es.unican.bicis.activities.stations.StationsPresenter}:
 * {@link #searchByName(List, String)} updates the list while the user types, and
 * {@link #findMostSimilar(List, String)} gives the "¿Quizás quisiste decir...?" suggestion
 * when no station contains the text. This class only has static methods.
 */


public class StationSearch {

    /** Maximum number of letter changes for a name to be considered similar to the text */
    public static final int MAX_CHANGES = 2;

    /**
     * Private constructor: this class only has static methods and must not be instantiated.
     */
    private StationSearch() {
    }

    /**
     * The spaces at the beginning and at the end of the text are ignored.
     * Null stations and stations without name (null) never match a text.
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
     * Returns the station whose name is the most similar to the given text, ignoring case
     * and accents. A name is similar if the text can be converted into it by changing,
     * adding or removing at most {@link #MAX_CHANGES} letters. If several stations are
     * similar, the one that needs fewer changes is returned (the first one in the list
     * if there is a tie). The spaces at the beginning and at the end of the text and of the
     * names are ignored. Null stations and stations without name (null) are never suggested.
     * @param stations the stations to search in (may be null)
     * @param text the text introduced by the user (may be null)
     * @return the most similar station, or null if no station is similar, if the text is
     *         null, empty or blank, or if the list is null or empty
     */
    public static Station findMostSimilar(List<Station> stations, String text) {
        if (stations == null || text == null || text.trim().isEmpty()) {
            return null;
        }
        String normalizedText = normalize(text.trim());
        Station mostSimilar = null;
        int fewestChanges = MAX_CHANGES + 1;
        for (Station station : stations) {
            if (station != null && station.getName() != null) {
                int changes = countChanges(normalizedText, normalize(station.getName().trim()));
                if (changes < fewestChanges) {
                    mostSimilar = station;
                    fewestChanges = changes;
                }
            }
        }
        return mostSimilar;
    }

    /**
     * Returns the minimum number of letters that must be changed, added or removed to
     * convert one text into the other (Levenshtein distance).
     * For example, "magdalna" needs 1 change to become "magdalena".
     *
     * @param a the first text (not null)
     * @param b the second text (not null)
     * @return the number of changes, 0 if both texts are equal
     */
    static int countChanges(String a, String b) {
        // previous[j] = changes to convert the first i-1 letters of a into the first j letters of b
        int[] previous = new int[b.length() + 1];
        int[] current = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) {
            previous[j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            current[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int replace = previous[j - 1] + (a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1);
                int remove = previous[j] + 1;
                int add = current[j - 1] + 1;
                current[j] = Math.min(replace, Math.min(remove, add));
            }
            int[] aux = previous;
            previous = current;
            current = aux;
        }
        return previous[b.length()];
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