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

        /**
         * The presenter is informed that the "select city" icon (the pin in the toolbar) has
         * been clicked. If the networks could not be loaded, the presenter asks the view to show
         * the load error. Otherwise it asks the view to show the country and city selectors and,
         * if the city of the user was detected, it leaves it selected and filters the list.
         * Only the View should call this method
         */
        public void onSelectCityClicked();

        /**
         * The presenter is informed that the user has selected a country in the country selector.
         * The presenter updates the city selector with the cities of that country and asks the
         * view to show the networks of the country.
         * Only the View should call this method
         * @param countryCode the ISO code of the selected country (for example "ES"),
         *                    or null if the user has selected the option "Todas"
         */
        public void onCountrySelected(String countryCode);

        /**
         * The presenter is informed that the user has selected a city in the city selector.
         * The presenter asks the view to show only the networks of that city.
         * Only the View should call this method
         * @param city the name of the selected city, or null if no city is selected
         *             (then the networks of the selected country are shown)
         */
        public void onCitySelected(String city);

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
         * The view is requested to display a message indicating that the user has not given
         * the location permission. The list of networks stays visible so the user can pick one.
         * Only the Presenter should call this method
         */
        public void showLocationPermissionDenied();

        /**
         * The view is requested to display a message indicating that the location of the device
         * could not be obtained. The list of networks stays visible so the user can pick one.
         * Only the Presenter should call this method
         */
        public void showLocationUnavailable();

        /**
         * The view is requested to display a message indicating that there is no bike sharing
         * network close to the user. The list of networks stays visible so the user can pick one.
         * Only the Presenter should call this method
         */
        public void showNoNetworkNearby();

        /**
         * Returns the provider that gives the location of the device. Like the repository,
         * it is created in the view because it needs Android.
         * Only the Presenter should call this method
         * @return the location provider
         */
        public ILocationProvider getLocationProvider();

        /**
         * The view is requested to show the city and the stations of the network detected
         * from the user's location.
         * Only the Presenter should call this method
         * @param network the network detected from the user's location
         */
        public void showNetworkStations(Network network);

        /**
         * The view is requested to show the country and city selectors above the list of
         * networks. The first option of the country selector is "Todas", followed by the given
         * countries (the view shows their name in Spanish). The city selector shows the cities
         * of the selected country, or no options if no country is selected.
         * Only the Presenter should call this method
         * @param countries the ISO codes of the countries with networks, already sorted by
         *                  their name in Spanish
         * @param country the ISO code of the country that must appear selected,
         *                or null if no country is selected
         * @param city the name of the city that must appear selected,
         *             or null if no city is selected
         */
        public void showCitySelector(List<String> countries, String country, String city);

        /**
         * The view is requested to replace the options of the city selector.
         * Only the Presenter should call this method
         * @param cities the cities of the selected country, already sorted alphabetically
         *               (empty if the option "Todas" is selected)
         * @param city the name of the city that must appear selected,
         *             or null if no city is selected
         */
        public void showCities(List<String> cities, String city);

        /**
         * The view is requested to display the message "No se han podido cargar las ciudades".
         * The country and city selectors must not show any option.
         * Only the Presenter should call this method
         */
        public void showCitiesLoadError();

        /**
         * The view is requested to display the message "Elige el país y la ciudad en los que
         * vas a usar las bicis", used when the city of the user could not be detected.
         * Only the Presenter should call this method
         */
        public void showChooseCityHint();

    }
}