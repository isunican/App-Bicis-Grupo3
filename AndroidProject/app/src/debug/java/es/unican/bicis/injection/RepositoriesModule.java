package es.unican.bicis.injection;

import android.app.Application;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityComponent;
import es.unican.bicis.repository.INetworksRepository;
import es.unican.bicis.repository.JsonNetworksRepository;

/**
 * This class is the provider of {@link INetworksRepository} implementations for debug builds.
 *
 * Any time somebody demands an {@link INetworksRepository} implementation in a debug build,
 * Hilt will inject the implementation provided by this module. This implementation uses
 * fake data loaded from a local json file, so no internet connection is required.
 *
 * This file is located in the src/debug source set, so it is only compiled in debug builds.
 * Release builds use the version of this class located in src/release, which accesses the
 * real CityBikes API.
 */
@Module
@InstallIn(ActivityComponent.class)
public abstract class RepositoriesModule {

    @Provides
    public static INetworksRepository provideRepository(Application application) {
        return new JsonNetworksRepository(application);
    }

}