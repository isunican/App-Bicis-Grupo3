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

import es.unican.bicis.R;
import es.unican.bicis.common.StationUtils;
import es.unican.bicis.model.Station;

/**
 * Adapter that displays the stations of a bike sharing network.
 * Each row contains the station name, the bikes as "available/total"
 * and an icon if the station has electric bikes.
 */
public class StationsArrayAdapter extends ArrayAdapter<Station> {

    public StationsArrayAdapter(@NonNull Context context,
                                @NonNull List<Station> stations) {
        super(context, 0, stations);
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

        // INVISIBLE (not GONE) so that the columns stay aligned in every row
        ivElectric.setVisibility(StationUtils.hasEbikes(station) ? View.VISIBLE : View.INVISIBLE);

        return row;
    }
}

