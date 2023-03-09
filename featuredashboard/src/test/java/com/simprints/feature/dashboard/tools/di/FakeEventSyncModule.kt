package com.simprints.feature.dashboard.tools.di

import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.eventsync.EventSyncModule
import com.simprints.infra.eventsync.EventSyncRepository
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.status.up.EventUpSyncScopeRepository
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [EventSyncModule::class]
)
object FakeEventSyncModule {

    @Provides
    @Singleton
    fun provideEventUpSyncScopeRepository(): EventUpSyncScopeRepository = mockk()

    @Provides
    @Singleton
    fun provideEventDownSyncScopeRepository(): EventDownSyncScopeRepository = mockk()

    @Provides
    @Singleton
    fun provideEventSyncRepository(): EventSyncRepository = mockk()

    @Provides
    @Singleton
    fun provideEventSyncCache(): EventSyncCache = mockk()

    @Provides
    @Singleton
    fun provideEventSyncManager(): EventSyncManager = mockk()
}
