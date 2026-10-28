package es.unican.bicis.activities.stations;

import java.util.List;

import es.unican.bicis.model.Network;
import es.unican.bicis.model.Station;
import es.unican.bicis.repository.INetworksRepository;

/**
 * The Presenter-View contract for the Stations activity.
 * The Stations activity shows the stations of a bike sharing network,
 * allows to filter them by a minimum number of free bikes or by maximum
 * distance to the user, and gives access to the detailed information
 * of the network.
 */
public interface IStationsContract {

    /**
     * Methods that must be implemented in the Stations Presenter.
     * Only the View should call these methods.
     */
    public interface Presenter {

        /**
         * Links the presenter with its view and the network whose stations must be shown.
         * The presenter shows the network header and requests its stations to the repository.
         * Only the View should call this method
         * @param view the view controlled by this presenter
         * @param network the network whose stations must be shown
         */
        public void init(View view, Network network);

        /**
         * The presenter is informed that the user wants to filter the stations
         * by a minimum number of free bikes.
         * Only the View should call this method
         * @param minFreeBikesText the text introduced by the user
         */
        public void onFilterApplied(String minFreeBikesText);

        /**
         * Notifies the presenter that the user wants to remove
         * all applied station filters.
         *
         * If successful, the complete list of stations is restored.
         * If the operation fails, the view displays an error message.
         *
         * Only the view should call this method.
         */
        public void onFilterCleared();

        /**
         * The presenter is informed that the user wants to see the detailed
         * information of the network.
         * Only the View should call this method
         */
        public void onInfoClicked();

        /**
         * The presenter is informed that the user wants to filter the stations
         * by maximum distance to their current location.
         *
         * The presenter asks the view for the user's location. If the location
         * is not available, or if the introduced value is not a positive number,
         * the presenter asks the view to show the appropriate error message.
         * Otherwise, only the stations within the given distance are shown,
         * sorted by free bikes.
         *
         * Only the View should call this method.
         *
         * @param maxDistKmText the text introduced by the user (distance in km)
         */
        public void onDistanceFilterApplied(String maxDistKmText);

    }

    /**
     * Methods that must be implemented in the Stations View.
     * Only the Presenter should call these methods.
     */
    public interface View {

        /**
         * Returns a repository that can be used to access the stations of a network.
         * Only the Presenter should call this method
         * @return the repository
         */
        public INetworksRepository getNetworksRepository();

        /**
         * The view is requested to display the name of the network.
         * Only the Presenter should call this method
         * @param name the name of the network, or "-" if not available
         */
        public void showNetworkName(String name);

        /**
         * The view is requested to display the location of the network in the header.
         * Only the Presenter should call this method
         * @param location the text with city and country, optionally followed by the
         *                 number of stations (e.g. "Santander, España · 42 estaciones")
         */
        public void showNetworkLocation(String location);

        /**
         * The view is requested to display the list of stations, in the given order.
         * Only the Presenter should call this method
         * @param stations the stations already sorted (never null nor empty)
         */
        public void showStations(List<Station> stations);

        /**
         * The view is requested to hide the list and display a message indicating
         * that the network has no stations.
         * Only the Presenter should call this method
         */
        public void showNoStations();

        /**
         * The view is requested to hide the list and display a message indicating
         * that the stations could not be loaded.
         * Only the Presenter should call this method
         */
        public void showLoadError();

        /**
         * The view is requested to hide the list and display a message indicating
         * that no station has at least the given number of free bikes.
         * Only the Presenter should call this method
         * @param minFreeBikes the minimum number of free bikes requested
         */
        public void showNoStationsWithMinFreeBikes(int minFreeBikes);

        /**
         * The view is requested to display an error indicating that the introduced
         * value is not valid (it must be an integer greater than 0).
         * The filter that was active before, if any, remains unchanged.
         * Only the Presenter should call this method
         */
        public void showInvalidMinFreeBikes();

        /**
         * Requests the view to display an error message when
         * the applied station filters cannot be removed.
         *
         * The currently displayed stations and filter settings
         * should remain unchanged if the operation fails.
         *
         * Only the presenter should call this method.
         */
        public void mostrarErrorQuitarFiltros();

        /**
         * The view is requested to indicate that a filter is active.
         * Only the Presenter should call this method
         * @param minFreeBikes the minimum number of free bikes of the active filter
         */
        public void showFilterActive(int minFreeBikes);

        /**
         * The view is requested to indicate that no filter is active.
         * Only the Presenter should call this method
         */
        public void showFilterInactive();

        /**
         * The view is requested to open the detailed view of the given network.
         * Only the Presenter should call this method
         * @param network the network
         */
        public void showNetworkDetails(Network network);

        // ── HU Filtrar por distancia ──────────────────────────────────────

        /**
         * Returns the user's current location as a two-element array {latitude, longitude},
         * or null if the location is not available (GPS off, permission denied, etc.).
         * Only the Presenter should call this method.
         *
         * @return double[]{latitude, longitude} or null if unavailable
         */
        public double[] getUserLocation();

        /**
         * The view is requested to hide the list and display a message indicating
         * that no station is within the given maximum distance from the user.
         * Only the Presenter should call this method.
         *
         * @param maxDistKm the maximum distance requested by the user, in km
         */
        public void showNoStationsWithinDistance(double maxDistKm);

        /**
         * The view is requested to display an error indicating that the user's
         * location is not available and therefore the distance filter cannot be applied.
         * The filter that was active before, if any, remains unchanged.
         * Only the Presenter should call this method.
         */
        public void showLocationUnavailableForFilter();

        /**
         * The view is requested to display an error indicating that the introduced
         * distance value is not valid (it must be a positive number greater than 0).
         * The filter that was active before, if any, remains unchanged.
         * Only the Presenter should call this method.
         */
        public void showInvalidMaxDistance();

        /**
         * The view is requested to indicate that a distance filter is active.
         * Only the Presenter should call this method.
         *
         * @param maxDistKm the maximum distance of the active filter, in km
         */
        public void showDistanceFilterActive(double maxDistKm);

    }
}