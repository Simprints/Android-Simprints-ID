package com.simprints.eventsystem

import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.EventRepositoryImpl
import com.simprints.eventsystem.event.domain.validators.SessionEventValidatorsFactory
import com.simprints.eventsystem.event.domain.validators.SessionEventValidatorsFactoryImpl
import com.simprints.eventsystem.event.local.*
import com.simprints.eventsystem.event.remote.EventRemoteDataSource
import com.simprints.eventsystem.event.remote.EventRemoteDataSourceImpl
import com.simprints.eventsystem.events_sync.down.EventDownSyncScopeRepository
import com.simprints.eventsystem.events_sync.down.EventDownSyncScopeRepositoryImpl
import com.simprints.eventsystem.events_sync.up.EventUpSyncScopeRepository
import com.simprints.eventsystem.events_sync.up.EventUpSyncScopeRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext


@Module(
    includes = [
        EventSystemProvider::class
    ]
)
@InstallIn(ActivityComponent::class)
abstract class EventSystemModule {

    @Binds
    internal abstract fun bindSessionDataCache(impl: SessionDataCacheImpl): SessionDataCache

    @Binds
    internal abstract fun bindSessionEventValidatorsFactory(impl: SessionEventValidatorsFactoryImpl): SessionEventValidatorsFactory

    @Binds
    internal abstract fun bindEventRemoteDataSource(impl: EventRemoteDataSourceImpl): EventRemoteDataSource

    @Binds
    internal abstract fun bindEventDatabaseFactory(impl: DbEventDatabaseFactoryImpl): EventDatabaseFactory

    @Binds
    internal abstract fun bindEventLocalDataSource(impl: EventLocalDataSourceImpl): EventLocalDataSource

    @Binds
    internal abstract fun bindEventUpSyncScopeRepository(impl: EventUpSyncScopeRepositoryImpl): EventUpSyncScopeRepository

    @Binds
    internal abstract fun bindEventDownSyncScopeRepository(impl: EventDownSyncScopeRepositoryImpl): EventDownSyncScopeRepository

    @Binds
    internal abstract fun bindEventRepositoryImpl(impl: EventRepositoryImpl): EventRepository

}

@Module
@InstallIn(ActivityComponent::class)
internal class EventSystemProvider {

    @Provides
    @Singleton
    fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideWritableNonCancelableDispatcher(): CoroutineContext =
        provideIODispatcher() + NonCancellable

}
