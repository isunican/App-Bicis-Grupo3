package es.unican.bicis.repository;

import es.unican.bicis.model.Network;

/**
 * A repository to retrieve bike sharing networks
 */
public interface INetworksRepository {

    /**
     * Asynchronously requests the list of all bike sharing networks.
     * @param cb the callback that will asynchronously process the returned networks
     */
    public void requestNetworks(NetworksCallback cb);

    /**
     * Asynchronously requests the details of a single bike sharing network,
     * including its stations.
     * @param cb the callback that will asynchronously process the returned network
     * @param networkId id of the network
     */
    public void requestNetwork(NetworkDetailsCallback cb, String networkId);

    /**
     * Asynchronously requests the stations of a single bike sharing network.
     * If the network has no stations, onSuccess is called with an empty list.
     * If the service cannot be reached or does not respond, onFailure is called.
     * @param cb the callback that will asynchronously process the returned stations
     * @param networkId id of the network
     */
    public void requestStations(StationsCallback cb, String networkId);

}