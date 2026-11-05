package es.unican.bicis.activities.stations;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.IntentCompat;
import androidx.cursoradapter.widget.SimpleCursorAdapter;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.android.material.materialswitch.MaterialSwitch;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import es.unican.bicis.R;
import es.unican.bicis.activities.details.DetailsView;
import es.unican.bicis.common.StationUtils;
import es.unican.bicis.location.ILocationProvider;
import es.unican.bicis.location.LocationResultCallback;
import es.unican.bicis.model.Network;
import es.unican.bicis.model.Station;
import es.unican.bicis.repository.INetworksRepository;

/**
 * View that displays the stations of a bike sharing network.
 * It follows the MVP pattern: all the logic is in {@link StationsPresenter},
 * this view only displays what the presenter tells it and forwards the user actions.
 * The toolbar includes the station search (HU 532314 Buscar estación por nombre): every
 * change of the text is sent to the presenter, the names of the matching stations are shown
 * in a dropdown below the search field and, when no station matches, a panel shows the
 * "not found" message with the suggestion of the most similar station.
 */
@AndroidEntryPoint
public class StationsView extends AppCompatActivity implements IStationsContract.View {

    /** Key for the intent that contains the network */
    public static final String INTENT_NETWORK = "INTENT_NETWORK";

    /** The repository to access the data. This is automatically injected by Hilt in this class */
    @Inject
    INetworksRepository repository;

    /** Gives the location of the user, for the distance filter. Injected by Hilt */
    @Inject
    ILocationProvider locationProvider;

    /** Last known location of the user {latitude, longitude}, or null if it is not known */
    private double[] userLocation;

    /** The presenter of this view */
    private IStationsContract.Presenter presenter;

    /** View elements */
    private TextView tvNetworkName;
    private TextView tvNetworkLocation;
    private TextView tvStationsMessage;
    private ListView lvStations;
    private View stationsHeader;
    private Toolbar toolbar;

    /** Panel shown when no station has the searched name, with the "¿Quizás quisiste decir...?" suggestion */
    private View searchNotFoundPanel;

    /** Group of the not found panel with the suggestion title and the suggested station */
    private View suggestionGroup;

    /** Name of the suggested station; pressing it shows the station location */
    private TextView tvSuggestion;

    /** Message "No hay sugerencias similares", shown when no station is similar */
    private TextView tvNoSuggestion;

    /** Filter dialog currently open (null if it is closed) and its input fields */
    private AlertDialog filterDialog;
    private EditText filterInput;
    private EditText emptySlotsInput;
    private MaterialSwitch distanceSwitch;
    private SeekBar distanceBar;
    private EditText distanceInput;

    /** Whether the distance bar and the distance field are being synchronized (avoids loops) */
    private boolean syncingDistance = false;

    /** Whether the presenter has reported an invalid value while applying the filters */
    private boolean invalidValueShown = false;

    /** Texts of the last filters applied, to fill the dialog when it is opened again */
    private String appliedFreeBikesText = "";
    private String appliedEmptySlotsText = "";
    private boolean appliedDistanceOn = false;
    private String appliedDistanceText = "";

    /** Parts of the subtitle that describe the active filters (null if that filter is not active) */
    private String freeBikesSubtitle;
    private String emptySlotsSubtitle;
    private String distanceSubtitle;

    /** Number of active filters, shown over the filter icon */
    private int activeFilters = 0;
    private BadgeDrawable filterBadge;

    /** Adapter used to display station name suggestions. */
    private SimpleCursorAdapter suggestionsAdapter;

    /** Search field displayed in the toolbar. */
    private SearchView stationSearchView;

    /**
     * Stations currently displayed in the suggestions dropdown, in the same order as
     * the rows of {@link #suggestionsAdapter}, to know which station the user selects.
     */
    private List<Station> suggestedStations = new ArrayList<>();

    /**
     * Links the view elements and creates the presenter with the network
     * received in the intent ({@link #INTENT_NETWORK}).
     * * @param savedInstanceState the saved state of the activity (not used)
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
        searchNotFoundPanel = findViewById(R.id.searchNotFoundPanel);
        suggestionGroup = findViewById(R.id.suggestionGroup);
        tvSuggestion = findViewById(R.id.tvSuggestion);
        tvNoSuggestion = findViewById(R.id.tvNoSuggestion);

        // Get Network from the intent that triggered this activity
        Parcelable wrapped = IntentCompat.getParcelableExtra(getIntent(), INTENT_NETWORK, Parcelable.class);
        Network network = wrapped == null ? null : Parcels.unwrap(wrapped);

        presenter = new StationsPresenter();
        presenter.init(this, network);

        // The location is requested in advance, so that it is known when the filter is applied
        requestUserLocation();
    }

    /**
     * Requests the location of the user and keeps it for the distance filter (HU 532304).
     * If it cannot be obtained (no permission, GPS off...) the location remains unknown.
     */
    private void requestUserLocation() {
        if (locationProvider == null) {
            return;
        }
        locationProvider.requestCurrentLocation(new LocationResultCallback() {
            @Override
            public void onSuccess(double latitude, double longitude) {
                userLocation = new double[] {latitude, longitude};
            }

            @Override
            public void onFailure(Throwable e) {
                // the last known location (if any) is kept
            }
        });
    }

    /**
     * Creates the stations toolbar menu and prepares the station search (HU 532314).
     * Every change of the text of the search field, and its submission, is sent to the
     * presenter with {@link IStationsContract.Presenter#onSearchTextChanged(String)}.
     * Selecting a name of the suggestions dropdown selects that station.
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
                // Every time the text changes the presenter updates the results (HU 532314)
                stationSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        presenter.onSearchTextChanged(query);
                        stationSearchView.clearFocus();
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        presenter.onSearchTextChanged(newText);
                        return true;
                    }
                });

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

        // The menu is created after the presenter has loaded the stations: show the badge now
        toolbar.post(this::updateFilterBadge);
        return true;
    }

    /**
     * Closes the suggestions dropdown and notifies the presenter that the user
     * has selected the station in the given position of {@link #suggestedStations}.
     *
     * @param position position of the selected suggestion
     * @return true if the selection was handled, false if the position is not valid
     */
    private boolean selectSuggestion(int position) {
        if (stationSearchView == null || presenter == null
                || position < 0 || position >= suggestedStations.size()) {
            return false;
        }

        Station station = suggestedStations.get(position);

        if (station == null) {
            return false;
        }

        AutoCompleteTextView searchInput =
                stationSearchView.findViewById(
                        androidx.appcompat.R.id.search_src_text
                );

        if (searchInput != null) {
            searchInput.dismissDropDown();
        }

        presenter.onStationSelected(station);

        return true;
    }

    /**
     * This is called when an item in the action bar menu is selected.
     * @param item The menu item that was selected.
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
        setUpDistanceControls(content);

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
            distanceSwitch = null;
            distanceBar = null;
            distanceInput = null;
        });

        filterDialog.show();
    }

    /**
     * Prepares the distance controls of the filter dialog (HU 532304): a switch that
     * activates the filter, and a bar and a field in km that are kept synchronized.
     * The bar goes from 0.1 km to the maximum distance of the city (the farthest station),
     * in steps of 0.1 km. If the location of the user is not known, the controls are disabled.
     * @param content the content of the dialog
     */
    private void setUpDistanceControls(View content) {
        distanceSwitch = content.findViewById(R.id.swDistanceFilter);
        distanceBar = content.findViewById(R.id.sbMaxDistance);
        distanceInput = content.findViewById(R.id.etMaxDistance);
        TextView unavailable = content.findViewById(R.id.tvDistanceUnavailable);

        // the location is updated every time the dialog is opened, so that the bar goes
        // up to the farthest station from where the user is now (not from where he was
        // when the screen was opened)
        requestUserLocation();
        int maxTenths = (int) Math.ceil(presenter.getMaxCityDistanceKm() * 10);
        boolean available = maxTenths > 0;
        unavailable.setVisibility(available ? View.GONE : View.VISIBLE);
        distanceSwitch.setEnabled(available);
        distanceSwitch.setChecked(available && appliedDistanceOn);

        distanceBar.setMin(1);
        distanceBar.setMax(Math.max(maxTenths, 1));
        distanceInput.setText(appliedDistanceText);
        updateBarFromInput();
        updateDistanceControlsEnabled();

        distanceSwitch.setOnCheckedChangeListener((button, checked) -> {
            if (checked) {
                fillMaxDistanceIfEmpty();
            }
            updateDistanceControlsEnabled();
        });
        distanceBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                if (fromUser && !syncingDistance) {
                    syncingDistance = true;
                    distanceInput.setText(formatKm(progress / 10.0));
                    syncingDistance = false;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar bar) {
                // Nothing to do: the value is updated while the bar moves
            }

            @Override
            public void onStopTrackingTouch(SeekBar bar) {
                // Nothing to do: the value is updated while the bar moves
            }
        });

        distanceInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
                // Nothing to do before the change
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                // Nothing to do during the change
            }

            @Override
            public void afterTextChanged(Editable text) {
                if (!syncingDistance) {
                    updateBarFromInput();
                }
            }
        });
    }

    /**
     * Moves the distance bar to the value written in the distance field, if it is a number.
     */
    private void updateBarFromInput() {
        try {
            double km = Double.parseDouble(distanceInput.getText().toString().trim().replace(",", "."));
            syncingDistance = true;
            distanceBar.setProgress((int) Math.round(km * 10));
        } catch (NumberFormatException e) {
            // Not a number (or empty): the bar is not moved
        } finally {
            syncingDistance = false;
        }
    }


    /**
     * Puts the maximum distance of the city (the end of the bar) in the distance field
     * if it is empty, so that by default the filter shows all the stations
     * (agreed with the Product Owner in the Sprint Planning).
     */
    private void fillMaxDistanceIfEmpty() {
        if (distanceInput.getText().toString().trim().isEmpty()) {
            syncingDistance = true;
            distanceBar.setProgress(distanceBar.getMax());
            distanceInput.setText(formatKm(distanceBar.getMax() / 10.0));
            syncingDistance = false;
        }
    }

    /**
     * Enables the distance bar and field only if the distance switch is on.
     */
    private void updateDistanceControlsEnabled() {
        boolean on = distanceSwitch.isChecked();
        distanceBar.setEnabled(on);
        distanceInput.setEnabled(on);
    }

    /**
     * Formats a distance in km with one decimal and a comma (e.g. "1,0").
     * @param km the distance
     * @return the formatted distance
     */
    private static String formatKm(double km) {
        return String.format(Locale.forLanguageTag("es-ES"), "%.1f", km);
    }

    /**
     * Sends to the presenter the filters introduced in the dialog.
     * An empty field (or the distance switch off) means that the corresponding filter
     * is not applied. If no filter is given at all, the free bikes filter is sent so that
     * the presenter reports the invalid value (behaviour of HU 532376).
     * If a filter that was applied has been removed in the dialog, all the filters are
     * removed and the remaining ones are applied again.
     * The dialog is closed only if the presenter has not reported any invalid value.
     */
    private void onApplyFiltersClicked() {
        String freeBikesText = filterInput.getText().toString().trim();
        String emptySlotsText = emptySlotsInput.getText().toString().trim();
        boolean distanceOn = distanceSwitch.isChecked();
        String distanceText = distanceInput.getText().toString().trim();
        boolean filterRemoved = (!appliedFreeBikesText.isEmpty() && freeBikesText.isEmpty())
                || (!appliedEmptySlotsText.isEmpty() && emptySlotsText.isEmpty())
                || (appliedDistanceOn && !distanceOn);
        boolean valid = true;

        if (!freeBikesText.isEmpty() || (emptySlotsText.isEmpty() && !distanceOn)) {
            valid = applyFilterAndCheck(() -> presenter.onFilterApplied(freeBikesText));
        }
        if (valid && !emptySlotsText.isEmpty()) {
            valid = applyFilterAndCheck(() -> presenter.onMinEmptySlotsFilterApplied(emptySlotsText));
        }
        if (valid && distanceOn) {
            valid = applyFilterAndCheck(() -> presenter.onDistanceFilterApplied(distanceText));
        }
        if (valid && filterRemoved) {
            reapplyFilters(freeBikesText, emptySlotsText, distanceOn ? distanceText : "");
        }
        if (valid && filterDialog != null) {
            appliedFreeBikesText = freeBikesText;
            appliedEmptySlotsText = emptySlotsText;
            // if the location was not known the distance filter has not been applied
            appliedDistanceOn = distanceOn && distanceSubtitle != null;
            appliedDistanceText = appliedDistanceOn ? distanceText : "";
            filterDialog.dismiss();
        }
    }

    /**
     * Sends one filter to the presenter and tells whether its value was valid.
     * The presenter reports an invalid value by calling one of the showInvalid... methods
     * of this view, which set {@link #invalidValueShown}.
     * @param filter the call to the presenter that applies the filter
     * @return true if the value was valid, false if the presenter reported it as invalid
     */
    private boolean applyFilterAndCheck(Runnable filter) {
        invalidValueShown = false;
        filter.run();
        return !invalidValueShown;
    }

    /**
     * Removes all the filters and applies again the ones that are still in the dialog.
     * It is used when a filter has been removed in the dialog (field emptied or distance
     * switch turned off), because the presenter only removes all the filters at once.
     * The values have already been validated.
     * @param freeBikesText the free bikes value, or empty if it is not applied
     * @param emptySlotsText the empty slots value, or empty if it is not applied
     * @param distanceText the distance value, or empty if it is not applied
     */
    private void reapplyFilters(String freeBikesText, String emptySlotsText, String distanceText) {
        presenter.onFilterCleared();
        if (!freeBikesText.isEmpty()) {
            presenter.onFilterApplied(freeBikesText);
        }
        if (!emptySlotsText.isEmpty()) {
            presenter.onMinEmptySlotsFilterApplied(emptySlotsText);
        }
        if (!distanceText.isEmpty()) {
            presenter.onDistanceFilterApplied(distanceText);
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
        showStationList(new StationsArrayAdapter(this, stations));
    }

    /**
     * Shows the given adapter in the list, hiding any message.
     * @param adapter the adapter with the stations to show
     */
    private void showStationList(StationsArrayAdapter adapter) {
        lvStations.setAdapter(adapter);
        lvStations.setVisibility(View.VISIBLE);
        stationsHeader.setVisibility(View.VISIBLE);
        tvStationsMessage.setVisibility(View.GONE);
        searchNotFoundPanel.setVisibility(View.GONE);
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
     * @see IStationsContract.View#showEmptySlotsFilterError()
     */
    @Override
    public void showEmptySlotsFilterError() {
        showMessage(getString(R.string.filterEmptySlotsError));
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
        distanceSubtitle = null;
        appliedFreeBikesText = "";
        appliedEmptySlotsText = "";
        appliedDistanceOn = false;
        appliedDistanceText = "";
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
     * Hides the list and shows the not found panel with the message
     * "No hay sugerencias similares", until a suggestion is shown.
     * @see IStationsContract.View#showSearchSuggestions(List)
     * @param stations the stations to suggest
     */
    @Override
    public void showSearchSuggestions(List<Station> stations) {
        if (suggestionsAdapter == null || stationSearchView == null) {
            return;
        }

        suggestedStations = new ArrayList<>();

        MatrixCursor cursor = new MatrixCursor(
                new String[]{BaseColumns._ID, "station_name"}
        );

        if (stations != null) {
            for (Station station : stations) {
                if (station != null) {
                    suggestedStations.add(station);

                    cursor.addRow(new Object[]{
                            suggestedStations.size() - 1,
                            station.getName()
                    });
                }
            }
        }

        suggestionsAdapter.changeCursor(cursor);

        AutoCompleteTextView searchInput =
                stationSearchView.findViewById(
                        androidx.appcompat.R.id.search_src_text
                );

        if (searchInput == null) {
            return;
        }

        // Allow suggestions to appear from the first character.
        searchInput.setThreshold(1);

        if (suggestedStations.isEmpty()) {
            searchInput.dismissDropDown();
        } else {
            searchInput.post(() -> {
                if (searchInput.hasFocus()
                        && searchInput.getText().length() >= 1) {
                    searchInput.showDropDown();
                }
            });
        }
    }

    /**
     * @see IStationsContract.View#showStationNotFound()
     */
    @Override
    public void showStationNotFound() {
        lvStations.setVisibility(View.GONE);
        stationsHeader.setVisibility(View.GONE);
        tvStationsMessage.setVisibility(View.GONE);

        // Until the presenter sends a suggestion, "No hay sugerencias similares" is shown
        suggestionGroup.setVisibility(View.GONE);
        tvSuggestion.setOnClickListener(null);
        tvNoSuggestion.setVisibility(View.VISIBLE);
        searchNotFoundPanel.setVisibility(View.VISIBLE);
    }

    /**
     * Shows the name of the station in the not found panel. Pressing it
     * informs the presenter that the user has selected that station.
     * @see IStationsContract.View#showSuggestion(Station)
     * @param station the most similar station
     */
    @Override
    public void showSuggestion(Station station) {
        tvSuggestion.setText(station.getName());
        tvSuggestion.setOnClickListener(v -> presenter.onStationSelected(station));
        tvNoSuggestion.setVisibility(View.GONE);
        suggestionGroup.setVisibility(View.VISIBLE);
    }

    /**
     * Opens a dialog with the name of the station as title, its available bikes
     * and its coordinates with four decimals.
     * @see IStationsContract.View#showStationLocation(Station)
     * @param station the station whose location must be shown
     */
    @Override
    public void showStationLocation(Station station) {
        String bikes = StationUtils.formatBikes(
                station.getFreeBikes(), station.getEmptySlots(), station.getSlots());
        String message = getString(R.string.stationBikes, bikes)
                + "\n" + getString(R.string.stationLocationLatitude, formatCoordinate(station.getLatitude()))
                + "\n" + getString(R.string.stationLocationLongitude, formatCoordinate(station.getLongitude()));

        new AlertDialog.Builder(this)
                .setTitle(station.getName())
                .setMessage(message)
                .setPositiveButton(R.string.stationLocationClose, null)
                .show();
    }

    /**
     * Formats a coordinate with four decimals and a dot (e.g. "43.4686").
     * @param value the coordinate (may be null)
     * @return the formatted coordinate, or "-" if it is not available
     */
    private String formatCoordinate(Double value) {
        if (value == null) {
            return StationUtils.NO_DATA;
        }
        return String.format(Locale.ROOT, "%.4f", value);
    }

    // ── HU 532304 Filtrar por distancia ──────────────────────────────

    /**
     * @see IStationsContract.View#getUserLocation()
     * @return the location of the user, or null if it is not available
     */
    @Override
    public double[] getUserLocation() {
        return userLocation;
    }

    /**
     * @see IStationsContract.View#showNoStationsWithinDistance(double)
     * @param maxDistKm the maximum distance requested, in km
     */
    @Override
    public void showNoStationsWithinDistance(double maxDistKm) {
        showMessage(getString(R.string.distanceFilterNoResults));
    }

    /**
     * @see IStationsContract.View#showLocationUnavailableForFilter()
     */
    @Override
    public void showLocationUnavailableForFilter() {
        // Temporary message: the list and the active filters do not change
        Toast.makeText(this, R.string.distanceLocationUnavailable, Toast.LENGTH_LONG).show();
    }

    /**
     * @see IStationsContract.View#showInvalidMaxDistance()
     */
    @Override
    public void showInvalidMaxDistance() {
        invalidValueShown = true;
        if (distanceInput != null) {
            distanceInput.setError(getString(R.string.distanceFilterInvalid));
        }
    }

    /**
     * @see IStationsContract.View#showDistanceFilterActive(double)
     * @param maxDistKm the maximum distance of the active filter, in km
     */
    @Override
    public void showDistanceFilterActive(double maxDistKm) {
        distanceSubtitle = getString(R.string.distanceFilterActive, StationUtils.formatDistance(maxDistKm));
        updateSubtitle();
    }

    /**
     * @see IStationsContract.View#showStationsWithDistance(List, Map)
     * @param stations the stations sorted by distance
     * @param distances the distance of each station, in km
     */
    @Override
    public void showStationsWithDistance(List<Station> stations, Map<Station, Double> distances) {
        showStationList(new StationsArrayAdapter(this, stations, distances));
    }

    /**
     * Indicates whether any filter is active, to enable the "Quitar filtros" button.
     * Indicates whether any filter is active, to enable the "Quitar filtros" button.
     * A filter is active if the presenter has reported it (number over the filter icon)
     * or if its text is shown in the subtitle of the action bar.
     * @return true if there is at least one filter active
     */
    private boolean isAnyFilterActive() {
        return activeFilters > 0 || freeBikesSubtitle != null || emptySlotsSubtitle != null
                || distanceSubtitle != null;
    }

    /**
     * Shows in the subtitle of the action bar the active filters, separated by " · ",
     * or removes the subtitle if no filter is active.
     */
    private void updateSubtitle() {
        StringBuilder subtitle = new StringBuilder();
        for (String part : new String[] {freeBikesSubtitle, emptySlotsSubtitle, distanceSubtitle}) {
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
        searchNotFoundPanel.setVisibility(View.GONE);
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