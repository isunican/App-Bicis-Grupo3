package es.unican.bicis.model;

import java.util.List;

/**
 * Finds the bike sharing network of the city where the user is, using the user's coordinates.
 * The nearest network to the user is chosen.
 */
public class NetworkLocator {

    /** Maximum distance (km) to consider that the user is in the city of a network */
    public static final double MAX_DISTANCE_KM = 20.0;

    /** Radius of the Earth in km */
    private static final double RADIO_TIERRA_KM = 6371.0;

    /**
     * Private constructor, this class only has static methods.
     */
    private NetworkLocator() {
    }

    /**
     * Returns the network that is nearest to the position of the user.
     *
     * @param networks list of networks
     * @param lat latitude of the user
     * @param lon longitude of the user
     * @param maxKm maximum distance in km
     * @return the nearest network, or null if there is none closer than maxKm
     */
    public static Network findNearestNetwork(List<Network> networks, double lat, double lon, double maxKm) {
        if (networks == null) {
            return null;
        }

        Network masCercana = null;
        double menorDistancia = maxKm;

        for (Network n : networks) {
            // networks without valid coordinates are skipped
            if (n == null || n.getLocation() == null || !n.getLocation().hasValidCoordinates()) {
                continue;
            }

            double distancia = distanceKm(lat, lon, n.getLocation().getLatitude(), n.getLocation().getLongitude());

            if (distancia <= menorDistancia) {
                menorDistancia = distancia;
                masCercana = n;
            }
        }
        return masCercana;
    }

    /**
     * Calculates the distance in km between two points using the Haversine formula.
     *
     * @param lat1 latitude of point 1
     * @param lon1 longitude of point 1
     * @param lat2 latitude of point 2
     * @param lon2 longitude of point 2
     * @return distance in km
     */
    public static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.pow(Math.sin(dLon / 2), 2);

        return RADIO_TIERRA_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}