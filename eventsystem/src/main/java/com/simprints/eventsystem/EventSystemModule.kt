package com.simprints.eventsystem

import android.content.Context
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.EventRepositoryImpl
import com.simprints.eventsystem.event.domain.validators.SessionEventValidatorsFactory
import com.simprints.eventsystem.event.domain.validators.SessionEventValidatorsFactoryImpl
import com.simprints.eventsystem.event.local.*
import com.simprints.eventsystem.event.remote.EventRemoteDataSource
import com.simprints.eventsystem.event.remote.EventRemoteDataSourceImpl
import com.simprints.eventsystem.events_sync.EventSyncStatusDatabase
import com.simprints.eventsystem.events_sync.down.EventDownSyncScopeRepository
import com.simprints.eventsystem.events_sync.down.EventDownSyncScopeRepositoryImpl
import com.simprints.eventsystem.events_sync.down.local.DbEventDownSyncOperationStateDao
import com.simprints.eventsystem.events_sync.up.EventUpSyncScopeRepository
import com.simprints.eventsystem.events_sync.up.EventUpSyncScopeRepositoryImpl
import com.simprints.eventsystem.events_sync.up.local.DbEventUpSyncOperationStateDao
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


@Module(
    includes = [
        EventSystemProvider::class
    ]
)
@InstallIn(SingletonComponent::class)
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
@InstallIn(SingletonComponent::class)
internal class EventSystemProvider {

    @Provides
    @Singleton
    fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideWritableNonCancelableDispatcher(): CoroutineContext =
        provideIODispatcher() + NonCancellable

    @Provides
    @Singleton
    fun provideDbEventUpSyncOperationStateDao(database: EventSyncStatusDatabase): DbEventUpSyncOperationStateDao =
        database.upSyncOperationsDaoDb

    @Provides
    @Singleton
    fun provideDbEventDownSyncOperationStateDao(database: EventSyncStatusDatabase): DbEventDownSyncOperationStateDao =
        database.downSyncOperationsDao

    @Provides
    @Singleton
    fun provideEventsSyncStatusDatabase(ctx: Context): EventSyncStatusDatabase =
        EventSyncStatusDatabase.getDatabase(ctx)

}
