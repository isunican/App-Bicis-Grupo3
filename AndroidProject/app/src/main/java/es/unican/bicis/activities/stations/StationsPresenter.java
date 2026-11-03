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

    // FIX: estado de cada filtro activo (null = sin filtro)
    private Integer activeMinFreeBikes = null;
    private Double  activeMaxDistKm    = null;
    private double[] lastUserLocation  = null;

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
        // FIX: guardar estado y combinar filtros
        activeMinFreeBikes = minFreeBikes;
        view.showFilterActive(minFreeBikes);
        view.showActiveFiltersCount(contarFiltrosActivos());
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
        }
        // FIX: guardar estado y combinar filtros
        activeMaxDistKm   = maxDistKm;
        lastUserLocation  = userLocation;
        view.showDistanceFilterActive(maxDistKm);
        view.showActiveFiltersCount(contarFiltrosActivos());
        applyFilters();
    }

    @Override
    public void onFilterCleared() {
        // FIX: limpiar ambos filtros
        activeMinFreeBikes = null;
        activeMaxDistKm    = null;
        lastUserLocation   = null;
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

    /**
     * Aplica los filtros activos sobre la lista completa de estaciones y
     * pide a la vista que muestre el resultado.
     * Orden: primero filtra por bicis, luego por distancia (y ordena por ella),
     * y deja las estaciones sin coordenadas al final.
     */
    private void applyFilters() {
        List<Station> resultado = new ArrayList<>(stations);

        // FIX: filtro de bicis sobre la lista completa
        if (activeMinFreeBikes != null) {
            resultado = StationFilter.filterByMinFreeBikes(resultado, activeMinFreeBikes);
        }

        // FIX: filtro de distancia + ordenar por distancia + rellenar distanciaKm
        if (activeMaxDistKm != null && lastUserLocation != null) {
            resultado = StationFilter.filterByMaxDistance(
                    resultado, lastUserLocation[0], lastUserLocation[1], activeMaxDistKm);
            // Rellenar distanciaKm en cada estación y ordenar por ella
            for (Station s : resultado) {
                if (StationFilter.hasValidCoordinates(s)) {
                    s.setDistanciaKm(StationFilter.distanceKm(
                            lastUserLocation[0], lastUserLocation[1],
                            s.getLatitude(), s.getLongitude()));
                }
            }
            resultado = sortByDistancia(resultado);
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
            showStationList(resultado);
        }
    }
    /**
     * Ordena la lista por distanciaKm ascendente.
     * Las estaciones sin distancia (sin coordenadas) van al final.
     */
    private List<Station> sortByDistancia(List<Station> lista) {
        List<Station> sorted = new ArrayList<>(lista);
        sorted.sort(Comparator.comparing(
                Station::getDistanciaKm,
                Comparator.nullsLast(Comparator.naturalOrder())
        ));
        return sorted;
    }

    @Override
    public double getMaxCityDistanceKm() {
        List<Station> conCoords = new ArrayList<>();
        for (Station s : stations) {
            if (StationFilter.hasValidCoordinates(s)) {
                conCoords.add(s);
            }
        }
        double max = 0;
        for (int i = 0; i < conCoords.size(); i++) {
            for (int j = i + 1; j < conCoords.size(); j++) {
                double d = StationFilter.distanceKm(
                        conCoords.get(i).getLatitude(), conCoords.get(i).getLongitude(),
                        conCoords.get(j).getLatitude(), conCoords.get(j).getLongitude());
                if (d > max) max = d;
            }
        }
        return max;
    }

    private int contarFiltrosActivos() {
        int count = 0;
        if (activeMinFreeBikes != null) count++;
        if (activeMaxDistKm    != null) count++;
        return count;
    }
}