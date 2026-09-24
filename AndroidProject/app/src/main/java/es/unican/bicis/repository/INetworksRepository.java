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

}