package es.unican.bicis.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;

import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;

/**
 * Gets the real location of the device using the GPS provider of Android.
 */
public class DeviceLocationProvider implements ILocationProvider {

    /** Maximum age of the last known location to be used directly (2 minutes) */
    private static final long MAX_AGE_MS = 2L * 60 * 1000;

    private final Context context;
    private final LocationManager locationManager;

    /**
     * Constructor.
     * @param context the application context
     */
    public DeviceLocationProvider(Context context) {
        this.context = context;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    /**
     * Requests the current location of the device and returns it through the callback.
     * If there is a recent last known location it is used directly, if not a new one is requested.
     * @param cb the callback that receives the location or the error
     */
    @SuppressLint("MissingPermission")
    @Override
    public void requestCurrentLocation(LocationResultCallback cb) {
        // sin permiso no se puede pedir la ubicacion
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || locationManager == null) {
            cb.onFailure(new Exception("No hay permiso de ubicacion"));
            return;
        }

        // si hay una ubicacion reciente se usa esa, asi no hay que esperar al GPS
        Location ultima = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (ultima != null && System.currentTimeMillis() - ultima.getTime() < MAX_AGE_MS) {
            cb.onSuccess(ultima.getLatitude(), ultima.getLongitude());
            return;
        }

        // si no, se pide una nueva (devuelve null si el GPS esta apagado)
        LocationManagerCompat.getCurrentLocation(locationManager, LocationManager.GPS_PROVIDER,
                (android.os.CancellationSignal) null, ContextCompat.getMainExecutor(context), location -> {                    if (location != null) {
                        cb.onSuccess(location.getLatitude(), location.getLongitude());
                    } else {
                        cb.onFailure(new Exception("No se ha podido obtener la ubicacion"));
                    }
                });
    }
}