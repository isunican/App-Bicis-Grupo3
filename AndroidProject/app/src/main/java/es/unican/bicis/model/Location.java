package es.unican.bicis.model;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import lombok.Getter;
import lombok.Setter;

/**
 * Geographic location of a bike sharing network, as defined in the
 * <a href="https://api.citybik.es/v2/">CityBikes API</a>.
 *
 * The #SerializedName annotation is a GSON annotation that defines the name of the property
 * as defined in the json response.
 *
 * Getters are automatically generated at compile time by Lombok.
 */
@Parcel
@Getter
@Setter
public class Location {

    @SerializedName("city")                     protected String city;
    @SerializedName("country")                  protected String country;

    @JsonAdapter(DoubleAdapter.class)
    @SerializedName("latitude")                 protected Double latitude;

    @JsonAdapter(DoubleAdapter.class)
    @SerializedName("longitude")                protected Double longitude;

    /**
     * Checks if both coordinates are available and inside the valid ranges:
     * latitude in [-90, 90] and longitude in [-180, 180], limits included.
     * @return true if latitude and longitude are valid
     */
    public boolean hasValidCoordinates() {
        return latitude != null && longitude != null
                && latitude >= -90 && latitude <= 90
                && longitude >= -180 && longitude <= 180;
    }
}