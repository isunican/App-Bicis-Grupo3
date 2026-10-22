package es.unican.bicis.activities.stations;

import java.util.List;

import es.unican.bicis.model.Network;
import es.unican.bicis.model.Station;
import es.unican.bicis.repository.INetworksRepository;

/**
 * Contract between the view and the presenter of the list of stations of a network.
 */
public interface IStationsContract {

    /**
     * Methods that must be implemented in the Stations Presenter.
     * Only the View should call these methods.
     */
    public interface Presenter {

        /**
         * Links the presenter with its view and the network whose stations must be shown.
         * The presenter shows the header of the network and requests its stations.
         * Only the View should call this method
         * @param view the view controlled by this presenter
         * @param network the network whose stations must be shown
         */
        public void init(View view, Network network);

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
    }

}
