package com.simprints.infra.eventsync

import android.content.Context
import com.simprints.infra.events.event.local.*
import com.simprints.infra.events.local.*
import com.simprints.infra.eventsync.event.remote.EventRemoteDataSource
import com.simprints.infra.eventsync.event.remote.EventRemoteDataSourceImpl
import com.simprints.infra.eventsync.status.EventSyncStatusDatabase
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepositoryImpl
import com.simprints.infra.eventsync.status.down.local.DbEventDownSyncOperationStateDao
import com.simprints.infra.eventsync.status.up.EventUpSyncScopeRepository
import com.simprints.infra.eventsync.status.up.EventUpSyncScopeRepositoryImpl
import com.simprints.infra.eventsync.status.up.local.DbEventUpSyncOperationStateDao
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import com.simprints.infra.eventsync.sync.common.EventSyncCacheImpl
import com.simprints.infra.eventsync.sync.common.SyncWorkersLiveDataProvider
import com.simprints.infra.eventsync.sync.common.SyncWorkersLiveDataProviderImpl
import com.simprints.infra.eventsync.sync.down.EventDownSyncHelper
import com.simprints.infra.eventsync.sync.down.EventDownSyncHelperImpl
import com.simprints.infra.eventsync.sync.down.SubjectFactory
import com.simprints.infra.eventsync.sync.down.SubjectFactoryImpl
import com.simprints.infra.eventsync.sync.up.EventUpSyncHelper
import com.simprints.infra.eventsync.sync.up.EventUpSyncHelperImpl
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
    internal abstract fun bindEventRemoteDataSource(impl: EventRemoteDataSourceImpl): EventRemoteDataSource


    @Binds
    internal abstract fun bindEventUpSyncScopeRepository(impl: EventUpSyncScopeRepositoryImpl): EventUpSyncScopeRepository

    @Binds
    internal abstract fun bindEventDownSyncScopeRepository(impl: EventDownSyncScopeRepositoryImpl): EventDownSyncScopeRepository

    @Binds
    internal abstract fun bindEventSyncRepositoryImpl(impl: EventSyncRepositoryImpl): EventSyncRepository

    @Binds
    abstract fun provideEventUpSyncHelper(impl: EventUpSyncHelperImpl): EventUpSyncHelper

    @Binds
    abstract fun provideSubjectFactory(impl: SubjectFactoryImpl): SubjectFactory

    @Binds
    abstract fun provideEventDownSyncHelper(impl: EventDownSyncHelperImpl): EventDownSyncHelper

    @Binds
    abstract fun provideEventSyncCache(impl: EventSyncCacheImpl): EventSyncCache

    @Binds
    abstract fun provideSyncWorkersLiveDataProvider(impl: SyncWorkersLiveDataProviderImpl): SyncWorkersLiveDataProvider

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
