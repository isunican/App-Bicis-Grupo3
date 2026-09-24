package es.unican.bicis.repository;

import java.util.List;

import es.unican.bicis.model.Network;

/**
 * The callback used by the repository to asynchronously retrieve the list of bike networks.
 */
public interface NetworksCallback {

    /**
     * This method is automatically called when the list of networks was successfully retrieved
     * @param networks the list of retrieved networks
     */
    public void onSuccess(List<Network> networks);

    /**
     * This method is automatically called when there was some failure when retrieving the
     * networks.
     * @param e the information about the failure
     */
    public void onFailure(Throwable e);

}