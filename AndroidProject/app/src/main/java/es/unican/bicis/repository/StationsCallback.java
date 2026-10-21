package es.unican.bicis.repository;

import java.util.List;

import es.unican.bicis.model.Station;

/**
 * The callback used by the repository to asynchronously retrieve the stations of a
 * bike network.
 */
public interface StationsCallback {

    /**
     * This method is automatically called when the stations were successfully retrieved.
     * If the network has no stations, the list is empty (never null).
     * @param stations the list of retrieved stations
     */
    public void onSuccess(List<Station> stations);

    /**
     * This method is automatically called when the stations could not be retrieved:
     * there is no connection, the service does not respond, or it returns an error.
     * @param e the information about the failure
     */
    public void onFailure(Throwable e);

}
