package es.unican.bicis.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import lombok.Getter;
import lombok.Setter;

import com.google.gson.annotations.JsonAdapter;

/**
 * A station of a bike sharing network, as defined in the
 * <a href="https://api.citybik.es/v2/">CityBikes API</a>.
 * The #SerializedName annotation is a JSON annotation that defines the name of the property
 * as defined in the JSON response.
 * Getters and setters are automatically generated at compile time by Lombok.
 */
@Parcel
@Getter
@Setter
public class Station {

    @SerializedName("id")                   protected String id;
    @SerializedName("name")                 protected String name;
    @SerializedName("latitude")             protected Double latitude;
    @SerializedName("longitude")            protected Double longitude;
    @SerializedName("timestamp")            protected String timestamp;

    @JsonAdapter(IntegerAdapter.class)
    @SerializedName("free_bikes")           protected Integer freeBikes;
    @JsonAdapter(IntegerAdapter.class)
    @SerializedName("empty_slots")          protected Integer emptySlots;

    @SerializedName("extra")                protected StationExtra extra;

    /**
     * Devuelve el número de bicis eléctricas disponibles en la estación.
     * @return número de bicis eléctricas, o null si la red no informa de ese dato
     */
    public Integer getEbikes() {
        if (extra == null) {
            return null;
        }
        return extra.getEbikes();
    }

}
