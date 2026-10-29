package es.unican.bicis.activities.stations;

import java.util.List;

import es.unican.bicis.model.Network;
import es.unican.bicis.model.Station;
import es.unican.bicis.repository.INetworksRepository;

/**
 * The Presenter-View contract for the Stations activity.
 * The Stations activity shows the stations of a bike sharing network,
 * allows to filter them by a minimum number of free bikes and by a minimum
 * number of empty slots, and gives access to the detailed information of the network.
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
         * The presenter is informed that the user wants to filter the stations
         * by a minimum number of empty slots (HU 532335).
         * A value of 0 shows all the stations of the network. Negative or non
         * integer values are not valid. This filter is combined with the other
         * active filters (a station is shown only if it satisfies all of them).
         * Only the View should call this method
         * @param minEmptySlotsText the text introduced by the user
         */
        public void onMinEmptySlotsFilterApplied(String minEmptySlotsText);

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
         * The view is requested to hide the list and display a message indicating
         * that no station has at least the given number of empty slots (HU 532335).
         * Only the Presenter should call this method
         * @param minEmptySlots the minimum number of empty slots requested
         */
        public void showNoStationsWithMinEmptySlots(int minEmptySlots);

        /**
         * The view is requested to display an error indicating that the introduced
         * minimum number of empty slots is not valid (it must be an integer
         * greater than or equal to 0) (HU 532335).
         * The filters that were active before, if any, remain unchanged.
         * Only the Presenter should call this method
         */
        public void showInvalidMinEmptySlots();

        /**
         * The view is requested to indicate in the filter icon how many filters
         * are active (HU 532335). With 0 the icon shows no indicator.
         * Only the Presenter should call this method
         * @param numActiveFilters the number of active filters (0 or more)
         */
        public void showActiveFiltersCount(int numActiveFilters);

        /**
         * The view is requested to indicate which minimum number of empty slots
         * is active (HU 532335). With 0 no indication is shown, because with 0
         * all the stations are shown.
         * Only the Presenter should call this method
         * @param minEmptySlots the minimum number of empty slots of the active filter
         */
        public void showMinEmptySlotsFilterActive(int minEmptySlots);

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

    }
}