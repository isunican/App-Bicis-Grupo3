package es.unican.bicis.model;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * A bike sharing network (a city-level bike rental system), as defined in the
 * <a href="https://api.citybik.es/v2/">CityBikes API</a>.
 * The #SerializedName annotation is a GSON annotation that defines the name of the property
 * as defined in the json response.
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
    @SerializedName("ebikes")               protected Boolean ebikes;

    @SerializedName("stations")             protected List<Station> stations;

    /**
     * Counts the operating companies of the network, from the list {@code company}.
     * The count is only done if the list is available and all its elements are
     * non-blank texts; otherwise the data is considered not available or anomalous.
     * @return the number of companies, or null if company is missing, empty
     * or contains blank elements
     */
    public Integer countOperators() {
        if (company == null || company.length == 0) {
            return null;
        }
        for (String c : company) {
            if (isBlank(c)) {
                return null;
            }
        }
        return company.length;
    }

    /**
     * Checks if the network has no detailed data, only the basic data shown in the
     * main list (name and city). Detailed data are the coordinates, ebikes and company.
     * A false value of ebikes counts as a data (it is not the same as missing).
     * @return true if latitude, longitude, ebikes and company are all missing
     */
    public boolean hasOnlyBasicInfo() {
        boolean hasLocationData = location != null
                && (location.getLatitude() != null || location.getLongitude() != null);
        return !hasLocationData && ebikes == null && company == null;
    }

    /**
     * Checks if a text is missing or only contains blank characters.
     * @param s the text to check
     * @return true if s is null, empty or blank
     */
    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }


}