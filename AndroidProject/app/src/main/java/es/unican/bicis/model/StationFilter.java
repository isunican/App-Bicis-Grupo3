package es.unican.bicis.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Filters the stations of a bike sharing network according to their number of free bikes,
 * their number of empty slots or their distance to the user.
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

    /**
     * Returns only the stations whose number of empty slots is equal to or greater than the given minimum.
     * If the minimum is 0, all the stations are returned, including those that do not report their
     * empty slots. Otherwise, stations that do not report their empty slots (null) or report an
     * invalid value (negative) are excluded, since it cannot be guaranteed that they have room to
     * leave a bike.
     *
     * Preconditions (guaranteed by the caller, the StationsPresenter):
     *     stations is not null
     *     minEmptySlots is equal to or greater than 0
     *
     * @param stations the stations to filter
     * @param minEmptySlots the minimum number of empty slots
     * @return a new list with the stations that fulfil the condition, in the same order.
     *         It is empty if no station fulfils it. The given list is not modified.
     */
    public static List<Station> filterByMinEmptySlots(List<Station> stations, int minEmptySlots) {
        List<Station> result = new ArrayList<>();
        if (minEmptySlots == 0) {
            result.addAll(stations);
            return result;
        }
        for (Station station : stations) {
            if (hasValidEmptySlots(station) && station.getEmptySlots() >= minEmptySlots) {
                result.add(station);
            }
        }
        return result;
    }

    /**
     * Checks whether a station reports a valid number of empty slots.
     *
     * @param station the station to check
     * @return true if the station is not null and its empty slots are not null and not negative
     */
    public static boolean hasValidEmptySlots(Station station) {
        return station != null && station.getEmptySlots() != null && station.getEmptySlots() >= 0;
    }

    // =========================================================================
    // HU 532304 — Filtrar las estaciones por distancia
    // =========================================================================

    /**
     * Devuelve únicamente las estaciones que se encuentran a una distancia igual o
     * inferior a {@code maxDistKm} kilómetros de la posición del usuario.
     *
     * <p>La distancia se calcula con la fórmula de Haversine. Las estaciones que
     * no informan de sus coordenadas (no se puede calcular su distancia) se añaden
     * al final, pero solo si hay al menos una estación dentro de la distancia.</p>
     *
     * @param stations   lista de estaciones a filtrar (puede ser {@code null})
     * @param userLat    latitud de la posición del usuario, en grados decimales
     * @param userLon    longitud de la posición del usuario, en grados decimales
     * @param maxDistKm  distancia máxima en kilómetros; debe ser estrictamente positiva
     * @return nueva lista con las estaciones cuya distancia al usuario es menor o
     *         igual a {@code maxDistKm}, en el mismo orden que en la lista de entrada,
     *         seguidas de las que no tienen coordenadas.
     *         Es vacía si ninguna cumple la condición, si la lista es {@code null}
     *         o si {@code maxDistKm} no es positivo.
     */
    public static List<Station> filterByMaxDistance(
            List<Station> stations, double userLat, double userLon, double maxDistKm) {

        List<Station> dentro  = new ArrayList<>();
        List<Station> sinCoords = new ArrayList<>();

        if (stations == null || maxDistKm <= 0) {
            return dentro; // lista vacía
        }
        for (Station station : stations) {
            if (!hasValidCoordinates(station)) {
                sinCoords.add(station);   // sin coords → al final
            } else {
                double dist = distanceKm(userLat, userLon,
                        station.getLatitude(), station.getLongitude());
                if (dist <= maxDistKm) {
                    dentro.add(station);  // dentro del radio → incluida
                }
                // fuera del radio y con coords → descartada (no aparece)
            }
        }
        if (!dentro.isEmpty()) {
            dentro.addAll(sinCoords);
        }
        return dentro;
    }

    /**
     * Comprueba si una estación tiene coordenadas geográficas válidas.
     *
     * <p>Se consideran válidas si tanto la latitud como la longitud son no
     * {@code null} y están dentro de los rangos geográficos permitidos:
     * latitud en [&minus;90, 90] y longitud en [&minus;180, 180].</p>
     *
     * @param station estación a comprobar
     * @return {@code true} si la estación no es {@code null} y sus coordenadas
     *         son válidas; {@code false} en caso contrario
     */
    public static boolean hasValidCoordinates(Station station) {
        if (station == null || station.getLatitude() == null || station.getLongitude() == null) {
            return false;
        }
        double lat = station.getLatitude();
        double lon = station.getLongitude();
        return lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180;
    }

    /**
     * Calcula la distancia en kilómetros entre dos puntos geográficos
     * usando la fórmula de Haversine.
     *
     * @param lat1 latitud del punto 1, en grados decimales
     * @param lon1 longitud del punto 1, en grados decimales
     * @param lat2 latitud del punto 2, en grados decimales
     * @param lon2 longitud del punto 2, en grados decimales
     * @return distancia en kilómetros entre los dos puntos
     */
    public static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        final double radioTierraKm = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.pow(Math.sin(dLon / 2), 2);
        return radioTierraKm * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
