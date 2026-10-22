
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
import es.unican.bicis.common.StationUtils;
import es.unican.bicis.model.Location;
import es.unican.bicis.model.Network;
import es.unican.bicis.model.Station;
import es.unican.bicis.repository.INetworksRepository;
import es.unican.bicis.repository.StationsCallback;

/**
 * Displays the stations of a bike sharing network.
 * Handles successful loading, empty lists and loading errors.
 */
@AndroidEntryPoint
public class StationsView extends AppCompatActivity {

    public static final String INTENT_NETWORK = "INTENT_NETWORK";

    @Inject
    INetworksRepository repository;

    private TextView tvNetworkName;
    private TextView tvNetworkLocation;
    private TextView tvStationsMessage;
    private ListView lvStations;
    private View stationsHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stations);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
        }

        tvNetworkName = findViewById(R.id.tvNetworkName);
        tvNetworkLocation = findViewById(R.id.tvNetworkLocation);
        tvStationsMessage = findViewById(R.id.tvStationsMessage);
        lvStations = findViewById(R.id.lvStations);
        stationsHeader = findViewById(R.id.stationsHeader);

        Parcelable wrapped = IntentCompat.getParcelableExtra(
                getIntent(), INTENT_NETWORK, Parcelable.class);

        Network network = wrapped == null
                ? null
                : Parcels.unwrap(wrapped);

        if (network == null) {
            showLoadError();
            return;
        }

        tvNetworkName.setText(
                StationUtils.formatText(network.getName()));

        tvNetworkLocation.setText(
                formatLocation(network.getLocation()));

        if (network.getId() == null ||
                network.getId().trim().isEmpty()) {
            showLoadError();
            return;
        }

        loadStations(network.getId());
    }

    /**
     * Requests the stations asynchronously.
     */
    /**private void loadStations(String networkId) {
        repository.requestStations(new StationsCallback() {

            @Override
            public void onSuccess(List<Station> stations) {
                runOnUiThread(() -> showStations(stations));
            }

            @Override
            public void onFailure(Throwable error) {
                runOnUiThread(() -> showLoadError());
            }

        }, networkId);
    }**/


    private void loadStations(String networkId) {

        // Comportamiento provisional:
        // Solo se muestran estaciones de TUeBICI (Santander).
        // Las demás redes quedan sin información de estaciones.

        if (!"tuebici".equalsIgnoreCase(networkId)) {
            showStations(new java.util.ArrayList<Station>());
            return;
        }

        repository.requestStations(new StationsCallback() {

            @Override
            public void onSuccess(List<Station> stations) {
                runOnUiThread(() -> showStations(stations));
            }

            @Override
            public void onFailure(Throwable error) {
                runOnUiThread(() -> showLoadError());
            }

        }, networkId);
    }



    /**
     * Displays the stations or the empty-list message.
     */
    private void showStations(List<Station> stations) {
        if (stations == null || stations.isEmpty()) {
            showMessage(
                    "Esta red no tiene estaciones disponibles");
            return;
        }

        List<Station> sorted =
                StationUtils.sortByFreeBikes(stations);

        StationsArrayAdapter adapter =
                new StationsArrayAdapter(this, sorted);

        lvStations.setAdapter(adapter);
        lvStations.setVisibility(View.VISIBLE);
        stationsHeader.setVisibility(View.VISIBLE);
        tvStationsMessage.setVisibility(View.GONE);

        String location = formatLocationFromHeader();
        tvNetworkLocation.setText(
                location + " · " + stations.size() + " estaciones");
    }

    /**
     * Displays an error when stations cannot be loaded.
     */
    private void showLoadError() {
        showMessage(
                "No se han podido cargar las estaciones\n" +
                        "Comprueba tu conexión a internet");
    }

    /**
     * Displays a message instead of the station list.
     */
    private void showMessage(String message) {
        lvStations.setVisibility(View.GONE);
        stationsHeader.setVisibility(View.GONE);
        tvStationsMessage.setText(message);
        tvStationsMessage.setVisibility(View.VISIBLE);
    }

    /**
     * Formats the city and country.
     */
    private String formatLocation(Location location) {
        if (location == null) {
            return StationUtils.NO_DATA;
        }

        String city =
                StationUtils.formatText(location.getCity());
        String country =
                StationUtils.formatText(location.getCountry());

        if (city.equals(StationUtils.NO_DATA)) {
            return country;
        }

        if (country.equals(StationUtils.NO_DATA)) {
            return city;
        }

        return city + ", " + country;
    }

    /**
     * Returns the location already displayed in the header.
     */
    private String formatLocationFromHeader() {
        return tvNetworkLocation.getText().toString();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

