package es.unican.bicis.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

/**
 * Gets the real location of the device.
 */
public class DeviceLocationProvider implements ILocationProvider {

    private final Context context;
    private final FusedLocationProviderClient client;

    /**
     * Constructor.
     * @param context the application context
     */
    public DeviceLocationProvider(Context context) {
        this.context = context;
        this.client = LocationServices.getFusedLocationProviderClient(context);
    }

    /**
     * Requests the current location of the device and returns it through the callback.
     * @param cb the callback that receives the location or the error
     */
    @SuppressLint("MissingPermission")
    @Override
    public void requestCurrentLocation(LocationResultCallback cb) {
        // without permission the location cannot be requested
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            cb.onFailure(new Exception("No hay permiso de ubicacion"));
            return;
        }

        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        cb.onSuccess(location.getLatitude(), location.getLongitude());
                    } else {
                        // happens if the GPS is off or there is no signal
                        cb.onFailure(new Exception("No se ha podido obtener la ubicacion"));
                    }
                })
                .addOnFailureListener(e -> cb.onFailure(e));
    }
}