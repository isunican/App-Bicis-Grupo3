package es.unican.bicis.activities.stations;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.IntentCompat;

import org.parceler.Parcels;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

import es.unican.bicis.R;
import es.unican.bicis.model.Network;
import es.unican.bicis.model.Station;
import es.unican.bicis.repository.INetworksRepository;

/**
 * Displays the stations of a bike sharing network.
 * It follows the MVP pattern: all the logic is in {@link StationsPresenter},
 * this view only displays what the presenter gives to it.
 */
@AndroidEntryPoint
public class StationsView extends AppCompatActivity implements IStationsContract.View {

    /** Key for the intent that contains the network */
    public static final String INTENT_NETWORK = "INTENT_NETWORK";

    /** The repository to access the data. This is automatically injected by Hilt in this class */
    @Inject
    INetworksRepository repository;

    /** The presenter of this view */
    private IStationsContract.Presenter presenter;

    /** View elements */
    private TextView tvNetworkName;
    private TextView tvNetworkLocation;
    private TextView tvStationsMessage;
    private ListView lvStations;
    private View stationsHeader;

    /**
     * @see AppCompatActivity#onCreate(Bundle)
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stations);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);  // show back button in action bar
        }

        // Link to view elements
        tvNetworkName = findViewById(R.id.tvNetworkName);
        tvNetworkLocation = findViewById(R.id.tvNetworkLocation);
        tvStationsMessage = findViewById(R.id.tvStationsMessage);
        lvStations = findViewById(R.id.lvStations);
        stationsHeader = findViewById(R.id.stationsHeader);

        // Get Network from the intent that triggered this activity
        Parcelable wrapped = IntentCompat.getParcelableExtra(getIntent(), INTENT_NETWORK, Parcelable.class);
        Network network = null;
        if (wrapped != null) {
            network = Parcels.unwrap(wrapped);
        }

        // The presenter decides what to show
        presenter = new StationsPresenter();
        presenter.init(this, network);
    }

    /**
     * @see IStationsContract.View#getNetworksRepository()
     * @return the repository to access the data
     */
    @Override
    public INetworksRepository getNetworksRepository() {
        return repository;
    }

    /**
     * @see IStationsContract.View#showNetworkName(String)
     * @param name the name of the network
     */
    @Override
    public void showNetworkName(String name) {
        tvNetworkName.setText(name);
    }

    /**
     * @see IStationsContract.View#showNetworkLocation(String)
     * @param location the text shown below the name of the network
     */
    @Override
    public void showNetworkLocation(String location) {
        tvNetworkLocation.setText(location);
    }

    /**
     * @see IStationsContract.View#showStations(List)
     * @param stations the stations already sorted
     */
    @Override
    public void showStations(List<Station> stations) {
        StationsArrayAdapter adapter = new StationsArrayAdapter(this, stations);
        lvStations.setAdapter(adapter);
        lvStations.setVisibility(View.VISIBLE);
        stationsHeader.setVisibility(View.VISIBLE);
        tvStationsMessage.setVisibility(View.GONE);
    }

    /**
     * @see IStationsContract.View#showNoStations()
     */
    @Override
    public void showNoStations() {
        showMessage("Esta red no tiene estaciones disponibles");
    }

    /**
     * @see IStationsContract.View#showLoadError()
     */
    @Override
    public void showLoadError() {
        showMessage("No se han podido cargar las estaciones\n" +
                "Comprueba tu conexión a internet");
    }

    /**
     * Hides the list and displays a message instead.
     * @param message the message to show
     */
    private void showMessage(String message) {
        lvStations.setVisibility(View.GONE);
        stationsHeader.setVisibility(View.GONE);
        tvStationsMessage.setText(message);
        tvStationsMessage.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
