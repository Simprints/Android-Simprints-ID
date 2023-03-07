package com.simprints.feature.dashboard.tools.di

import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.EventSystemModule
import com.simprints.infra.events.events_sync.down.EventDownSyncScopeRepository
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

}
