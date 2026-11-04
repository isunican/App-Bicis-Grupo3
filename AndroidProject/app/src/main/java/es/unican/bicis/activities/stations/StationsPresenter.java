package es.unican.bicis.activities.stations;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import es.unican.bicis.common.StationUtils;
import es.unican.bicis.model.Location;
import es.unican.bicis.model.Network;
import es.unican.bicis.model.Station;
import es.unican.bicis.model.StationFilter;
import es.unican.bicis.model.StationSearch;
import es.unican.bicis.repository.INetworksRepository;
import es.unican.bicis.repository.StationsCallback;
import java.util.HashMap;
import java.util.Map;

/**
 * The presenter of the stations activity of the application. It controls {@link StationsView}.
 * It loads the stations of a network and sorts them by free bikes (HU 532377 Listar estaciones),
 * filters them by a minimum number of free bikes introduced by the user (HU 532376 Filtrar
 * estaciones) and by distance (HU 532304), and decides what the view must show.
 * filters them by a minimum number of free bikes (HU 532376 Filtrar estaciones) and by a minimum
 * number of empty slots (HU 532335 Filtrar estaciones por huecos) introduced by the user,
 * searches stations by their name (HU 532314 Buscar estación por nombre) and decides what the
 * view must show. The filters can be combined: a station is shown only if it satisfies all the
 * active filters.
 */
public class StationsPresenter implements IStationsContract.Presenter {

    /** Minimum value accepted for the free bikes filter */
    static final int MIN_FILTER_VALUE = 1;

    /** Minimum value accepted for the empty slots filter (0 shows all the stations) */
    static final int MIN_EMPTY_SLOTS_FILTER_VALUE = 0;

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

    private Integer activeMinFreeBikes = null;
    private Double  activeMaxDistKm    = null;
    private double[] lastUserLocation  = null;

    /** Minimum number of free bikes of the active filter, or null if that filter is not active */
    private Integer minFreeBikes = null;

    /**
     * Minimum number of empty slots of the active filter, or null if that filter is not active.
     * A minimum of 0 is not stored, because it shows all the stations (it does not filter).
     */
    private Integer minEmptySlots = null;

    /**
     * @see IStationsContract.Presenter#init(IStationsContract.View, Network)
     */
    @Override
    public void init(IStationsContract.View view, Network network) {
        this.view = view;
        this.network = network;
        this.stations = new ArrayList<>();
        this.loadFailed = false;
        this.minFreeBikes = null;
        this.minEmptySlots = null;

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
        Integer value = parseMinFreeBikes(minFreeBikesText);
        if (value == null) {
            view.showInvalidMinFreeBikes();
            return;
        }

        activeMinFreeBikes = minFreeBikes;
        view.showFilterActive(minFreeBikes);
        applyFilters();
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
        minFreeBikes = value;
        view.showFilterActive(value);
        List<Station> filtered = applyFilters();
        if (filtered.isEmpty()) {
            view.showNoStationsWithMinFreeBikes(value);
            view.showNetworkLocation(location);
        } else {
            showStationList(filtered);
        }

        activeMaxDistKm   = maxDistKm;
        lastUserLocation  = userLocation;
        view.showDistanceFilterActive(maxDistKm);
        applyFilters();
    }

    /**
     * @see IStationsContract.Presenter#onMinEmptySlotsFilterApplied(String)
     * @param minEmptySlotsText the text introduced by the user
     */
    @Override
    public void onMinEmptySlotsFilterApplied(String minEmptySlotsText) {
        Integer value = parseMinEmptySlots(minEmptySlotsText);
        if (value == null) {
            view.showInvalidMinEmptySlots();
            return;
        }

        // With 0 all the stations are shown, so the empty slots filter is not active
        minEmptySlots = value > 0 ? value : null;
        view.showMinEmptySlotsFilterActive(value);
        List<Station> filtered = applyFilters();

        if (activeFiltersCount() == 0) {
            showAllStations();
        } else if (!filtered.isEmpty()) {
            showStationList(filtered);
        } else {
            if (minEmptySlots != null) {
                view.showNoStationsWithMinEmptySlots(value);
            } else {
                // Only the free bikes filter is active and no station satisfies it
                view.showNoStationsWithMinFreeBikes(minFreeBikes);
            }
            view.showNetworkLocation(location);
        }
    }

    @Override
    public void onFilterCleared() {

        activeMinFreeBikes = null;
        activeMaxDistKm    = null;
        lastUserLocation   = null;
        minFreeBikes = null;
        minEmptySlots = null;
        view.showFilterInactive();
        view.showActiveFiltersCount(0);
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

    /**
     * @see IStationsContract.Presenter#onSearchClicked()
     */
    @Override
    public void onSearchClicked() {
        if (view != null) {
            view.showSearchField();
        }
    }

    /**
     * @see IStationsContract.Presenter#onSearchTextChanged(String)
     * @param text the text introduced in the search field
     */
    @Override
    public void onSearchTextChanged(String text) {
        if (view == null) {
            return;
        }

        if (loadFailed) {
            view.showSearchSuggestions(new ArrayList<>());
            view.showLoadError();
            return;
        }

        if (text == null || text.trim().isEmpty()) {
            view.showSearchSuggestions(new ArrayList<>());
            showAllStations();
            return;
        }

        List<Station> matches = StationSearch.searchByName(stations, text);

        if (matches.isEmpty()) {
            view.showSearchSuggestions(new ArrayList<>());
            view.showStationNotFound();
            view.showNetworkLocation(location + " · 0 estaciones");
            Station mostSimilar = StationSearch.findMostSimilar(stations, text);
            if (mostSimilar != null) {
                view.showSuggestion(mostSimilar);
            }
            return;
        }

        showStationList(matches);
        view.showSearchSuggestions(matches);
    }

    /**
     * @see IStationsContract.Presenter#onStationSelected(Station)
     * @param station the selected station
     */
    @Override
    public void onStationSelected(Station station) {
        if (view != null && station != null) {
            view.showStationLocation(station);
        }
    }

    /**
     * Applies all the active filters to the stations of the network and tells the view
     * how many filters are active.
     * If the empty slots filter is active, the result is sorted from more to fewer empty
     * slots (HU 532335); otherwise it keeps the order of the complete list.
     * @return the stations that satisfy all the active filters
     */
    private List<Station> applyFilters() {
        List<Station> filtered = stations;
        if (minFreeBikes != null) {
            filtered = StationFilter.filterByMinFreeBikes(filtered, minFreeBikes);
        }
        if (minEmptySlots != null) {
            filtered = StationFilter.filterByMinEmptySlots(filtered, minEmptySlots);
            filtered = StationUtils.sortByEmptySlots(filtered);
        }
        view.showActiveFiltersCount(activeFiltersCount());
        return filtered;
    }

    /**
     * Counts the filters that are active.
     * @return the number of active filters (0, 1 or 2)
     */
    private int activeFiltersCount() {
        int count = 0;
        if (minFreeBikes != null) {
            count++;
        }
        if (minEmptySlots != null) {
            count++;
        }
        return count;
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

    private void showStationList(List<Station> list) {
        view.showStations(list);
        view.showNetworkLocation(location + " · " + list.size() + " estaciones");
    }


    /**
     * Converts the text introduced by the user into the minimum number of free bikes.
     * @param text the text introduced by the user
     * @return the value if it is an integer greater than or equal to MIN_FILTER_VALUE, or null otherwise
     */
    private Integer parseMinFreeBikes(String text) {
        return parseMinimum(text, MIN_FILTER_VALUE);
    }

    /**
     * Converts the text introduced by the user into the minimum number of empty slots.
     * @param text the text introduced by the user
     * @return the value if it is an integer greater than or equal to MIN_EMPTY_SLOTS_FILTER_VALUE,
     *         or null otherwise
     */
    private Integer parseMinEmptySlots(String text) {
        return parseMinimum(text, MIN_EMPTY_SLOTS_FILTER_VALUE);
    }

    /**
     * Converts a text into an integer that must be equal to or greater than a lower limit.
     * @param text the text introduced by the user (may be null)
     * @param lowerLimit the smallest valid value
     * @return the value, or null if the text is not an integer or is smaller than the limit
     */
    private Integer parseMinimum(String text, int lowerLimit) {
        if (text == null) {
            return null;
        }
        try {
            int value = Integer.parseInt(text.trim());
            return value >= lowerLimit ? value : null;
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

    private void showStationListWithDistance(List<Station> list, Map<Station, Double> distancias) {
        view.showStationsWithDistance(list, distancias);
        view.showNetworkLocation(location + " · " + list.size() + " estaciones");
    }

    /**
     * Aplica los filtros activos sobre la lista completa de estaciones y
     * pide a la vista que muestre el resultado.
     * Orden: primero filtra por bicis, luego por distancia (y ordena por ella),
     * y deja las estaciones sin coordenadas al final.
     */
    private void applyFilters() {
        List<Station> resultado = new ArrayList<>(stations);

        if (activeMinFreeBikes != null) {
            resultado = StationFilter.filterByMinFreeBikes(resultado, activeMinFreeBikes);
        }


        Map<Station, Double> distancias = null;
        if (activeMaxDistKm != null && lastUserLocation != null) {
            resultado = StationFilter.filterByMaxDistance(
                    resultado, lastUserLocation[0], lastUserLocation[1], activeMaxDistKm);
            distancias = new HashMap<>();
            for (Station s : resultado) {
                if (StationFilter.hasValidCoordinates(s)) {
                    distancias.put(s, StationFilter.distanceKm(
                            lastUserLocation[0], lastUserLocation[1],
                            s.getLatitude(), s.getLongitude()));
                }
            }
            resultado = sortByDistancia(resultado, distancias);
        }

        if (resultado.isEmpty()) {
            // Mostrar el mensaje apropiado según qué filtro dio vacío
            if (activeMaxDistKm != null) {
                view.showNoStationsWithinDistance(activeMaxDistKm);
            } else {
                view.showNoStationsWithMinFreeBikes(activeMinFreeBikes);
            }
            view.showNetworkLocation(location);
        } else {
            if (distancias != null) {
                showStationListWithDistance(resultado, distancias);
            } else {
                showStationList(resultado);
            }
        }
    }
    /**
     * Ordena la lista por distanciaKm ascendente.
     * Las estaciones sin distancia (sin coordenadas) van al final.
     */
    private List<Station> sortByDistancia(List<Station> lista, Map<Station, Double> distancias) {
        List<Station> sorted = new ArrayList<>(lista);
        sorted.sort(Comparator.comparing(
                s -> distancias.getOrDefault(s, null),
                Comparator.nullsLast(Comparator.naturalOrder())
        ));
        return sorted;
    }

    @Override
    public double getMaxCityDistanceKm() {
        double[] userLocation = view.getUserLocation();
        if (userLocation == null) return 0;
        double max = 0;
        for (Station s : stations) {
            if (StationFilter.hasValidCoordinates(s)) {
                double d = StationFilter.distanceKm(
                        userLocation[0], userLocation[1],
                        s.getLatitude(), s.getLongitude());
                if (d > max) max = d;
            }
        }
        return max;
    }

}