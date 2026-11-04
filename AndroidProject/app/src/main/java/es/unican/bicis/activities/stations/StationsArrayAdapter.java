package es.unican.bicis.activities.stations;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Map;

import es.unican.bicis.R;
import es.unican.bicis.common.StationUtils;
import es.unican.bicis.model.Station;

/**
 * Adapter that displays the stations of a bike sharing network.
 * Each row contains the station name, the bikes as "available/total"
 * and an icon if the station has electric bikes. When the distance filter
 * is active (HU 532304) it also shows the distance from the user to each station.
 */
public class StationsArrayAdapter extends ArrayAdapter<Station> {

    /** Distance (km) from the user to each station, or null if the distances are not shown */
    private final Map<Station, Double> distances;

    /**
     * Creates an adapter that does not show distances.
     * @param context the context
     * @param stations the stations to show
     */
    public StationsArrayAdapter(@NonNull Context context,
                                @NonNull List<Station> stations) {
        this(context, stations, null);
    }

    /**
     * Creates an adapter that shows the distance from the user to each station.
     * @param context the context
     * @param stations the stations to show
     * @param distances distance (km) of each station; stations that are not in the map
     *                  show "-". If null, no distance is shown.
     */
    public StationsArrayAdapter(@NonNull Context context,
                                @NonNull List<Station> stations,
                                @Nullable Map<Station, Double> distances) {
        super(context, 0, stations);
        this.distances = distances;
    }

    @NonNull
    @Override
    public View getView(int position,
                        @Nullable View convertView,
                        @NonNull ViewGroup parent) {

        View row = convertView;

        if (row == null) {
            row = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_station, parent, false);
        }

        Station station = getItem(position);

        TextView tvName = row.findViewById(R.id.tvStationName);
        TextView tvDistance = row.findViewById(R.id.tvStationDistance);
        TextView tvBikes = row.findViewById(R.id.tvStationBikes);
        ImageView ivElectric = row.findViewById(R.id.ivStationElectric);

        if (station != null) {
            tvName.setText(StationUtils.formatText(station.getName()));
            tvBikes.setText(StationUtils.formatBikes(
                    station.getFreeBikes(), station.getEmptySlots(), station.getSlots()));
        } else {
            tvName.setText(StationUtils.NO_DATA);
            tvBikes.setText(StationUtils.formatBikes(null, null, null));
        }

        // The distance is only shown when the distance filter is active
        if (distances != null) {
            tvDistance.setText(StationUtils.formatDistance(distances.get(station)));
            tvDistance.setVisibility(View.VISIBLE);
        } else {
            tvDistance.setVisibility(View.GONE);
        }

        // INVISIBLE (not GONE) so that the columns stay aligned in every row
        ivElectric.setVisibility(StationUtils.hasEbikes(station) ? View.VISIBLE : View.INVISIBLE);

        return row;
    }
}
