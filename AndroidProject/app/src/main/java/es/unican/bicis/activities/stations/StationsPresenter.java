package es.unican.bicis.activities.stations;

import java.util.ArrayList;
import java.util.List;

import es.unican.bicis.common.StationUtils;
import es.unican.bicis.model.Location;
import es.unican.bicis.model.Network;
import es.unican.bicis.model.Station;
import es.unican.bicis.model.StationFilter;
import es.unican.bicis.repository.INetworksRepository;
import es.unican.bicis.repository.StationsCallback;

/**
 * The presenter of the stations activity of the application. It controls {@link StationsView}.
 * It loads the stations of a network and sorts them by free bikes (HU 532377 Listar estaciones),
 * filters them by a minimum number of free bikes introduced by the user (HU 532376 Filtrar
 * estaciones) and decides what the view must show.
 */
public class StationsPresenter implements IStationsContract.Presenter {

    /** Minimum value accepted for the free bikes filter */
    static final int MIN_FILTER_VALUE = 1;

    /** The view that is controlled by this presenter */
    private IStationsContract.View view;

    /** The network whose stations are shown */
    private Network network;

    /** City and country of the network, as shown in the header */
    private String location;

    /** All the stations of the network, sorted by free bikes and without filters */
    private List<Station> stations = new ArrayList<>();

    /** Whether the stations could not be loaded */
    private boolean loadFailed = false;

    /**
     * @see IStationsContract.Presenter#init(IStationsContract.View, Network)
     * @param view the view to control
     * @param network the network whose stations must be shown
     */
    @Override
    public void init(IStationsContract.View view, Network network) {
        this.view = view;
        this.network = network;
        this.stations = new ArrayList<>();
        this.loadFailed = false;

        if (network == null) {
            loadFailed = true;
            view.showLoadError();
            return;
        }

        // Header: always shown, even if the stations cannot be loaded
        location = formatLocation(network.getLocation());
        view.showNetworkName(StationUtils.formatText(network.getName()));
        view.showNetworkLocation(location);

        String id = network.getId();
        if (id == null || id.trim().isEmpty()) {
            loadFailed = true;
            view.showLoadError();
            return;
        }

        load(id);
    }

    /**
     * Requests the stations of the network to the repository and sends them to the view.
     * @param networkId id of the network
     */
    private void load(String networkId) {
        INetworksRepository repository = view.getNetworksRepository();

        StationsCallback callback = new StationsCallback() {

            /**
             * Sorts the received stations by free bikes and shows them.
             * @param loadedStations the stations of the network (may be null or empty)
             */
            @Override
            public void onSuccess(List<Station> loadedStations) {
                stations = StationUtils.sortByFreeBikes(loadedStations);
                loadFailed = false;
                showAllStations();
            }

            /**
             * Shows the load error when the stations cannot be retrieved.
             * @param e the information about the failure
             */
            @Override
            public void onFailure(Throwable e) {
                stations = new ArrayList<>();
                loadFailed = true;
                view.showLoadError();
            }
        };

        repository.requestStations(callback, networkId);
    }

    /**
     * @see IStationsContract.Presenter#onFilterApplied(String)
     * @param minFreeBikesText the text introduced by the user
     */
    @Override
    public void onFilterApplied(String minFreeBikesText) {
        Integer minFreeBikes = parseMinFreeBikes(minFreeBikesText);
        if (minFreeBikes == null) {
            view.showInvalidMinFreeBikes();
            return;
        }

        view.showFilterActive(minFreeBikes);
        List<Station> filtered = StationFilter.filterByMinFreeBikes(stations, minFreeBikes);
        if (filtered.isEmpty()) {
            view.showNoStationsWithMinFreeBikes(minFreeBikes);
            view.showNetworkLocation(location);
        } else {
            showStationList(filtered);
        }
    }

    /**
     * @see IStationsContract.Presenter#onMinEmptySlotsFilterApplied(String)
     * @param minEmptySlotsText the text introduced by the user
     */
    @Override
    public void onMinEmptySlotsFilterApplied(String minEmptySlotsText) {
        // Se implementa en la tarea 978157 (filtro por huecos en el presenter)
    }

    /**
     * Removes the applied station filters and restores the complete
     * list of stations.
     *
     * If no filters are currently applied, the complete list is
     * displayed again without any additional changes.
     *
     * If the view fails to display the stations, the presenter
     * requests the view to show an error message.
     *
     * @see IStationsContract.Presenter#onFilterCleared()
     */
    @Override
    public void onFilterCleared() {
        view.showFilterInactive();
        try {
            showAllStations();
        } catch (RuntimeException error) {
            view.mostrarErrorQuitarFiltros();
        }
    }

    /**
     * @see IStationsContract.Presenter#onInfoClicked()
     */
    @Override
    public void onInfoClicked() {
        if (network != null) {
            view.showNetworkDetails(network);
        }
    }

    /**
     * Shows all the stations of the network, or the corresponding message
     * if they could not be loaded or there are none.
     */
    private void showAllStations() {
        if (loadFailed) {
            view.showLoadError();
        } else if (stations.isEmpty()) {
            view.showNoStations();
        } else {
            showStationList(stations);
        }
    }

    /**
     * Shows the given stations and their number in the header.
     * @param list the stations to show (not empty)
     */
    private void showStationList(List<Station> list) {
        view.showStations(list);
        view.showNetworkLocation(location + " · " + list.size() + " estaciones");
    }
//
    /**
     * Converts the text introduced by the user into the minimum number of free bikes.
     * @param text the text introduced by the user
     * @return the value if it is an integer greater than or equal to MIN_FILTER_VALUE, or null otherwise
     */
    private Integer parseMinFreeBikes(String text) {
        if (text == null) {
            return null;
        }
        try {
            int value = Integer.parseInt(text.trim());
            return value >= MIN_FILTER_VALUE ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Builds the location text with the city and the country of the network.
     * @param location the location of the network
     * @return "city, country", only one of them if the other is missing, or "-" if both are missing
     */
    private String formatLocation(Location location) {
        if (location == null) {
            return StationUtils.NO_DATA;
        }
        String city = StationUtils.formatText(location.getCity());
        String country = StationUtils.formatText(location.getCountry());

        if (city.equals(StationUtils.NO_DATA)) {
            return country;
        }
        if (country.equals(StationUtils.NO_DATA)) {
            return city;
        }
        return city + ", " + country;
    }
}
