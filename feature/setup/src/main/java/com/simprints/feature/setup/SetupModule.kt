package com.simprints.feature.setup

import com.simprints.feature.setup.location.LocationStoreWorkerScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SetupModule {
    @Binds
    internal abstract fun provideLocationStore(authManager: LocationStoreWorkerScheduler): LocationStore
}
