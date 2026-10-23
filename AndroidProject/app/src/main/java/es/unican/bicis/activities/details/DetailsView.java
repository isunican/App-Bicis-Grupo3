package es.unican.bicis.activities.details;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.IntentCompat;

import org.parceler.Parcels;

import es.unican.bicis.R;
import es.unican.bicis.activities.stations.StationsView;
import es.unican.bicis.model.Network;

/**
 * View that shows the details of one bike sharing network.
 * It follows the MVP pattern: all the logic is in {@link DetailsPresenter},
 * this view only displays the texts the presenter gives to it.
 */
public class DetailsView extends AppCompatActivity implements IDetailsContract.View {

    /**
     * Key for the intent that contains the network
     */
    public static final String INTENT_NETWORK = "INTENT_NETWORK";

    /**
     * The presenter of this view
     */
    private IDetailsContract.Presenter presenter;

    /**
     * View elements
     */
    private TextView tvName;
    private TextView tvCity;

    /**
     * @param savedInstanceState
     * @see AppCompatActivity#onCreate(Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_view);

        // The default theme does not include a toolbar.
        // In this app the toolbar is explicitly declared in the layout
        // Set this toolbar as the activity ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        assert bar != null;  // to avoid warning in the line below
        bar.setDisplayHomeAsUpEnabled(true);  // show back button in action bar

        // Link to view elements
        tvName = findViewById(R.id.tvName);
        tvCity = findViewById(R.id.tvCity);
        Button btnStations = findViewById(R.id.btnStations);
        btnStations.setOnClickListener(v -> presenter.onStationsClicked());

        // Get Network from the intent that triggered this activity
        Parcelable wrapped = IntentCompat.getParcelableExtra(getIntent(), INTENT_NETWORK, Parcelable.class);
        Network network = null;
        if (wrapped != null) {
            network = Parcels.unwrap(wrapped);
        }

        // The presenter decides what to show
        presenter = new DetailsPresenter();
        presenter.init(this, network);
    }

    /**
     * @param name the name of the network
     * @see IDetailsContract.View#showName(String)
     */
    @Override
    public void showName(String name) {
        tvName.setText(name);
    }

    /**
     * @param location the city and country of the network
     * @see IDetailsContract.View#showLocation(String)
     */
    @Override
    public void showLocation(String location) {
        tvCity.setText(location);
    }

    /**
     * @param latitude the latitude as text
     * @see IDetailsContract.View#showLatitude(String)
     */
    @Override
    public void showLatitude(String latitude) {
        // TODO: pending view element in layout (tvLatitude)
    }

    /**
     * @param longitude the longitude as text
     * @see IDetailsContract.View#showLongitude(String)
     */
    @Override
    public void showLongitude(String longitude) {
        // TODO: pending view element in layout (tvLongitude)
    }

    /**
     * @param ebikes "Sí", "No" or "-"
     * @see IDetailsContract.View#showEbikes(String)
     */
    @Override
    public void showEbikes(String ebikes) {
        // TODO: pending view element in layout (tvEbikes)
    }

    /**
     * @param count the number of companies as text
     * @see IDetailsContract.View#showOperatorsCount(String)
     */
    @Override
    public void showOperatorsCount(String count) {
        // TODO: pending view element in layout (tvOperatorsCount)
    }

    /**
     * @param operators the formatted list of companies
     * @see IDetailsContract.View#showOperators(String)
     */
    @Override
    public void showOperators(String operators) {
        // TODO: pending view element in layout (tvOperators)
    }

    /**
     * @see IDetailsContract.View#showNoDetailsAvailable()
     */
    @Override
    public void showNoDetailsAvailable() {
        // TODO: pending view element in layout (hide details and show tvNoDetails message)
    }

    /**
     * @param network the network whose stations must be shown
     * @see IDetailsContract.View#showStations(Network)
     */
    @Override
    public void showStations(Network network) {
        Intent intent = new Intent(this, StationsView.class);
        intent.putExtra(StationsView.INTENT_NETWORK, Parcels.wrap(network));
        startActivity(intent);
    }
}