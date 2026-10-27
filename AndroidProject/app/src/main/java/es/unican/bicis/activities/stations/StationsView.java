package es.unican.bicis.activities.stations;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.database.MatrixCursor;
import android.provider.BaseColumns;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.IntentCompat;
import androidx.appcompat.widget.SearchView;
import androidx.cursoradapter.widget.SimpleCursorAdapter;

import org.parceler.Parcels;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import es.unican.bicis.R;
import es.unican.bicis.activities.details.DetailsView;
import es.unican.bicis.model.Network;
import es.unican.bicis.model.Station;
import es.unican.bicis.repository.INetworksRepository;

/**
 * View that displays the stations of a bike sharing network.
 * It follows the MVP pattern: all the logic is in {@link StationsPresenter},
 * this view only displays what the presenter tells it and forwards the user actions.
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

    /** Filter dialog currently open (null if it is closed) and its input field */
    private AlertDialog filterDialog;
    private EditText filterInput;

    /** Adapter used to display station name suggestions. */
    private SimpleCursorAdapter suggestionsAdapter;

    /** Search field displayed in the toolbar. */
    private SearchView stationSearchView;

    /**
     * @see AppCompatActivity#onCreate(Bundle)
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stations);

        // The toolbar is explicitly declared in the layout: set it as the activity ActionBar
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
        Network network = wrapped == null ? null : Parcels.unwrap(wrapped);

        presenter = new StationsPresenter();
        presenter.init(this, network);
    }

    /**
     * Creates the stations toolbar menu and prepares the search suggestions.
     *
     * @param menu the options menu
     * @return true if the menu has been created
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_stations, menu);

        MenuItem searchItem = menu.findItem(R.id.menuItemSearch);

        if (searchItem != null) {
            stationSearchView = (SearchView) searchItem.getActionView();

            if (stationSearchView != null) {
                stationSearchView.setQueryHint(getString(R.string.menuSearch));
                stationSearchView.setIconifiedByDefault(true);

                String[] from = {"station_name"};
                int[] to = {android.R.id.text1};

                suggestionsAdapter = new SimpleCursorAdapter(
                        this,
                        android.R.layout.simple_list_item_1,
                        null,
                        from,
                        to,
                        0
                );

                stationSearchView.setSuggestionsAdapter(suggestionsAdapter);

                stationSearchView.setOnSuggestionListener(
                        new SearchView.OnSuggestionListener() {
                            @Override
                            public boolean onSuggestionSelect(int position) {
                                return selectSuggestion(position);
                            }

                            @Override
                            public boolean onSuggestionClick(int position) {
                                return selectSuggestion(position);
                            }
                        }
                );
            }
        }

        return true;
    }

    /**
     * Displays the station names provided as search suggestions.
     * This method will be connected to the Presenter once the
     * search contract is available.
     *
     * @param stationNames names of the suggested stations
     */
    private void updateSearchSuggestions(List<String> stationNames) {
        if (suggestionsAdapter == null) {
            return;
        }

        MatrixCursor cursor = new MatrixCursor(
                new String[]{BaseColumns._ID, "station_name"}
        );

        if (stationNames != null) {
            for (int i = 0; i < stationNames.size(); i++) {
                cursor.addRow(new Object[]{i, stationNames.get(i)});
            }
        }

        suggestionsAdapter.changeCursor(cursor);
    }

    /**
     * Places the selected station name into the search field.
     *
     * @param position position of the selected suggestion
     * @return true if the selection was handled
     */
    private boolean selectSuggestion(int position) {
        if (suggestionsAdapter == null || stationSearchView == null) {
            return false;
        }

        android.database.Cursor cursor = suggestionsAdapter.getCursor();

        if (cursor == null || !cursor.moveToPosition(position)) {
            return false;
        }

        int column = cursor.getColumnIndex("station_name");

        if (column < 0) {
            return false;
        }

        String stationName = cursor.getString(column);
        stationSearchView.setQuery(stationName, false);

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
        if (itemId == R.id.menuItemFilter) {
            openFilterDialog();
            return true;
        }
        if (itemId == R.id.menuItemNetworkInfo) {
            presenter.onInfoClicked();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Opens the dialog where the user introduces the minimum number of free bikes.
     * The dialog is only closed by the presenter when the value is valid,
     * so that the error can be shown inside it.
     */
    private void openFilterDialog() {
        filterInput = new EditText(this);
        filterInput.setId(R.id.etMinFreeBikes);
        filterInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        filterInput.setHint(R.string.filterHint);

        FrameLayout container = new FrameLayout(this);
        int margin = (int) (24 * getResources().getDisplayMetrics().density);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(margin, margin / 2, margin, 0);
        container.addView(filterInput, params);

        filterDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.filterTitle)
                .setView(container)
                .setPositiveButton(R.string.filterApply, null)
                .setNeutralButton(R.string.filterClear, (dialog, which) -> presenter.onFilterCleared())
                .setNegativeButton(R.string.filterCancel, null)
                .create();

        // The positive button is overridden so that the dialog does not close on invalid values
        filterDialog.setOnShowListener(d -> {
            Button apply = filterDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            apply.setOnClickListener(v -> presenter.onFilterApplied(filterInput.getText().toString()));
        });
        filterDialog.setOnDismissListener(d -> {
            filterDialog = null;
            filterInput = null;
        });
        filterDialog.show();
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
     * @param location the location text of the network
     */
    @Override
    public void showNetworkLocation(String location) {
        tvNetworkLocation.setText(location);
    }

    /**
     * @see IStationsContract.View#showStations(List)
     * @param stations the stations to display
     */
    @Override
    public void showStations(List<Station> stations) {
        lvStations.setAdapter(new StationsArrayAdapter(this, stations));
        lvStations.setVisibility(View.VISIBLE);
        stationsHeader.setVisibility(View.VISIBLE);
        tvStationsMessage.setVisibility(View.GONE);
    }

    /**
     * @see IStationsContract.View#showNoStations()
     */
    @Override
    public void showNoStations() {
        showMessage(getString(R.string.stationsEmpty));
    }

    /**
     * @see IStationsContract.View#showLoadError()
     */
    @Override
    public void showLoadError() {
        showMessage(getString(R.string.stationsLoadError));
    }

    /**
     * @see IStationsContract.View#showNoStationsWithMinFreeBikes(int)
     * @param minFreeBikes the minimum number of free bikes requested
     */
    @Override
    public void showNoStationsWithMinFreeBikes(int minFreeBikes) {
        showMessage(getString(R.string.filterNoResults, minFreeBikes));
    }


    /**
     * @see IStationsContract.View#mostrarErrorQuitarFiltros()
     */
    @Override
    public void mostrarErrorQuitarFiltros() {
        showMessage(getString(R.string.clearFilterError));
    }

    /**
     * @see IStationsContract.View#showInvalidMinFreeBikes()
     */
    @Override
    public void showInvalidMinFreeBikes() {
        if (filterInput != null) {
            filterInput.setError(getString(R.string.filterInvalid));
        }
    }

    /**
     * @see IStationsContract.View#showFilterActive(int)
     * @param minFreeBikes the minimum number of free bikes of the active filter
     */
    @Override
    public void showFilterActive(int minFreeBikes) {
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setSubtitle(getString(R.string.filterActive, minFreeBikes));
        }
        if (filterDialog != null) {
            filterDialog.dismiss();
        }
    }

    /**
     * @see IStationsContract.View#showFilterInactive()
     */
    @Override
    public void showFilterInactive() {
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setSubtitle(null);
        }
    }

    /**
     * @see IStationsContract.View#showNetworkDetails(Network)
     * @param network the network
     */
    @Override
    public void showNetworkDetails(Network network) {
        Intent intent = new Intent(this, DetailsView.class);
        intent.putExtra(DetailsView.INTENT_NETWORK, Parcels.wrap(network));
        startActivity(intent);
    }

    /**
     * Displays a message instead of the station list.
     * @param message the message to display
     */
    private void showMessage(String message) {
        lvStations.setVisibility(View.GONE);
        stationsHeader.setVisibility(View.GONE);
        tvStationsMessage.setText(message);
        tvStationsMessage.setVisibility(View.VISIBLE);
    }

    /**
     * Closes this activity when the back button of the action bar is pressed.
     * @return true because the navigation has been handled
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}
