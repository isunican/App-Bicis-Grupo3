package es.unican.bicis.activities.stations;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.IntentCompat;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;

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
    private Toolbar toolbar;

    /** Filter dialog currently open (null if it is closed) and its input fields */
    private AlertDialog filterDialog;
    private EditText filterInput;
    private EditText emptySlotsInput;

    /** Whether the presenter has reported an invalid value while applying the filters */
    private boolean invalidValueShown = false;

    /** Texts of the last filters applied, to fill the dialog when it is opened again */
    private String appliedFreeBikesText = "";
    private String appliedEmptySlotsText = "";

    /** Parts of the subtitle that describe the active filters (null if that filter is not active) */
    private String freeBikesSubtitle;
    private String emptySlotsSubtitle;

    /** Number of active filters, shown over the filter icon */
    private int activeFilters = 0;
    private BadgeDrawable filterBadge;

    /**
     * @see AppCompatActivity#onCreate(Bundle)
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stations);

        // The toolbar is explicitly declared in the layout: set it as the activity ActionBar
        toolbar = findViewById(R.id.toolbar);
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
     * This creates the menu that is shown in the action bar (the upper toolbar)
     * @param menu The options menu in which you place your items.
     *
     * @return true because we are defining a new menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_stations, menu);
        // The menu is created after the presenter has loaded the stations: show the badge now
        toolbar.post(this::updateFilterBadge);
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
     * Opens the dialog with all the station filters (free bikes and empty slots).
     * The fields show the values of the filters that are active.
     * The dialog is only closed when all the values are valid, so that the
     * errors can be shown inside it. "Quitar filtros" is only enabled if
     * there is any filter active.
     */
    private void openFilterDialog() {
        View content = getLayoutInflater().inflate(R.layout.dialog_filters, null);
        filterInput = content.findViewById(R.id.etMinFreeBikes);
        emptySlotsInput = content.findViewById(R.id.etMinEmptySlots);
        filterInput.setText(appliedFreeBikesText);
        emptySlotsInput.setText(appliedEmptySlotsText);

        filterDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.filterTitle)
                .setView(content)
                .setPositiveButton(R.string.filterApply, null)
                .setNeutralButton(
                        R.string.filterClear,
                        (dialog, which) -> presenter.onFilterCleared()
                )
                .setNegativeButton(R.string.filterCancel, null)
                .create();

        // The positive button is overridden so that the dialog does not close on invalid values
        filterDialog.setOnShowListener(d -> {
            Button apply = filterDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            apply.setOnClickListener(v -> onApplyFiltersClicked());

            Button clear = filterDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
            clear.setEnabled(isAnyFilterActive());
        });

        filterDialog.setOnDismissListener(d -> {
            filterDialog = null;
            filterInput = null;
            emptySlotsInput = null;
        });

        filterDialog.show();
    }

    /**
     * Sends to the presenter the filters introduced in the dialog.
     * An empty field means that the corresponding filter is not applied.
     * If every field is empty, the free bikes filter is sent so that the
     * presenter reports the invalid value (behaviour of HU 532376).
     * The dialog is closed only if the presenter has not reported any invalid value.
     */
    private void onApplyFiltersClicked() {
        String freeBikesText = filterInput.getText().toString().trim();
        String emptySlotsText = emptySlotsInput.getText().toString().trim();
        invalidValueShown = false;

        if (!freeBikesText.isEmpty() || emptySlotsText.isEmpty()) {
            presenter.onFilterApplied(freeBikesText);
        }
        if (!invalidValueShown && !emptySlotsText.isEmpty()) {
            presenter.onMinEmptySlotsFilterApplied(emptySlotsText);
        }
        if (!invalidValueShown && filterDialog != null) {
            appliedFreeBikesText = freeBikesText;
            appliedEmptySlotsText = emptySlotsText;
            filterDialog.dismiss();
        }
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
     * @see IStationsContract.View#showNoStationsWithMinEmptySlots(int)
     * @param minEmptySlots the minimum number of empty slots requested
     */
    @Override
    public void showNoStationsWithMinEmptySlots(int minEmptySlots) {
        showMessage(getString(R.string.filterEmptySlotsNoResults));
    }

    /**
     * @see IStationsContract.View#showInvalidMinEmptySlots()
     */
    @Override
    public void showInvalidMinEmptySlots() {
        invalidValueShown = true;
        if (emptySlotsInput != null) {
            emptySlotsInput.setError(getString(R.string.filterEmptySlotsInvalid));
        }
    }

    /**
     * @see IStationsContract.View#showActiveFiltersCount(int)
     * @param numActiveFilters the number of active filters
     */
    @Override
    public void showActiveFiltersCount(int numActiveFilters) {
        activeFilters = Math.max(numActiveFilters, 0);
        updateFilterBadge();
    }

    /**
     * @see IStationsContract.View#showMinEmptySlotsFilterActive(int)
     * @param minEmptySlots the minimum number of empty slots of the active filter
     */
    @Override
    public void showMinEmptySlotsFilterActive(int minEmptySlots) {
        emptySlotsSubtitle = minEmptySlots > 0
                ? getString(R.string.filterEmptySlotsActive, minEmptySlots)
                : null;
        updateSubtitle();
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
        invalidValueShown = true;
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
        freeBikesSubtitle = getString(R.string.filterActive, minFreeBikes);
        updateSubtitle();
    }

    /**
     * @see IStationsContract.View#showFilterInactive()
     */
    @Override
    public void showFilterInactive() {
        freeBikesSubtitle = null;
        emptySlotsSubtitle = null;
        appliedFreeBikesText = "";
        appliedEmptySlotsText = "";
        updateSubtitle();
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
     * Indicates whether any filter is active, to enable the "Quitar filtros" button.
     * @return true if there is at least one filter active
     */
    /**
     * Indicates whether any filter is active, to enable the "Quitar filtros" button.
     * A filter is active if the presenter has reported it (number over the filter icon)
     * or if its text is shown in the subtitle of the action bar.
     * @return true if there is at least one filter active
     */
    private boolean isAnyFilterActive() {
        return activeFilters > 0 || freeBikesSubtitle != null || emptySlotsSubtitle != null;
    }

    /**
     * Shows in the subtitle of the action bar the active filters, separated by " · ",
     * or removes the subtitle if no filter is active.
     */
    private void updateSubtitle() {
        StringBuilder subtitle = new StringBuilder();
        for (String part : new String[] {freeBikesSubtitle, emptySlotsSubtitle}) {
            if (part != null) {
                if (subtitle.length() > 0) {
                    subtitle.append(getString(R.string.filterSubtitleSeparator));
                }
                subtitle.append(part);
            }
        }
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setSubtitle(subtitle.length() > 0 ? subtitle.toString() : null);
        }
    }

    /**
     * Shows over the filter icon the number of active filters, or hides it if there are none.
     * The badge can only be attached once the menu of the toolbar has been created.
     */
    @OptIn(markerClass = ExperimentalBadgeUtils.class)
    private void updateFilterBadge() {
        if (toolbar == null || toolbar.findViewById(R.id.menuItemFilter) == null) {
            return;     // the menu has not been created yet
        }
        if (filterBadge == null) {
            if (activeFilters == 0) {
                return;
            }
            filterBadge = BadgeDrawable.create(this);
            BadgeUtils.attachBadgeDrawable(filterBadge, toolbar, R.id.menuItemFilter);
        }
        filterBadge.setNumber(activeFilters);
        filterBadge.setVisible(activeFilters > 0);
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