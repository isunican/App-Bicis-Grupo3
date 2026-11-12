package es.unican.bicis.activities.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

import es.unican.bicis.R;
import es.unican.bicis.activities.info.InfoView;
import es.unican.bicis.activities.details.DetailsView;
import es.unican.bicis.activities.stations.StationsView;
import es.unican.bicis.location.ILocationProvider;
import es.unican.bicis.model.CitySelector;
import es.unican.bicis.model.Network;
import es.unican.bicis.repository.INetworksRepository;

/**
 * The main view of the application. It shows a list of bike sharing networks.
 */
@AndroidEntryPoint
public class MainView extends AppCompatActivity implements IMainContract.View {

    /** The presenter of this view */
    private IMainContract.Presenter presenter;

    /** The repository to access the data. This is automatically injected by Hilt in this class */
    @Inject
    INetworksRepository repository;

    /** Gives the location of the device. It is also injected by Hilt */
    @Inject
    ILocationProvider locationProvider;

    /**
     * ISO codes of the countries in the country selector, in the same order as its options.
     * The first one is null, the option "Todos" (HU 532301).
     */
    private final List<String> countryCodes = new ArrayList<>();

    /** ISO code of the country shown as selected in the country selector, or null if none */
    private String shownCountry;

    /** Name of the city shown as selected in the city selector, or null if none */
    private String shownCity;

    /**
     * Shows the Android dialog asking for the location permission and tells the presenter
     * the answer. It has to be registered here (before onCreate finishes), Android requires it.
     */
    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    this::onLocationPermissionAnswer);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // The default theme does not include a toolbar.
        // In this app the toolbar is explicitly declared in the layout
        // Set this toolbar as the activity ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // instantiate presenter and launch initial business logic
        presenter = new MainPresenter();
        presenter.init(this);
    }

    /**
     * This creates the menu that is shown in the action bar (the upper toolbar)
     * @param menu The options menu in which you place your items.
     *
     * @return true because we are defining a new menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * This is called when an item in the action bar menu is selected.
     * @param item The menu item that was selected.
     *
     * @return true if we have handled the selection
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menuItemInfo) {
            presenter.onMenuInfoClicked();
            return true;
        }
        if (itemId == R.id.menuItemSelectCity) {
            presenter.onSelectCityClicked();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * @see IMainContract.View#init()
     */
    @Override
    public void init() {
        // initialize on click listeners (when clicking on a network in the list)
        ListView list = findViewById(R.id.lvNetworks);
        list.setOnItemClickListener((parent, view, position, id) -> {
            Network network = (Network) parent.getItemAtPosition(position);
            presenter.onNetworkClicked(network);
        });

        // desplegables de pais y ciudad (HU 532301)
        Spinner spCountry = findViewById(R.id.spCountry);
        spCountry.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onCountryOptionSelected(position);
            }
        });

        Spinner spCity = findViewById(R.id.spCity);
        spCity.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onCityOptionSelected(parent, position);
            }
        });
    }

    /**
     * @see IMainContract.View#getNetworksRepository()
     * @return the repository to access the data
     */
    @Override
    public INetworksRepository getNetworksRepository() {
        return repository;
    }

    /**
     * @see IMainContract.View#showNetworks(List)
     * @param networks the list of networks
     */
    @Override
    public void showNetworks(List<Network> networks) {
        ListView list = findViewById(R.id.lvNetworks);
        NetworksArrayAdapter adapter = new NetworksArrayAdapter(this, networks);
        list.setAdapter(adapter);
    }

    /**
     * @see IMainContract.View#showLoadCorrect(int)
     * @param networks
     */
    @Override
    public void showLoadCorrect(int networks) {
        Toast.makeText(this, getString(R.string.loadCorrect, networks), Toast.LENGTH_SHORT).show();
    }

    /**
     * @see IMainContract.View#showLoadError()
     */
    @Override
    public void showLoadError() {
        Toast.makeText(this, R.string.loadError, Toast.LENGTH_SHORT).show();
    }

    /**
     * @see IMainContract.View#showNetworkDetails(Network)
     * @param network the network
     */

    @Override
    public void showNetworkDetails(Network network) {
        Intent intent = new Intent(this, DetailsView.class);
        intent.putExtra(DetailsView.INTENT_NETWORK, Parcels.wrap(network));
        startActivity(intent);
    }


    /**
     * @see IMainContract.View#showInfoActivity()
     */
    @Override
    public void showInfoActivity() {
        Intent intent = new Intent(this, InfoView.class);
        startActivity(intent);
    }

    /**
     * @see IMainContract.View#requestLocationPermission()
     */
    @Override
    public void requestLocationPermission() {
        // si ya lo dio otra vez no se le vuelve a preguntar
        if (hasLocationPermission()) {
            presenter.onLocationPermissionResult(true);
        } else {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION});
        }
    }

    /**
     * @see IMainContract.View#showLocationPermissionDenied()
     */
    @Override
    public void showLocationPermissionDenied() {
        Toast.makeText(this, R.string.locationPermissionDenied, Toast.LENGTH_LONG).show();
    }

    /**
     * @see IMainContract.View#showLocationUnavailable()
     */
    @Override
    public void showLocationUnavailable() {
        Toast.makeText(this, R.string.locationUnavailable, Toast.LENGTH_LONG).show();
    }

    /**
     * @see IMainContract.View#showNoNetworkNearby()
     */
    @Override
    public void showNoNetworkNearby() {
        Toast.makeText(this, R.string.noNetworkNearby, Toast.LENGTH_LONG).show();
    }

    /**
     * @see IMainContract.View#getLocationProvider()
     * @return the location provider
     */
    @Override
    public ILocationProvider getLocationProvider() {
        return locationProvider;
    }

    /**
     * @see IMainContract.View#showNetworkStations(Network)
     * @param network the network detected from the user's location
     */
    @Override
    public void showNetworkStations(Network network) {
        // si la pantalla ya se ha cerrado no se hace nada (la ubicacion puede llegar tarde)
        if (isFinishing() || isDestroyed()) {
            return;
        }

        // se muestra la ciudad, y si no la tiene el nombre de la red
        String ciudad = network.getLocation() != null && network.getLocation().getCity() != null
                ? network.getLocation().getCity()
                : network.getName();
        Toast.makeText(this, getString(R.string.cityDetected, ciudad), Toast.LENGTH_LONG).show();

        // se abre directamente la lista de estaciones de esa red
        Intent intent = new Intent(this, StationsView.class);
        intent.putExtra(StationsView.INTENT_NETWORK, Parcels.wrap(network));
        startActivity(intent);
    }

    /**
     * @see IMainContract.View#showCitySelector(List, String, String)
     * @param countries the ISO codes of the countries with networks
     * @param country the ISO code of the selected country, or null
     * @param city the name of the selected city, or null
     */
    @Override
    public void showCitySelector(List<String> countries, String country, String city) {
        findViewById(R.id.llCitySelector).setVisibility(View.VISIBLE);

        // la primera opcion es "Todos" (null) y despues los paises con su nombre en espanol
        countryCodes.clear();
        countryCodes.add(null);
        countryCodes.addAll(countries);
        List<String> names = new ArrayList<>();
        names.add(getString(R.string.cityAllCountries));
        for (String code : countries) {
            names.add(CitySelector.getCountryName(code));
        }

        // se guarda antes de cambiar el desplegable, para que su aviso no llegue al presenter
        shownCountry = country;
        shownCity = city;

        Spinner spCountry = findViewById(R.id.spCountry);
        spCountry.setAdapter(new HintAdapter(this, names, getString(R.string.citySelectHint)));
        spCountry.setEnabled(true);
        int position = countryCodes.indexOf(country);
        spCountry.setSelection(Math.max(position, 0), false);
    }

    /**
     * @see IMainContract.View#showCities(List, String)
     * @param cities the cities of the selected country
     * @param city the name of the selected city, or null
     */
    @Override
    public void showCities(List<String> cities, String city) {
        shownCity = city;
        Spinner spCity = findViewById(R.id.spCity);

        // sin pais elegido el desplegable de ciudades no tiene opciones
        if (cities.isEmpty()) {
            spCity.setAdapter(new HintAdapter(this, new ArrayList<>(),
                    getString(R.string.citySelectHint)));
            spCity.setEnabled(false);
            return;
        }

        // la primera opcion es "Todas" (null): se ven todas las redes del pais
        List<String> options = new ArrayList<>();
        options.add(getString(R.string.cityAllCities));
        options.addAll(cities);
        spCity.setAdapter(new HintAdapter(this, options, getString(R.string.citySelectHint)));
        spCity.setEnabled(true);
        spCity.setSelection(Math.max(indexOfIgnoreCase(cities, city) + 1, 0), false);
    }

    /**
     * @see IMainContract.View#showCitiesLoadError()
     */
    @Override
    public void showCitiesLoadError() {
        findViewById(R.id.llCitySelector).setVisibility(View.VISIBLE);

        // los dos desplegables se quedan sin ninguna opcion
        countryCodes.clear();
        shownCountry = null;
        shownCity = null;
        String hint = getString(R.string.citySelectHint);

        Spinner spCountry = findViewById(R.id.spCountry);
        spCountry.setAdapter(new HintAdapter(this, new ArrayList<>(), hint));
        spCountry.setEnabled(false);

        Spinner spCity = findViewById(R.id.spCity);
        spCity.setAdapter(new HintAdapter(this, new ArrayList<>(), hint));
        spCity.setEnabled(false);

        Toast.makeText(this, R.string.citiesLoadError, Toast.LENGTH_LONG).show();
    }

    /**
     * @see IMainContract.View#showChooseCityHint()
     */
    @Override
    public void showChooseCityHint() {
        Toast.makeText(this, R.string.chooseCityHint, Toast.LENGTH_LONG).show();
    }

    /**
     * Called when an option of the country selector is selected. The presenter is only informed
     * if the country is different from the one already shown: Android also calls the listener
     * when the options are changed from the code, and that must not reach the presenter.
     * @param position the position of the option (0 is "Todos")
     */
    private void onCountryOptionSelected(int position) {
        if (position < 0 || position >= countryCodes.size()) {
            return;
        }
        String country = countryCodes.get(position);
        if (Objects.equals(country, shownCountry)) {
            return;
        }
        shownCountry = country;
        shownCity = null;
        presenter.onCountrySelected(country);
    }

    /**
     * Called when an option of the city selector is selected. As with the countries, the
     * presenter is only informed if the city is different from the one already shown.
     * @param parent the city selector
     * @param position the position of the option (0 is "Todas", that is, no city)
     */
    private void onCityOptionSelected(AdapterView<?> parent, int position) {
        String city = position <= 0 ? null : (String) parent.getItemAtPosition(position);
        if (city == null ? shownCity == null : city.equalsIgnoreCase(shownCity)) {
            return;
        }
        shownCity = city;
        presenter.onCitySelected(city);
    }

    /**
     * Looks for a text in a list, ignoring case.
     * @param list the list of texts
     * @param text the text to look for (can be null)
     * @return the position of the text in the list, or -1 if it is not in the list or is null
     */
    private static int indexOfIgnoreCase(List<String> list, String text) {
        if (text != null) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).equalsIgnoreCase(text)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Checks if the user has already given the location permission (approximate is enough).
     * @return true if the permission is granted
     */
    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Called when the user answers the permission dialog.
     * @param answers for each permission asked, true if the user allowed it
     */
    private void onLocationPermissionAnswer(Map<String, Boolean> answers) {
        // vale con que haya dado la aproximada o la precisa
        boolean granted = Boolean.TRUE.equals(answers.get(Manifest.permission.ACCESS_COARSE_LOCATION))
                || Boolean.TRUE.equals(answers.get(Manifest.permission.ACCESS_FINE_LOCATION));
        presenter.onLocationPermissionResult(granted);
    }

    /**
     * Listener of the selectors that only needs to know when an option is selected.
     */
    private abstract static class SimpleItemSelectedListener
            implements AdapterView.OnItemSelectedListener {

        /**
         * Nothing to do: the selectors always have an option selected when they have options.
         * @param parent the selector
         */
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // no se usa
        }
    }

    /**
     * Adapter of the country and city selectors. The first option ("Todos" or "Todas") means
     * that nothing is selected: in the open list it shows its own text, but in the closed
     * selector it shows the hint "Selecciona…", as in the mock-up.
     */
    private static class HintAdapter extends ArrayAdapter<String> {

        /** Text shown in the closed selector when the first option is selected */
        private final String hint;

        /**
         * Creates the adapter.
         * @param context the context
         * @param options the options of the selector
         * @param hint the text shown when the first option is selected
         */
        HintAdapter(Context context, List<String> options, String hint) {
            super(context, android.R.layout.simple_spinner_item, options);
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            this.hint = hint;
        }

        /**
         * Returns the view of the closed selector.
         * @param position the position of the selected option
         * @param convertView the old view to reuse, if possible
         * @param parent the selector
         * @return the view, with the hint (and lighter) if the first option is selected
         */
        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            TextView view = (TextView) super.getView(position, convertView, parent);
            if (position == 0) {
                view.setText(hint);
                view.setAlpha(0.5f);
            } else {
                view.setAlpha(1f);
            }
            return view;
        }
    }
}