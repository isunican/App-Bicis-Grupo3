package es.unican.bicis.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Filtra las estaciones de una red de bicicletas compartidas según distintos criterios.
 *
 * <p>Todos los métodos son estáticos. Esta clase no se instancia.</p>
 */
public class StationFilter {

    // Constructor privado: clase de utilidades estáticas
    private StationFilter() {
    }

    // =========================================================================
    // HU 532376 — Filtrar las estaciones por número mínimo de bicis libres
    // =========================================================================

    /**
     * Devuelve únicamente las estaciones cuyo número de bicis libres es igual o
     * superior al mínimo indicado.
     *
     * <p>Las estaciones que no informan de sus bicis libres ({@code null}) o que
     * informan de un valor no válido (negativo) se excluyen, porque no se puede
     * garantizar que tengan bicis disponibles.</p>
     *
     * @param stations     lista de estaciones a filtrar (puede ser {@code null})
     * @param minFreeBikes número mínimo de bicis libres requerido
     * @return nueva lista con las estaciones que cumplen la condición, en el mismo
     *         orden que en la lista de entrada.
     *         Es vacía si ninguna cumple la condición, si la lista es {@code null}
     *         o si el mínimo es negativo.
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
     * Comprueba si una estación informa de un número válido de bicis libres.
     *
     * <p>Se considera válido un valor no {@code null} y no negativo.</p>
     *
     * @param station estación a comprobar
     * @return {@code true} si la estación no es {@code null}, sus bicis libres no
     *         son {@code null} y el valor es mayor o igual que cero
     */
    public static boolean hasValidFreeBikes(Station station) {
        return station != null
                && station.getFreeBikes() != null
                && station.getFreeBikes() >= 0;
    }

    // =========================================================================
    // HU 532304 — Filtrar las estaciones por distancia
    // =========================================================================

    /**
     * Devuelve únicamente las estaciones que se encuentran a una distancia igual o
     * inferior a {@code maxDistKm} kilómetros de la posición del usuario.
     *
     * <p>La distancia se calcula con la fórmula de Haversine. Las estaciones que
     * no informan de sus coordenadas (latitud o longitud {@code null}) se excluyen,
     * ya que no es posible calcular su distancia.</p>
     *
     * @param stations   lista de estaciones a filtrar (puede ser {@code null})
     * @param userLat    latitud de la posición del usuario, en grados decimales
     * @param userLon    longitud de la posición del usuario, en grados decimales
     * @param maxDistKm  distancia máxima en kilómetros; debe ser estrictamente positiva
     * @return nueva lista con las estaciones cuya distancia al usuario es menor o
     *         igual a {@code maxDistKm}, en el mismo orden que en la lista de entrada.
     *         Es vacía si ninguna cumple la condición, si la lista es {@code null}
     *         o si {@code maxDistKm} no es positivo.
     */
    public static List<Station> filterByMaxDistance(
            List<Station> stations, double userLat, double userLon, double maxDistKm) {

        List<Station> result = new ArrayList<>();
        if (stations == null || maxDistKm <= 0) {
            return result;
        }
        for (Station station : stations) {
            if (hasValidCoordinates(station)) {
                double dist = distanceKm(userLat, userLon,
                        station.getLatitude(), station.getLongitude());
                if (dist <= maxDistKm) {
                    result.add(station);
                }
            }
        }
        return result;
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
    static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
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