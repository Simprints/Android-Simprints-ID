package com.simprints.infra.events

import android.content.Context
import com.simprints.infra.events.domain.validators.SessionEventValidatorsFactory
import com.simprints.infra.events.domain.validators.SessionEventValidatorsFactoryImpl
import com.simprints.infra.events.event.local.*
import com.simprints.infra.events.event.local.DbEventDatabaseFactoryImpl
import com.simprints.infra.events.event.local.EventDatabaseFactory
import com.simprints.infra.events.event.local.EventLocalDataSource
import com.simprints.infra.events.event.local.EventLocalDataSourceImpl
import com.simprints.infra.events.event.local.SessionDataCacheImpl
import com.simprints.infra.events.local.*
import com.simprints.infra.events.remote.EventRemoteDataSource
import com.simprints.infra.events.remote.EventRemoteDataSourceImpl
import com.simprints.infra.events.events_sync.EventSyncStatusDatabase
import com.simprints.infra.events.events_sync.down.EventDownSyncScopeRepository
import com.simprints.infra.events.events_sync.down.EventDownSyncScopeRepositoryImpl
import com.simprints.infra.events.events_sync.down.local.DbEventDownSyncOperationStateDao
import com.simprints.infra.events.events_sync.up.EventUpSyncScopeRepository
import com.simprints.infra.events.events_sync.up.EventUpSyncScopeRepositoryImpl
import com.simprints.infra.events.events_sync.up.local.DbEventUpSyncOperationStateDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

    @Binds
    internal abstract fun bindEventSyncRepositoryImpl(impl: EventSyncRepositoryImpl): EventSyncRepository
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
    fun provideEventsSyncStatusDatabase(@ApplicationContext ctx: Context): EventSyncStatusDatabase =
        EventSyncStatusDatabase.getDatabase(ctx)
}
