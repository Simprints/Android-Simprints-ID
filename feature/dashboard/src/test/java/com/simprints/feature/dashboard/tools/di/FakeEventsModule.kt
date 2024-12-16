package com.simprints.feature.dashboard.tools.di

import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.EventsModule
import com.simprints.infra.events.session.SessionEventRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [EventsModule::class],
)
object FakeEventsModule {
    @Provides
    @Singleton
    fun provideEventRepository(): EventRepository = mockk()

    @Provides
    @Singleton
    fun provideSessionEventRepository(): SessionEventRepository = mockk()
}
