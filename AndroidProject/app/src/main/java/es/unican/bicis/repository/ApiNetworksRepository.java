package es.unican.bicis.repository;

import javax.annotation.Nonnull;

import es.unican.bicis.model.NetworkResponse;
import es.unican.bicis.model.NetworksResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

import es.unican.bicis.model.Station;

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

    /**
     * Request the stations of a single network from the CityBikes real API.
     * @see INetworksRepository#requestStations(StationsCallback, String)
     * @param cb the callback that will asynchronously process the returned stations
     * @param networkId id of the network
     */
    @Override
    public void requestStations(StationsCallback cb, String networkId) {
        Call<NetworkResponse> call = CityBikesService.api.network(networkId);
        call.enqueue(new Callback<NetworkResponse>() {
            @Override
            public void onResponse(@Nonnull Call<NetworkResponse> call, @Nonnull Response<NetworkResponse> response) {
                NetworkResponse body = response.body();
                // El servicio contesta, pero con un error (404, 500...) o sin datos
                if (!response.isSuccessful() || body == null || body.getNetwork() == null) {
                    cb.onFailure(new Throwable("Error retrieving stations of network " + networkId
                            + " (HTTP " + response.code() + ")"));
                    return;
                }
                // Red sin estaciones: devolvemos lista vacía, no null
                List<Station> stations = body.getNetwork().getStations();
                if (stations == null) {
                    stations = new ArrayList<>();
                }
                cb.onSuccess(stations);
            }

            @Override
            public void onFailure(@Nonnull Call<NetworkResponse> call, @Nonnull Throwable t) {
                // Sin conexión, tiempo de espera agotado o JSON que no se puede leer
                cb.onFailure(t);
            }
        });
    }

}
