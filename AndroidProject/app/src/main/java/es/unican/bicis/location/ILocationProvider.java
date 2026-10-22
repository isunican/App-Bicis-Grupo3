package es.unican.bicis.location;

/**
 * Gives the current location of the device.
 */
public interface ILocationProvider {

    /**
     * Asynchronously requests the current location of the device.
     * @param cb the callback that will receive the location or the error
     */
    void requestCurrentLocation(LocationResultCallback cb);
}