package com.simprints.infra.eventsync

import android.content.Context
import com.simprints.infra.eventsync.status.EventSyncStatusDatabase
import com.simprints.infra.eventsync.status.down.local.DbEventDownSyncOperationStateDao
import com.simprints.infra.eventsync.status.up.local.DbEventUpSyncOperationStateDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module(
    includes = [
        EventSyncProvider::class,
    ],
)
@InstallIn(SingletonComponent::class)
abstract class EventSyncModule {
    @Binds
    internal abstract fun provideEventSyncManager(impl: EventSyncManagerImpl): EventSyncManager
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
    fun provideEventsSyncStatusDatabase(
        @ApplicationContext ctx: Context,
    ): EventSyncStatusDatabase = EventSyncStatusDatabase.getDatabase(ctx)
}
