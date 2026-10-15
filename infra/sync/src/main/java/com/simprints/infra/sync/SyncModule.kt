package com.simprints.infra.sync

import com.simprints.infra.sync.devicestate.DeviceStateDataTracker
import com.simprints.infra.sync.devicestate.DeviceStateDataTrackerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {
    @Binds
    internal abstract fun provideSyncOrchestrator(syncOrchestratorImpl: SyncOrchestratorImpl): SyncOrchestrator

    @Binds
    internal abstract fun provideDeviceStateDataTracker(impl: DeviceStateDataTrackerImpl): DeviceStateDataTracker
}
