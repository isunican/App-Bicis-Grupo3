package es.unican.bicis.injection;

import android.app.Application;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityComponent;
import es.unican.bicis.location.DeviceLocationProvider;
import es.unican.bicis.location.ILocationProvider;

/**
 * This class tells Hilt which implementation of {@link ILocationProvider} must be injected.
 * In UI tests it can be replaced by a fake location.
 */
@Module
@InstallIn(ActivityComponent.class)
public abstract class LocationModule {

    /**
     * Creates the provider of the real location of the device.
     * @param application the application (Hilt passes it automatically)
     * @return the location provider
     */
    @Provides
    public static ILocationProvider provideLocationProvider(Application application) {
        return new DeviceLocationProvider(application);
    }
}