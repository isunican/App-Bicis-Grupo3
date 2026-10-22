package es.unican.bicis.activities.stations;

import java.util.List;

import es.unican.bicis.common.StationUtils;
import es.unican.bicis.model.Location;
import es.unican.bicis.model.Network;
import es.unican.bicis.model.Station;
import es.unican.bicis.repository.INetworksRepository;
import es.unican.bicis.repository.StationsCallback;

/**
 * The presenter of the list of stations of a network. It controls {@link StationsView}
 */
public class StationsPresenter implements IStationsContract.Presenter {

    /** The view that is controlled by this presenter */
    private IStationsContract.View view;

    /** The location shown in the header, without the number of stations */
    private String location;

    /**
     * @see IStationsContract.Presenter#init(IStationsContract.View, Network)
     * @param view the view to control
     * @param network the network whose stations must be shown
     */
    @Override
    public void init(IStationsContract.View view, Network network) {
        this.view = view;

        if (network == null) {
            view.showLoadError();
            return;
        }

        // Header: always shown, even if the stations cannot be loaded
        location = formatLocation(network.getLocation());
        view.showNetworkName(StationUtils.formatText(network.getName()));
        view.showNetworkLocation(location);

        String id = network.getId();
        if (id == null || id.trim().isEmpty()) {
            view.showLoadError();
            return;
        }

        load(id);
    }

    /**
     * Requests the stations of the network to the repository, and sends them to the view
     * @param networkId id of the network
     */
    private void load(String networkId) {
        INetworksRepository repository = view.getNetworksRepository();

        StationsCallback callback = new StationsCallback() {

            @Override
            public void onSuccess(List<Station> stations) {
                showStations(stations);
            }

            @Override
            public void onFailure(Throwable e) {
                view.showLoadError();
            }
        };

        repository.requestStations(callback, networkId);
    }

    /**
     * Sends the stations to the view sorted by free bikes, or the empty-list message
     * if there are none. It also adds the number of stations to the header.
     * @param stations the stations returned by the repository
     */
    private void showStations(List<Station> stations) {
        if (stations == null || stations.isEmpty()) {
            view.showNoStations();
            return;
        }

        view.showStations(StationUtils.sortByFreeBikes(stations));
        view.showNetworkLocation(location + " · " + stations.size() + " estaciones");
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