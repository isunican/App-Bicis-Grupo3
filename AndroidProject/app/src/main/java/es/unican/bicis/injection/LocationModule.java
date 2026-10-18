package es.unican.bicis.injection;

import android.app.Application;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityComponent;
import es.unican.bicis.location.DeviceLocationProvider;
import es.unican.bicis.location.ILocationProvider;

/**
 * Indica a Hilt que implementacion de ILocationProvider tiene que inyectar.
 * En los tests de interfaz se puede cambiar por una ubicacion simulada.
 */
@Module
@InstallIn(ActivityComponent.class)
public abstract class LocationModule {

    /**
     * Crea el proveedor de ubicacion real del dispositivo.
     * @param application la aplicacion (Hilt la pasa automaticamente)
     * @return el proveedor de ubicacion
     */
    @Provides
    public static ILocationProvider provideLocationProvider(Application application) {
        return new DeviceLocationProvider(application);
    }
}