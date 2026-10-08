package es.unican.bicis.activities.details;

import java.util.List;

import es.unican.bicis.activities.main.IMainContract;
import es.unican.bicis.model.Network;
import es.unican.bicis.repository.INetworksRepository;
public interface IDetailsContract {

    /**
     * Methods that must be implemented in the Main Presenter.
     * Only the View should call these methods.
     */
    public interface Presenter {

        /**
         * Links the presenter with its view and the network to display.
         * The presenter processes the network data and asks the view to show it.
         * Only the View should call this method
         * @param view the view controlled by this presenter
         * @param network the network whose details must be shown
         */
        public void init(View view, Network network);

    }

    /**
     * Methods that must be implemented in the Main View.
     * Only the Presenter should call these methods.
     */
    public interface View {

        /**
         * The view is requested to display the name of the network.
         * Only the Presenter should call this method
         *
         * @param name the name of the network, or "-" if not available
         */
        public void showName(String name);

        /**
         * The view is requested to display the location of the network (city and country).
         * Only the Presenter should call this method
         *
         * @param location the text with city and country (e.g. "Santander, ES"), or "-" if not available
         */
        public void showLocation(String location);

        /**
         * The view is requested to display the latitude of the network.
         * Only the Presenter should call this method
         * @param latitude the latitude as text, or "-" if not available or invalid
         */
        public void showLatitude(String latitude);

        /**
         * The view is requested to display the longitude of the network.
         * Only the Presenter should call this method
         * @param longitude the longitude as text, or "-" if not available or invalid
         */
        public void showLongitude(String longitude);

        /**
         * The view is requested to display whether the network offers electric bikes.
         * Only the Presenter should call this method
         * @param ebikes "Sí", "No", or "-" if not available or invalid
         */
        public void showEbikes(String ebikes);

        /**
         * The view is requested to display the number of operating companies.
         * Only the Presenter should call this method
         * @param count the number of companies as text, or "-" if not available or invalid
         */
        public void showOperatorsCount(String count);

        /**
         * The view is requested to display the names of the operating companies.
         * Only the Presenter should call this method
         * @param operators the list of company names already formatted (one per line),
         *                  or "-" if not available or invalid
         */
        public void showOperators(String operators);

        /**
         * The view is requested to hide the detailed data and display a message
         * indicating that there is no detailed information available for this network.
         * Only the Presenter should call this method
         */
        public void showNoDetailsAvailable();
    }

}
