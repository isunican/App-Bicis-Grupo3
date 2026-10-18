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
 * Obtiene la ubicacion real del dispositivo.
 */
public class DeviceLocationProvider implements ILocationProvider {

    private final Context context;
    private final FusedLocationProviderClient client;

    /**
     * Constructor.
     * @param context contexto de la aplicacion
     */
    public DeviceLocationProvider(Context context) {
        this.context = context;
        this.client = LocationServices.getFusedLocationProviderClient(context);
    }

    /**
     * Pide la ubicacion actual del dispositivo y la devuelve por el callback.
     * @param cb callback que recibe la ubicacion o el error
     */
    @SuppressLint("MissingPermission")
    @Override
    public void requestCurrentLocation(LocationResultCallback cb) {
        // sin permiso no se puede pedir la ubicacion
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
                        // pasa si el GPS esta apagado o no hay señal
                        cb.onFailure(new Exception("No se ha podido obtener la ubicacion"));
                    }
                })
                .addOnFailureListener(e -> cb.onFailure(e));
    }
}