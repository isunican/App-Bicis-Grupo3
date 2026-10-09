package es.unican.bicis.activities.details;

import android.os.Bundle;
import android.os.Parcelable;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.IntentCompat;

import org.parceler.Parcels;

import es.unican.bicis.R;
import es.unican.bicis.model.Network;

/**
 * View that shows the details of one bike sharing network.
 * It follows the MVP pattern: all the logic is in {@link DetailsPresenter},
 * this view only displays the texts the presenter gives to it.
 */
public class DetailsView extends AppCompatActivity implements IDetailsContract.View {

    /** Key for the intent that contains the network */
    public static final String INTENT_NETWORK = "INTENT_NETWORK";

    /** The presenter of this view */
    private IDetailsContract.Presenter presenter;

    /** View elements */
    private TextView tvName;
    private TextView tvCity;

    /**
     * @see AppCompatActivity#onCreate(Bundle)
     * @param savedInstanceState
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
     * @see IDetailsContract.View#showName(String)
     * @param name the name of the network
     */
    @Override
    public void showName(String name) {
        tvName.setText(name);
    }

    /**
     * @see IDetailsContract.View#showLocation(String)
     * @param location the city and country of the network
     */
    @Override
    public void showLocation(String location) {
        tvCity.setText(location);
    }

    /**
     * @see IDetailsContract.View#showLatitude(String)
     * @param latitude the latitude as text
     */
    @Override
    public void showLatitude(String latitude) {
        // TODO: pending view element in layout (tvLatitude)
    }

    /**
     * @see IDetailsContract.View#showLongitude(String)
     * @param longitude the longitude as text
     */
    @Override
    public void showLongitude(String longitude) {
        // TODO: pending view element in layout (tvLongitude)
    }

    /**
     * @see IDetailsContract.View#showEbikes(String)
     * @param ebikes "Sí", "No" or "-"
     */
    @Override
    public void showEbikes(String ebikes) {
        // TODO: pending view element in layout (tvEbikes)
    }

    /**
     * @see IDetailsContract.View#showOperatorsCount(String)
     * @param count the number of companies as text
     */
    @Override
    public void showOperatorsCount(String count) {
        // TODO: pending view element in layout (tvOperatorsCount)
    }

    /**
     * @see IDetailsContract.View#showOperators(String)
     * @param operators the formatted list of companies
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

}