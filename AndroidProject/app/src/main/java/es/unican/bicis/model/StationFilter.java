package es.unican.bicis.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Filters the stations of a bike sharing network according to their number of free bikes.
 */
public class StationFilter {

    // Private constructor: this class only has static methods
    private StationFilter() {
    }

    /**
     * Returns only the stations whose number of free bikes is equal to or greater than the given minimum.
     * Stations that do not report their free bikes (null) or report an invalid value (negative)
     * are excluded, since it cannot be guaranteed that they have bikes.
     *
     * @param stations the stations to filter (may be null)
     * @param minFreeBikes the minimum number of free bikes
     * @return a new list with the stations that fulfil the condition, in the same order.
     *         It is empty if no station fulfils it, if the list is null or if the minimum is negative.
     */
    public static List<Station> filterByMinFreeBikes(List<Station> stations, int minFreeBikes) {
        List<Station> result = new ArrayList<>();
        if (stations == null || minFreeBikes < 0) {
            return result;
        }
        for (Station station : stations) {
            if (hasValidFreeBikes(station) && station.getFreeBikes() >= minFreeBikes) {
                result.add(station);
            }
        }
        return result;
    }

    /**
     * Checks whether a station reports a valid number of free bikes.
     *
     * @param station the station to check
     * @return true if the station is not null and its free bikes are not null and not negative
     */
    public static boolean hasValidFreeBikes(Station station) {
        return station != null && station.getFreeBikes() != null && station.getFreeBikes() >= 0;
    }
}
