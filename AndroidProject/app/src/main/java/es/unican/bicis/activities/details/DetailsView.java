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
import es.unican.bicis.model.Location;
import es.unican.bicis.model.Network;

/**
 * View that shows the details of one bike sharing network. Since this view does not have business logic,
 * it can be implemented as an activity directly, without the MVP pattern.
 */
public class DetailsView extends AppCompatActivity {

    /** Key for the intent that contains the network */
    public static final String INTENT_NETWORK = "INTENT_NETWORK";

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
        TextView tvName = findViewById(R.id.tvName);
        TextView tvCity = findViewById(R.id.tvCity);

        // Get Network from the intent that triggered this activity
        Parcelable wrapped = IntentCompat.getParcelableExtra(getIntent(), INTENT_NETWORK, Parcelable.class);
        Network network = null;
        if (wrapped != null) {
            network = Parcels.unwrap(wrapped);
        }

        // Set Texts
        if (network != null) {
            tvName.setText(network.getName());
            Location location = network.getLocation();
            if (location != null) {
                tvCity.setText(location.getCity());
            }
        }
    }
}