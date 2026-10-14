package es.unican.bicis.model;

import java.util.List;

/**
 * Clase para saber en que red de bicis esta el usuario a partir de su ubicacion.
 * Se busca la red mas cercana a sus coordenadas.
 */
public class NetworkLocator {

    // Distancia maxima (en km) para considerar que el usuario esta en la ciudad de una red
    public static final double MAX_DISTANCE_KM = 20.0;

    // Radio de la Tierra en km
    private static final double RADIO_TIERRA_KM = 6371.0;

    // Constructor privado porque solo tiene metodos estaticos
    private NetworkLocator() {
    }

    /**
     * Devuelve la red mas cercana a la posicion del usuario.
     *
     * @param networks lista de redes
     * @param lat latitud del usuario
     * @param lon longitud del usuario
     * @param maxKm distancia maxima en km
     * @return la red mas cercana, o null si no hay ninguna a menos de maxKm
     */
    public static Network findNearestNetwork(List<Network> networks, double lat, double lon, double maxKm) {
        if (networks == null) {
            return null;
        }

        Network masCercana = null;
        double menorDistancia = maxKm;

        for (Network n : networks) {
            // Si la red no tiene coordenadas validas la saltamos
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
     * Calcula la distancia en km entre dos puntos usando la formula de Haversine
     *
     * @param lat1 latitud del punto 1
     * @param lon1 longitud del punto 1
     * @param lat2 latitud del punto 2
     * @param lon2 longitud del punto 2
     * @return distancia en km
     */
    public static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.pow(Math.sin(dLon / 2), 2);

        return RADIO_TIERRA_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}