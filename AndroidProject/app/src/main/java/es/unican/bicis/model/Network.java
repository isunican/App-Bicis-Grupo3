package es.unican.bicis.model;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import lombok.Getter;
import lombok.Setter;

/**
 * A bike sharing network (a city-level bike rental system), as defined in the
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
public class Network {

    @SerializedName("id")                   protected String id;
    @SerializedName("name")                 protected String name;
    @SerializedName("location")             protected Location location;
    @SerializedName("href")                 protected String href;
    @SerializedName("gbfs_href")            protected String gbfsHref;



    @JsonAdapter(StringArrayAdapter.class)
    @SerializedName("company")              protected String[] company;


    @JsonAdapter(BooleanAdapter.class)
    @SerializedName("ebikes")               protected boolean ebikes;

}