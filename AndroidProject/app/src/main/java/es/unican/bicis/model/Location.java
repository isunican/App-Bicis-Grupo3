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

    //@return [true] si als coordenadas estan en los valores permitidos
    public boolean hasValidCoordinates() {
        return latitude != null && longitude != null
                && latitude >= -90 && latitude <= 90
                && longitude >= -180 && longitude <= 180;
    }
}