package es.unican.bicis.activities.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import org.parceler.Parcels;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

import es.unican.bicis.R;
import es.unican.bicis.activities.info.InfoView;
import es.unican.bicis.activities.details.DetailsView;
import es.unican.bicis.location.ILocationProvider;
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
        // se muestra la ciudad, y si no la tiene el nombre de la red
        String ciudad = network.getLocation() != null && network.getLocation().getCity() != null
                ? network.getLocation().getCity()
                : network.getName();
        Toast.makeText(this, getString(R.string.cityDetected, ciudad), Toast.LENGTH_LONG).show();
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
}