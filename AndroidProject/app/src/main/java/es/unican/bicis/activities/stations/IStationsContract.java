package es.unican.bicis.activities.stations;

import java.util.List;

import es.unican.bicis.model.Network;
import es.unican.bicis.model.Station;
import es.unican.bicis.repository.INetworksRepository;

/**
 * The Presenter-View contract for the Stations activity.
 * The Stations activity shows the stations of a bike sharing network,
 * allows to filter them by a minimum number of free bikes, allows to search
 * a station by its name and gives access to the detailed information of the network.
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
         * The presenter is informed that the user has pressed the search icon (magnifying glass)
         * of the toolbar. The presenter requests the view to show the search field.
         * Only the View should call this method
         */
        public void onSearchClicked();

        /**
         * The presenter is informed that the text of the station search field has changed.
         * It is called every time the user adds or removes a letter, without pressing any button.
         * The presenter requests the view to show the stations whose name contains the text
         * (ignoring case and accents, in the same order as the list of stations), all the
         * stations if the text is empty or blank, or the "station not found" message, together
         * with the most similar station as a suggestion if there is one. It also requests the
         * view to show, below the search field, the names of the matching stations as suggestions.
         * Only the View should call this method
         * @param text the text introduced in the search field (may be null or empty)
         */
        public void onSearchTextChanged(String text);

        /**
         * The presenter is informed that the user has selected a station, either from the
         * search results or from the suggestion of the most similar station.
         * The presenter requests the view to show the location of that station.
         * Only the View should call this method
         * @param station the selected station
         */
        public void onStationSelected(Station station);

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

        /**
         * The view is requested to expand and display the station search field,
         * ready for the user to type.
         * Only the Presenter should call this method
         */
        public void showSearchField();

        /**
         * The view is requested to display, below the search field, the names of the
         * given stations as suggestions, in the given order. The user can select one of them.
         * An empty list hides the suggestions.
         * Only the Presenter should call this method
         * @param stations the stations to suggest (never null, may be empty)
         */
        public void showSearchSuggestions(List<Station> stations);

        /**
         * The view is requested to hide the list and display the message
         * "No existe ninguna estación con ese nombre", because no station of the
         * network contains the searched text.
         * The message disappears the next time the view is requested to show stations.
         * Only the Presenter should call this method
         */
        public void showStationNotFound();

        /**
         * The view is requested to display, together with the "station not found" message,
         * the suggestion "¿Querías decir [name]?" with the station whose name is the most
         * similar to the searched text. The user can select the suggestion.
         * Only the Presenter should call this method
         * @param station the most similar station (never null)
         */
        public void showSuggestion(Station station);

        /**
         * The view is requested to display the location (latitude and longitude)
         * of the given station.
         * Only the Presenter should call this method
         * @param station the station whose location must be shown
         */
        public void showStationLocation(Station station);

    }
}
