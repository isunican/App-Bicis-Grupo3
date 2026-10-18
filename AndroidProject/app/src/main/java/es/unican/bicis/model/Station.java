package es.unican.bicis.model;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import lombok.Getter;
import lombok.Setter;

/**
 * A station of a bike sharing network, as defined in the
 * <a href="https://api.citybik.es/v2/">CityBikes API</a>.
 *
 * Numeric values use tolerant adapters: if a value is missing or invalid,
 * the field is null, meaning "not available".
 *
 * Getters and setters are automatically generated at compile time by Lombok.
 */
@Parcel
@Getter
@Setter
public class Station {

    @SerializedName("id")                   protected String id;
    @SerializedName("name")                 protected String name;

    @JsonAdapter(DoubleAdapter.class)
    @SerializedName("latitude")             protected Double latitude;

    @JsonAdapter(DoubleAdapter.class)
    @SerializedName("longitude")            protected Double longitude;

    @JsonAdapter(IntegerAdapter.class)
    @SerializedName("free_bikes")           protected Integer freeBikes;

    @JsonAdapter(IntegerAdapter.class)
    @SerializedName("empty_slots")          protected Integer emptySlots;

}
