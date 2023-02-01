package com.simprints.feature.dashboard.tools.di

import com.simprints.eventsystem.EventSystemModule
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.local.EventLocalDataSource
import com.simprints.eventsystem.events_sync.down.EventDownSyncScopeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [EventSystemModule::class]
)
object FakeEventSystemModule {

    @Provides
    @Singleton
    fun provideEventRepository(): EventRepository = mockk()

    @Provides
    @Singleton
    fun provideEventDownSyncScopeRepository(): EventDownSyncScopeRepository = mockk()

    @Provides
    @Singleton
    fun provideEventLocalDataSource(): EventLocalDataSource = mockk()
}
