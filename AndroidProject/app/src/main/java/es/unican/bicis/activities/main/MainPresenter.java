package es.unican.bicis.activities.main;

import java.util.List;
import java.util.Locale;

import es.unican.bicis.location.LocationResultCallback;
import es.unican.bicis.model.CitySelector;
import es.unican.bicis.model.Location;
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

    /**
     * The networks loaded from the repository (needed to find the user's city and to fill the
     * country and city selectors). It is null while they are not loaded or if the load failed.
     */
    private List<Network> networks;

    /**
     * The network detected from the location of the user (HU 532319), used to preselect its
     * country and city when the user opens the city selector. Null if it was not detected.
     */
    private Network detectedNetwork;

    /** ISO code of the country selected in the city selector, or null if none ("Todos") */
    private String selectedCountry;

    /** Name of the city selected in the city selector, or null if none */
    private String selectedCity;

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
                    // se guarda para preseleccionar su pais y su ciudad (HU 532301, Prueba 4)
                    detectedNetwork = red;
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
     * @see IMainContract.Presenter#onSelectCityClicked()
     */
    @Override
    public void onSelectCityClicked() {
        // sin redes no hay paises ni ciudades que mostrar
        if (networks == null) {
            view.showCitiesLoadError();
            return;
        }

        // si el usuario no habia elegido nada todavia, se parte de la ciudad detectada
        if (selectedCountry == null && hasCountryAndCity(detectedNetwork)) {
            selectedCountry = detectedNetwork.getLocation().getCountry().trim()
                    .toUpperCase(Locale.ROOT);
            selectedCity = detectedNetwork.getLocation().getCity().trim();
        }

        view.showCitySelector(CitySelector.getCountries(networks), selectedCountry, selectedCity);
        view.showCities(CitySelector.getCities(networks, selectedCountry), selectedCity);

        if (selectedCountry == null) {
            // no se sabe la ciudad: se le pide que la elija y la lista no cambia
            view.showChooseCityHint();
        } else {
            view.showNetworks(CitySelector.filterByCity(networks, selectedCountry, selectedCity));
        }
    }

    /**
     * @see IMainContract.Presenter#onCountrySelected(String)
     * @param countryCode the ISO code of the selected country, or null for "Todos"
     */
    @Override
    public void onCountrySelected(String countryCode) {
        if (networks == null) {
            view.showCitiesLoadError();
            return;
        }

        // al cambiar de pais la ciudad anterior deja de estar seleccionada
        selectedCountry = countryCode;
        selectedCity = null;

        view.showCities(CitySelector.getCities(networks, countryCode), null);
        view.showNetworks(CitySelector.filterByCity(networks, countryCode, null));
    }

    /**
     * @see IMainContract.Presenter#onCitySelected(String)
     * @param city the name of the selected city, or null if no city is selected
     */
    @Override
    public void onCitySelected(String city) {
        if (networks == null) {
            view.showCitiesLoadError();
            return;
        }

        selectedCity = city;
        view.showNetworks(CitySelector.filterByCity(networks, selectedCountry, city));
    }

    /**
     * Checks whether a network reports both its country and its city.
     * @param network the network to check (can be null)
     * @return true if the network is not null and its country and city are not null nor blank
     */
    private static boolean hasCountryAndCity(Network network) {
        if (network == null || network.getLocation() == null) {
            return false;
        }
        Location location = network.getLocation();
        return location.getCountry() != null && !location.getCountry().trim().isEmpty()
                && location.getCity() != null && !location.getCity().trim().isEmpty();
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
