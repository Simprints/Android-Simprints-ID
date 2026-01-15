package com.simprints.feature.dashboard.tools.di

import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.eventsync.EventSyncModule
import com.simprints.infra.eventsync.sync.down.EventDownSyncCountsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [EventSyncModule::class],
)
object FakeEventSyncModule {
    @Provides
    @Singleton
    fun provideEventSyncManager(): EventSyncManager = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideEventDownSyncCountsRepository(): EventDownSyncCountsRepository = mockk(relaxed = true)
}
