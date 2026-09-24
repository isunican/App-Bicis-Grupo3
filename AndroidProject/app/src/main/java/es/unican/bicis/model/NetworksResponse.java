package es.unican.bicis.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.Getter;

/**
 * Model that represents the response obtained from the
 * <a href="https://api.citybik.es/v2/">CityBikes API</a>
 *
 * The API returns an object with a single property "networks", which contains
 * the actual list of bike sharing networks.
 *
 * The #SerializedName annotation is a GSON annotation that defines the name of the property
 * as defined in the json response.
 *
 * Getters are automatically generated at compile time by Lombok.
 */
@Getter
public class NetworksResponse {

    @SerializedName("networks")             private List<Network> networks;

    /**
     * Returns the number of networks in the list
     * @return the number of networks in the list
     */
    public int getNetworksCount() {
        return networks != null ? networks.size() : 0;
    }

}