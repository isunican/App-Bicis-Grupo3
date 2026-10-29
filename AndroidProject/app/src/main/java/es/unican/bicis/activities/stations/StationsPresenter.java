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
 * estaciones) and by distance (HU 532304), and decides what the view must show.
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

    private void load(String networkId) {
        INetworksRepository repository = view.getNetworksRepository();

        StationsCallback callback = new StationsCallback() {
            @Override
            public void onSuccess(List<Station> loadedStations) {
                stations = StationUtils.sortByFreeBikes(loadedStations);
                loadFailed = false;
                showAllStations();
            }

            @Override
            public void onFailure(Throwable e) {
                stations = new ArrayList<>();
                loadFailed = true;
                view.showLoadError();
            }
        };

        repository.requestStations(callback, networkId);
    }

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

    @Override
    public void onDistanceFilterApplied(String maxDistKmText) {
        Double maxDistKm = parseMaxDistance(maxDistKmText);
        if (maxDistKm == null) {
            view.showInvalidMaxDistance();
            return;
        }

        double[] userLocation = view.getUserLocation();
        if (userLocation == null) {
            view.showLocationUnavailableForFilter();
            return;
        }

        view.showDistanceFilterActive(maxDistKm);
        List<Station> filtered = StationFilter.filterByMaxDistance(
                stations, userLocation[0], userLocation[1], maxDistKm);

        if (filtered.isEmpty()) {
            view.showNoStationsWithinDistance(maxDistKm);
            view.showNetworkLocation(location);
        } else {
            showStationList(filtered);
        }
    }

    @Override
    public void onFilterCleared() {
        view.showFilterInactive();
        try {
            showAllStations();
        } catch (RuntimeException error) {
            view.mostrarErrorQuitarFiltros();
        }
    }

    @Override
    public void onInfoClicked() {
        if (network != null) {
            view.showNetworkDetails(network);
        }
    }

    private void showAllStations() {
        if (loadFailed) {
            view.showLoadError();
        } else if (stations.isEmpty()) {
            view.showNoStations();
        } else {
            showStationList(stations);
        }
    }

    private void showStationList(List<Station> list) {
        view.showStations(list);
        view.showNetworkLocation(location + " · " + list.size() + " estaciones");
    }

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
     * Converts the text introduced by the user into the maximum distance in km.
     * @param text the text introduced by the user
     * @return the value if it is a decimal number strictly greater than 0, or null otherwise
     */
    private Double parseMaxDistance(String text) {
        if (text == null) {
            return null;
        }
        try {
            double value = Double.parseDouble(text.trim().replace(",", "."));
            return value > 0 ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

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