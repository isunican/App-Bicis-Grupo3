package es.unican.bicis.repository;

import es.unican.bicis.model.NetworkResponse;
import es.unican.bicis.model.NetworksResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * CityBikes <a href="https://api.citybik.es/v2/">API</a>
 * using Retrofit
 */
public interface ICityBikesAPI {

    /**
     * Retrieve all the bike sharing networks in the world.
     * This list only contains metadata about each network (no stations).
     *
     * @return retrofit call object
     */
    @GET("v2/networks")
    Call<NetworksResponse> networks();

    /**
     * Retrieve the details of a single network, including all its stations.
     *
     * @param networkId id of the network
     * @return retrofit call object
     */
    @GET("v2/networks/{network_id}")
    Call<NetworkResponse> network(@Path("network_id") String networkId);

}