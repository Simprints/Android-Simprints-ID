package com.simprints.infra.sync

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {
    @Binds
    internal abstract fun provideSyncOrchestrator(syncOrchestratorImpl: SyncOrchestratorImpl): SyncOrchestrator
}
