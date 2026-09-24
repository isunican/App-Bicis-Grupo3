package es.unican.bicis.repository;

import javax.annotation.Nonnull;

import es.unican.bicis.model.NetworkResponse;
import es.unican.bicis.model.NetworksResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Implementation of {@link INetworksRepository} that access the real
 * <a href="https://api.citybik.es/v2/">CityBikes API</a>
 */
public class ApiNetworksRepository implements INetworksRepository {


    /** Since this class does not have any state, it can be a singleton */
    public static final INetworksRepository INSTANCE = new ApiNetworksRepository();

    /** Singleton pattern with private constructor */
    private ApiNetworksRepository() {}

    /**
     * Request the list of bike networks from the CityBikes real API.
     * @see INetworksRepository#requestNetworks(NetworksCallback)
     * @param cb the callback that will asynchronously process the returned networks
     */
    @Override
    public void requestNetworks(NetworksCallback cb) {
        Call<NetworksResponse> call = CityBikesService.api.networks();
        call.enqueue(new Callback<NetworksResponse>() {
            @Override
            public void onResponse(@Nonnull Call<NetworksResponse> call, @Nonnull Response<NetworksResponse> response) {
                NetworksResponse body = response.body();
                if (response.isSuccessful() && body != null) {
                    cb.onSuccess(body.getNetworks());
                } else {
                    cb.onFailure(new Throwable("Error retrieving networks"));
                }
            }

            @Override
            public void onFailure(@Nonnull Call<NetworksResponse> call, @Nonnull Throwable t) {
                cb.onFailure(t);
            }
        });
    }

    /**
     * Request the details of a single network from the CityBikes real API.
     * @see INetworksRepository#requestNetwork(NetworkDetailsCallback, String)
     * @param cb the callback that will asynchronously process the returned network
     * @param networkId id of the network
     */
    @Override
    public void requestNetwork(NetworkDetailsCallback cb, String networkId) {
        Call<NetworkResponse> call = CityBikesService.api.network(networkId);
        call.enqueue(new Callback<NetworkResponse>() {
            @Override
            public void onResponse(@Nonnull Call<NetworkResponse> call, @Nonnull Response<NetworkResponse> response) {
                NetworkResponse body = response.body();
                if (response.isSuccessful() && body != null && body.getNetwork() != null) {
                    cb.onSuccess(body.getNetwork());
                } else {
                    cb.onFailure(new Throwable("Error retrieving network"));
                }
            }

            @Override
            public void onFailure(@Nonnull Call<NetworkResponse> call, @Nonnull Throwable t) {
                cb.onFailure(t);
            }
        });
    }

}
