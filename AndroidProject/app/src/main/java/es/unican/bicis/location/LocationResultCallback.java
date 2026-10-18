package es.unican.bicis.location;

/**
 * Callback to receive the result of a location request.
 */
public interface LocationResultCallback {

    /**
     * Called when the location of the device has been obtained.
     * @param latitude latitude of the device
     * @param longitude longitude of the device
     */
    void onSuccess(double latitude, double longitude);

    /**
     * Called when the location of the device could not be obtained.
     * @param e the error
     */
    void onFailure(Throwable e);
}