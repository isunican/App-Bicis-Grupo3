package es.unican.bicis.repository;

import es.unican.bicis.model.Network;

/**
 * The callback used by the repository to asynchronously retrieve the details of a single
 * bike network.
 */
public interface NetworkDetailsCallback {

    /**
     * This method is automatically called when the network was successfully retrieved
     * @param network the retrieved network
     */
    public void onSuccess(Network network);

    /**
     * This method is automatically called when there was some failure when retrieving the
     * network.
     * @param e the information about the failure
     */
    public void onFailure(Throwable e);

}
