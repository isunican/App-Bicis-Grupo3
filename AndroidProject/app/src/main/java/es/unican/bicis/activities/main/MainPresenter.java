package es.unican.bicis.activities.main;

import java.util.List;

import es.unican.bicis.location.LocationResultCallback;
import es.unican.bicis.model.Network;
import es.unican.bicis.model.NetworkLocator;
import es.unican.bicis.repository.INetworksRepository;
import es.unican.bicis.repository.NetworksCallback;

/**
 * The presenter of the main activity of the application. It controls {@link MainView}
 */
public class MainPresenter implements IMainContract.Presenter {

    /** The view that is controlled by this presenter */
    private IMainContract.View view;

    /** The networks loaded from the repository (needed to find the user's city) */
    private List<Network> networks;

    /**
     * @see IMainContract.Presenter#init(IMainContract.View)
     * @param view the view to control
     */
    @Override
    public void init(IMainContract.View view) {
        this.view = view;
        this.view.init();
        load();
    }

    /**
     * @see IMainContract.Presenter#onNetworkClicked(Network)
     * @param network the network that has been clicked
     */
    @Override
    public void onNetworkClicked(Network network) {
        view.showNetworkDetails(network);
    }

    /**
     * @see IMainContract.Presenter#onMenuInfoClicked()
     */
    @Override
    public void onMenuInfoClicked() {
        view.showInfoActivity();
    }

    /**
     * @see IMainContract.Presenter#onLocationPermissionResult(boolean)
     * @param granted true if the user has given the location permission, false otherwise
     */
    @Override
    public void onLocationPermissionResult(boolean granted) {
        // sin permiso no podemos saber donde esta, se queda en la lista de redes
        if (!granted) {
            view.showLocationPermissionDenied();
            return;
        }

        view.getLocationProvider().requestCurrentLocation(new LocationResultCallback() {

            @Override
            public void onSuccess(double latitude, double longitude) {
                Network red = NetworkLocator.findNearestNetwork(networks, latitude, longitude,
                        NetworkLocator.MAX_DISTANCE_KM);
                if (red != null) {
                    view.showNetworkStations(red);
                } else {
                    view.showNoNetworkNearby();
                }
            }

            @Override
            public void onFailure(Throwable e) {
                view.showLocationUnavailable();
            }
        });
    }

    /**
     * Loads the bike networks from the repository, and sends them to the view.
     * If they are loaded correctly, the location permission is requested to detect the city.
     */
    private void load() {
        INetworksRepository repository = view.getNetworksRepository();

        NetworksCallback callBack = new NetworksCallback() {

            @Override
            public void onSuccess(List<Network> loadedNetworks) {
                networks = loadedNetworks;
                view.showNetworks(loadedNetworks);
                view.showLoadCorrect(loadedNetworks.size());
                // ya tenemos las redes, ahora pedimos el permiso para detectar la ciudad
                view.requestLocationPermission();
            }

            @Override
            public void onFailure(Throwable e) {
                // si no hay redes no tiene sentido buscar la ciudad
                view.showLoadError();
            }
        };

        repository.requestNetworks(callBack);
    }
}