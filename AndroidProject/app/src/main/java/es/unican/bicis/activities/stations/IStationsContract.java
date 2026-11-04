package es.unican.bicis.activities.stations;

import java.util.List;
import java.util.Map;

import es.unican.bicis.model.Network;
import es.unican.bicis.model.Station;
import es.unican.bicis.repository.INetworksRepository;

/**
 * The Presenter-View contract for the Stations activity.
 * The Stations activity shows the stations of a bike sharing network,
 * allows to filter them by a minimum number of free bikes and by a minimum
 * number of empty slots and by a maximum distance to the user, allows to search a
 * station by its name and gives access to the detailed information of the network.
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
         * The presenter is informed that the user wants to filter the stations
         * by a maximum distance to their current location (HU 532304).
         *
         * The presenter asks the view for the user's location. If the location
         * is not available, or if the introduced value is not a positive number,
         * the presenter asks the view to show the appropriate error message.
         * Otherwise, only the stations within the given distance are shown,
         * sorted from the nearest to the farthest; the stations whose distance
         * cannot be calculated go at the end. This filter is combined with the
         * other active filters.
         *
         * Only the View should call this method.
         *
         * @param maxDistKmText the text introduced by the user (distance in km)
         */
        public void onDistanceFilterApplied(String maxDistKmText);

        /**
         * Returns the maximum distance (in km) from the user to the farthest station
         * of the network with valid coordinates (HU 532304). It is used as the upper
         * limit of the distance bar. Returns 0 if the location of the user is not
         * available or no station has valid coordinates.
         * Only the View should call this method.
         *
         * @return the maximum distance of the city, in km
         */
        public double getMaxCityDistanceKm();

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
         * The presenter is informed that the text of the station search field has changed.
         * It is called every time the user adds or removes a letter, without pressing any button,
         * and also when the user submits the search.
         * The search ignores case and accents and keeps the order of the list of stations.
         * The presenter requests the view to show:
         * if some station contains the text: those stations, their number in the
         * header and their names as suggestions below the search field;
         * if the text is null, empty or blank: all the stations, without suggestions;
         * if no station contains the text: the "station not found" message, "0 estaciones"
         * in the header and, if there is one, the most similar station as a suggestion;
         * if the stations could not be loaded: the load error, without suggestions.
         *
         */
        public void onSearchTextChanged(String text);

        /**
         * The presenter is informed that the user has selected a station, either from the
         * search results or from the suggestion of the most similar station.
         * The presenter requests the view to show the location of that station.
         * Only the View should call this method
         * @param station the selected station (if null, nothing is shown)
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

        /**
         * The view is requested to display, below the search field, the names of the
         * given stations as suggestions, in the given order. When the user selects one of them,
         * the view informs the presenter with {@link Presenter#onStationSelected(Station)}.
         * An empty list hides the suggestions.
         * Only the Presenter should call this method
         * @param stations the stations to suggest (never null, may be empty)
         */
        public void showSearchSuggestions(List<Station> stations);

        /**
         * The view is requested to hide the list and display the message
         * "No existe ninguna estación con ese nombre", because no station of the
         * network contains the searched text. Until {@link #showSuggestion(Station)} is called,
         * the view also indicates that there are no similar suggestions.
         * The message disappears the next time the view is requested to show stations
         * or another message.
         * Only the Presenter should call this method
         */
        public void showStationNotFound();

        /**
         * The view is requested to display, together with the "station not found" message,
         * the suggestion "¿Quizás quisiste decir...?" followed by the name of the station whose
         * name is the most similar to the searched text. When the user selects the suggestion,
         * the view informs the presenter with {@link Presenter#onStationSelected(Station)}.
         * Only the Presenter should call this method
         * @param station the most similar station (never null)
         */
        public void showSuggestion(Station station);

        /**
         * The view is requested to display the name, the available bikes and the
         * location (latitude and longitude) of the given station.
         * Only the Presenter should call this method
         * @param station the station whose location must be shown
         */
        public void showStationLocation(Station station);

        // ── HU 532304 Filtrar por distancia ──────────────────────────────

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

        /**
         * The view is requested to display the list of stations with their distances.
         * Only the Presenter should call this method.
         * @param stations the stations already sorted by distance
         * @param distances map from station to its distance in km (stations without
         *                  coordinates are not in the map)
         */
        public void showStationsWithDistance(List<Station> stations, Map<Station, Double> distances);

    }
}