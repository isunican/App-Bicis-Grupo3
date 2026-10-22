package es.unican.bicis.activities.stations;

import java.util.List;

import es.unican.bicis.model.Network;
import es.unican.bicis.model.Station;
import es.unican.bicis.repository.INetworksRepository;

/**
 * The Presenter-View contract for the Stations activity.
 * The Stations activity shows the stations of a bike sharing network,
 * allows to filter them by a minimum number of free bikes and gives access
 * to the detailed information of the network.
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
         * The presenter is informed that the user wants to remove the filter.
         * Only the View should call this method
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
         * Returns a repository that can be called by the Presenter to retrieve the stations.
         * Only the Presenter should call this method
         * @return the repository to access the data
         */
        public INetworksRepository getNetworksRepository();

        /**
         * The view is requested to display the header of the network.
         * Only the Presenter should call this method
         * @param name the name of the network, or "-" if not available
         * @param location the city and country of the network, or "-" if not available
         */
        public void showNetworkInfo(String name, String location);

        /**
         * The view is requested to display the given list of stations, in the given order.
         * Only the Presenter should call this method
         * @param stations the stations to display (never empty)
         */
        public void showStations(List<Station> stations);

        /**
         * The view is requested to display a message indicating that the network
         * has no stations.
         * Only the Presenter should call this method
         */
        public void showNoStations();

        /**
         * The view is requested to display a message indicating that the stations
         * could not be loaded.
         * Only the Presenter should call this method
         */
        public void showLoadError();

        /**
         * The view is requested to display a message indicating that no station
         * has at least the given number of free bikes.
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
