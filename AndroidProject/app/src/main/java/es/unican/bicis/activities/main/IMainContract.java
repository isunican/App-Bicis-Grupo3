package es.unican.bicis.activities.main;

import java.util.List;

import es.unican.bicis.location.ILocationProvider;
import es.unican.bicis.model.Network;
import es.unican.bicis.repository.INetworksRepository;

/**
 * The Presenter-View contract for the Main activity.
 * The Main activity shows a list of bike sharing networks.
 */
public interface IMainContract {

    /**
     * Methods that must be implemented in the Main Presenter.
     * Only the View should call these methods.
     */
    public interface Presenter {

        /**
         * Links the presenter with its view.
         * Only the View should call this method
         * @param view
         */
        public void init(View view);

        /**
         * The presenter is informed that a network has been clicked
         * Only the View should call this method
         * @param network the network that has been clicked
         */
        public void onNetworkClicked(Network network);

        /**
         * The presenter is informed that the Info item in the menu has been clicked
         * Only the View should call this method
         */
        public void onMenuInfoClicked();

        /**
         * The presenter is informed of the user's answer to the location permission request.
         * Only the View should call this method
         * @param granted true if the user has given the location permission, false otherwise
         */
        public void onLocationPermissionResult(boolean granted);

    }

    /**
     * Methods that must be implemented in the Main View.
     * Only the Presenter should call these methods.
     */
    public interface View {

        /**
         * Initialize the view. Typically this should initialize all the listeners in the view.
         * Only the Presenter should call this method
         */
        public void init();

        /**
         * Returns a repository that can be called by the Presenter to retrieve bike networks.
         * This method must be located in the view because Android resources must be accessed
         * in order to instantiate a repository (for example Internet Access). This requires
         * dependencies to Android. We want to keep the Presenter free of Android dependencies,
         * therefore the Presenter should be unable to instantiate repositories and must rely on
         * the view to create the repository.
         * Only the Presenter should call this method
         * @return
         */
        public INetworksRepository getNetworksRepository();

        /**
         * The view is requested to display the given list of bike networks.
         * Only the Presenter should call this method
         * @param networks the list of networks
         */
        public void showNetworks(List<Network> networks);

        /**
         * The view is requested to display a notification indicating  that the networks
         * were loaded correctly.
         * Only the Presenter should call this method
         * @param networks
         */
        public void showLoadCorrect(int networks);

        /**
         * The view is requested to display a notificacion indicating that the networks
         * were not loaded correctly.
         * Only the Presenter should call this method
         */
        public void showLoadError();

        /**
         * The view is requested to display the detailed view of the given network.
         * Only the Presenter should call this method
         * @param network the network
         */
        public void showNetworkDetails(Network network);

        /**
         * The view is requested to open the info activity.
         * Only the Presenter should call this method
         */
        public void showInfoActivity();

        /**
         * The view is requested to ask the user for the location permission. If the permission
         * was already given, the view must inform the presenter directly.
         * Only the Presenter should call this method
         */
        public void requestLocationPermission();

        /**
         * The view is requested to display a message indicating that the location of the user
         * could not be obtained. The list of networks stays visible so the user can pick one.
         * Only the Presenter should call this method
         */
        public void showLocationError();

        /**
         * Returns the provider that gives the location of the device. Like the repository,
         * it is created in the view because it needs Android.
         * Only the Presenter should call this method
         * @return the location provider
         */
        public ILocationProvider getLocationProvider();

        /**
         * The view is requested to show the stations of the network of the user's city.
         * Only the Presenter should call this method
         * @param network the network detected from the user's location
         */
        public void showNetworkStations(Network network);

    }
}