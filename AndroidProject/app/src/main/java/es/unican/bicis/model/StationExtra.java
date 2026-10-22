package es.unican.bicis.model;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import lombok.Getter;
import lombok.Setter;

/**
 * Extra information of a station returned by the
 * <a href="https://api.citybik.es/v2/">CityBikes API</a>.
 * Not every network provides it.
 */
@Parcel
@Getter
@Setter
public class StationExtra {

    @JsonAdapter(IntegerAdapter.class)
    @SerializedName("ebikes")               protected Integer ebikes;

}
