package es.unican.bicis.injection;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityComponent;
import es.unican.bicis.repository.INetworksRepository;
import es.unican.bicis.repository.ApiNetworksRepository;

/**
 * This class is the provider of {@link INetworksRepository} implementations for release builds.
 *
 * Any time somebody demands an {@link INetworksRepository} implementation in a release build,
 * Hilt will inject the implementation provided by this module. This implementation accesses
 * the real CityBikes API.
 *
 * This file is located in the src/release source set, so it is only compiled in release builds.
 * Debug builds use the version of this class located in src/debug, which provides fake data.
 */
@Module
@InstallIn(ActivityComponent.class)
public abstract class RepositoriesModule {

    @Provides
    public static INetworksRepository provideRepository() {
        return ApiNetworksRepository.INSTANCE;
    }

}