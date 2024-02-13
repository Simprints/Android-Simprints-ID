package com.simprints.infra.events

import com.simprints.infra.events.event.local.*
import com.simprints.infra.events.local.*
import com.simprints.infra.events.session.SessionEventRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class EventsModule {

    @Binds
    internal abstract fun bindEventRepositoryImpl(impl: EventRepositoryImpl): EventRepository

    @Binds
    internal abstract fun bindSessionEventRepository(impl: SessionEventRepositoryImpl): SessionEventRepository
}
