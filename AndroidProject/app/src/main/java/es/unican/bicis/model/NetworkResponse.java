package es.unican.bicis.model;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;

/**
 * Model that represents the response obtained from the
 * GET /v2/networks/{network_id} endpoint of the
 * <a href="https://api.citybik.es/v2/">CityBikes API</a>.
 *
 * The API returns an object with a single property "network", which contains
 * the requested network together with its stations.
 *
 * The #SerializedName annotation is a GSON annotation that defines the name of the property
 * as defined in the json response.
 *
 * Getters are automatically generated at compile time by Lombok.
 */
@Getter
public class NetworkResponse {

    @SerializedName("network")             private Network network;

}
