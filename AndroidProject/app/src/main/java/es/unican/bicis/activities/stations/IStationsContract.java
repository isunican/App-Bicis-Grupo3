package es.unican.bicis.activities.stations;

import java.util.List;

import es.unican.bicis.model.Station;

/**
 * The Presenter-View contract for the Stations activity.
 * The Stations activity shows the stations of a bike sharing network,
 * and allows to filter them by a minimum number of free bikes.
 */
public interface IStationsContract {

    /**
     * Methods that must be implemented in the Stations Presenter.
     * Only the View should call these methods.
     */
    public interface Presenter {

        /**
         * Links the presenter with its view and the stations to display.
         * Only the View should call this method
         * @param view the view controlled by this presenter
         * @param stations all the stations of the network
         */
        public void init(View view, List<Station> stations);

        /**
         * The presenter is informed that the user wants to filter the stations
         * by a minimum number of free bikes.
         * Only the View should call this method
         * @param minFreeBikesText the text introduced by the user
         */
        public void onFilterApplied(String minFreeBikesText);
//AÑADO
        /**
         * El usuario quiere quitar los filtros aplicados.
         * Si se quitan, la vista vuelve a mostrar todas las estaciones.
         * Si falla, se pide a la vista que muestre un error
         * (ver {@link View#mostrarErrorQuitarFiltros()}).
         * Solo la vista debe llamar a este método
         */
        public void onFilterCleared();
//
    }

    /**
     * Methods that must be implemented in the Stations View.
     * Only the Presenter should call these methods.
     */
    public interface View {

        /**
         * The view is requested to display the given list of stations.
         * Only the Presenter should call this method
         * @param stations the stations to display
         */
        public void showStations(List<Station> stations);

        /**
         * The view is requested to display a message indicating that no station
         * has at least the given number of free bikes.
         * Only the Presenter should call this method
         * @param minFreeBikes the minimum number of free bikes requested
         */
        public void showNoStationsWithMinFreeBikes(int minFreeBikes);

        /**
         * The view is requested to display an error indicating that the introduced
         * value is not valid (it must be an integer greater than 0).
         * Only the Presenter should call this method
         */
        public void showInvalidMinFreeBikes();
//AÑADO
        /**
         * Se pide a la vista que avise de que no se han podido quitar los filtros.
         * La vista mantiene la lista y el filtro que ya se veían.
         * Solo el presenter debe llamar a este método
         */
        public void mostrarErrorQuitarFiltros();
//
    }
}
