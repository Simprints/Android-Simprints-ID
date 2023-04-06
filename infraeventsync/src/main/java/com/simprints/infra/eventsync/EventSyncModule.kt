package com.simprints.infra.eventsync

import android.content.Context
import com.simprints.infra.events.event.local.*
import com.simprints.infra.events.local.*
import com.simprints.infra.eventsync.event.remote.EventRemoteDataSource
import com.simprints.infra.eventsync.event.remote.EventRemoteDataSourceImpl
import com.simprints.infra.eventsync.status.EventSyncStatusDatabase
import com.simprints.infra.eventsync.status.down.local.DbEventDownSyncOperationStateDao
import com.simprints.infra.eventsync.status.up.local.DbEventUpSyncOperationStateDao
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import com.simprints.infra.eventsync.sync.common.EventSyncCacheImpl
import com.simprints.infra.eventsync.sync.common.SyncWorkersLiveDataProvider
import com.simprints.infra.eventsync.sync.common.SyncWorkersLiveDataProviderImpl
import com.simprints.infra.eventsync.sync.down.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module(
    includes = [
        EventSyncProvider::class
    ]
)
@InstallIn(SingletonComponent::class)
abstract class EventSyncModule {

    @Binds
    internal abstract fun provideEventSyncManager(impl: EventSyncManagerImpl): EventSyncManager

    @Binds
    internal abstract fun bindEventRemoteDataSource(impl: EventRemoteDataSourceImpl): EventRemoteDataSource

    @Binds
    internal abstract fun provideEventSyncCache(impl: EventSyncCacheImpl): EventSyncCache

    @Binds
    internal abstract fun provideSyncWorkersLiveDataProvider(impl: SyncWorkersLiveDataProviderImpl): SyncWorkersLiveDataProvider
}

@Module
@InstallIn(SingletonComponent::class)
internal class EventSyncProvider {

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
    fun provideEventsSyncStatusDatabase(@ApplicationContext ctx: Context): EventSyncStatusDatabase = EventSyncStatusDatabase.getDatabase(ctx)
}
