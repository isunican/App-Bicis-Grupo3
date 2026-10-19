package es.unican.bicis.activities.stations;

import java.util.ArrayList;
import java.util.List;

import es.unican.bicis.model.Station;
import es.unican.bicis.model.StationFilter;

/**
 * The presenter of the stations activity of the application.
 * It receives the minimum number of free bikes introduced by the user, checks it,
 * filters the stations and decides what the view must show.
 */
public class StationsPresenter implements IStationsContract.Presenter {

    /** Minimum value accepted for the free bikes filter */
    static final int MIN_FILTER_VALUE = 1;

    /** The view that is controlled by this presenter */
    private IStationsContract.View view;

    /** All the stations of the network, without filters */
    private List<Station> stations = new ArrayList<>();

    /**
     * @see IStationsContract.Presenter#init(IStationsContract.View, List)
     * @param view the view to control
     * @param stations all the stations of the network
     */
    @Override
    public void init(IStationsContract.View view, List<Station> stations) {
        this.view = view;
        this.stations = stations == null ? new ArrayList<>() : stations;
        view.showStations(this.stations);
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

        List<Station> filtered = StationFilter.filterByMinFreeBikes(stations, minFreeBikes);
        if (filtered.isEmpty()) {
            view.showNoStationsWithMinFreeBikes(minFreeBikes);
        } else {
            view.showStations(filtered);
        }
    }

    //AÑADO
    /**
     * Quita los filtros mostrando otra vez todas las estaciones.
     * Si falla al hacerlo, pide a la vista que muestre un error.
     * Si no había ningún filtro aplicado, no pasa nada: se muestra la lista completa.
     * @see IStationsContract.Presenter#onFilterCleared()
     */
    @Override
    public void onFilterCleared() {
        try {
            view.showStations(stations);
        } catch (RuntimeException error) {
            view.mostrarErrorQuitarFiltros();
        }
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
}
