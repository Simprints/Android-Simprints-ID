package com.simprints.infra.events

import com.simprints.infra.events.domain.validators.SessionEventValidatorsFactory
import com.simprints.infra.events.domain.validators.SessionEventValidatorsFactoryImpl
import com.simprints.infra.events.event.local.*
import com.simprints.infra.events.local.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext


@Module
@InstallIn(SingletonComponent::class)
abstract class EventsModule {

    @Binds
    internal abstract fun bindSessionDataCache(impl: SessionDataCacheImpl): SessionDataCache

    @Binds
    internal abstract fun bindSessionEventValidatorsFactory(impl: SessionEventValidatorsFactoryImpl): SessionEventValidatorsFactory

    @Binds
    internal abstract fun bindEventDatabaseFactory(impl: DbEventDatabaseFactoryImpl): EventDatabaseFactory

    @Binds
    internal abstract fun bindEventLocalDataSource(impl: EventLocalDataSourceImpl): EventLocalDataSource

    @Binds
    internal abstract fun bindEventRepositoryImpl(impl: EventRepositoryImpl): EventRepository
}
