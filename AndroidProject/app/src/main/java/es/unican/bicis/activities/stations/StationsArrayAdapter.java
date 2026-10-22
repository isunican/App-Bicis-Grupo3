
package es.unican.bicis.activities.stations;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import es.unican.bicis.R;
import es.unican.bicis.common.StationUtils;
import es.unican.bicis.model.Station;

/**
 * Adapter that displays the stations of a bike sharing network.
 * Each row contains the station name, free bikes,
 * empty slots and electric bikes.
 */
public class StationsArrayAdapter extends ArrayAdapter<Station> {

    /**
     * Creates an adapter for the given stations.
     * @param context the context of the list where the stations are shown
     * @param stations the stations to show, in the order they must be shown
     */
    public StationsArrayAdapter(@NonNull Context context,
                                @NonNull List<Station> stations) {
        super(context, 0, stations);
    }

    /**
     * Returns the row of the list that shows the station in the given position.
     * Missing values are shown as "-".
     * @see ArrayAdapter#getView(int, View, ViewGroup)
     * @param position position of the station in the list
     * @param convertView an old row that can be reused, or null
     * @param parent the list that contains the row
     * @return the row with the data of the station
     */
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
        TextView tvBikes = row.findViewById(R.id.tvStationBikes);
        TextView tvSlots = row.findViewById(R.id.tvStationSlots);
        TextView tvElectric = row.findViewById(R.id.tvStationElectric);

        if (station != null) {
            tvName.setText(StationUtils.formatText(station.getName()));
            tvBikes.setText(StationUtils.formatValue(station.getFreeBikes()));
            tvSlots.setText(StationUtils.formatValue(station.getEmptySlots()));
            tvElectric.setText(StationUtils.formatValue(station.getEbikes()));
        } else {
            tvName.setText(StationUtils.NO_DATA);
            tvBikes.setText(StationUtils.NO_DATA);
            tvSlots.setText(StationUtils.NO_DATA);
            tvElectric.setText(StationUtils.NO_DATA);
        }

        return row;
    }
}

