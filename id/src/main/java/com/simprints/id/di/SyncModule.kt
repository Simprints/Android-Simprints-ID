package com.simprints.id.di

import android.content.Context
import com.simprints.id.data.db.people_sync.PeopleSyncStatusDatabase
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepositoryImpl
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperationFactory
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperationFactoryImpl
import com.simprints.id.data.db.people_sync.down.local.PeopleDownSyncOperationLocalDataSource
import com.simprints.id.data.db.people_sync.up.PeopleUpSyncScopeRepository
import com.simprints.id.data.db.people_sync.up.PeopleUpSyncScopeRepositoryImpl
import com.simprints.id.data.db.people_sync.up.local.PeopleUpSyncOperationLocalDataSource
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.secure.EncryptedSharedPreferencesBuilder
import com.simprints.id.services.scheduledSync.SyncManager
import com.simprints.id.services.scheduledSync.SyncSchedulerImpl
import com.simprints.id.services.scheduledSync.imageUpSync.ImageUpSyncScheduler
import com.simprints.id.services.scheduledSync.people.down.controllers.PeopleDownSyncWorkersBuilder
import com.simprints.id.services.scheduledSync.people.down.controllers.PeopleDownSyncWorkersBuilderImpl
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderTask
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderTaskImpl
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncManager
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncManagerImpl
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncStateProcessor
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncStateProcessorImpl
import com.simprints.id.services.scheduledSync.people.master.internal.PeopleSyncCache
import com.simprints.id.services.scheduledSync.people.master.internal.PeopleSyncCache.Companion.FILENAME_FOR_LAST_SYNC_TIME_SHARED_PREFS
import com.simprints.id.services.scheduledSync.people.master.internal.PeopleSyncCache.Companion.FILENAME_FOR_PROGRESSES_SHARED_PREFS
import com.simprints.id.services.scheduledSync.people.master.internal.PeopleSyncCacheImpl
import com.simprints.id.services.scheduledSync.people.up.controllers.PeopleUpSyncExecutor
import com.simprints.id.services.scheduledSync.people.up.controllers.PeopleUpSyncExecutorImpl
import com.simprints.id.services.scheduledSync.people.up.controllers.PeopleUpSyncWorkersBuilder
import com.simprints.id.services.scheduledSync.people.up.controllers.PeopleUpSyncWorkersBuilderImpl
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManager
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManagerImpl
import com.simprints.id.tools.TimeHelper
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
open class SyncModule {

    @Provides
    @Singleton
    open fun provideDownSyncScopeRepository(loginInfoManager: LoginInfoManager,
                                            preferencesManager: PreferencesManager,
                                            syncStatusDatabase: PeopleSyncStatusDatabase,
                                            peopleDownSyncOperationFactory: PeopleDownSyncOperationFactory): PeopleDownSyncScopeRepository =
        PeopleDownSyncScopeRepositoryImpl(loginInfoManager, preferencesManager, syncStatusDatabase.downSyncOperationOperationDataSource, peopleDownSyncOperationFactory)

    @Provides
    open fun providePeopleDownSyncOperationBuilder(): PeopleDownSyncOperationFactory = PeopleDownSyncOperationFactoryImpl()

    @Provides
    open fun providePeopleDownSyncDownloaderTask(personLocalDataSource: PersonLocalDataSource,
                                                 personRemoteDataSource: PersonRemoteDataSource,
                                                 downSyncScopeRepository: PeopleDownSyncScopeRepository,
                                                 peopleSyncCache: PeopleSyncCache,
                                                 timeHelper: TimeHelper): PeopleDownSyncDownloaderTask =
        PeopleDownSyncDownloaderTaskImpl(personLocalDataSource, personRemoteDataSource, downSyncScopeRepository, peopleSyncCache, timeHelper)

    @Provides
    open fun provideSessionEventsSyncManager(): SessionEventsSyncManager =
        SessionEventsSyncManagerImpl()


    @Provides
    open fun providePeopleSyncStateProcessor(ctx: Context,
                                             peopleSyncCache: PeopleSyncCache,
                                             personRepository: PersonRepository): PeopleSyncStateProcessor =
        PeopleSyncStateProcessorImpl(ctx, personRepository, peopleSyncCache)

    @Provides
    open fun providePeopleSyncManager(ctx: Context,
                                      peopleSyncStateProcessor: PeopleSyncStateProcessor,
                                      peopleUpSyncScopeRepository: PeopleUpSyncScopeRepository,
                                      peopleDownSyncScopeRepository: PeopleDownSyncScopeRepository,
                                      peopleSyncCache: PeopleSyncCache): PeopleSyncManager =
        PeopleSyncManagerImpl(ctx, peopleSyncStateProcessor, peopleUpSyncScopeRepository, peopleDownSyncScopeRepository, peopleSyncCache)

    @Provides
    open fun provideSyncManager(
        sessionEventsSyncManager: SessionEventsSyncManager,
        peopleSyncManager: PeopleSyncManager,
        imageUpSyncScheduler: ImageUpSyncScheduler
    ): SyncManager = SyncSchedulerImpl(
        sessionEventsSyncManager,
        peopleSyncManager,
        imageUpSyncScheduler
    )

    @Provides
    open fun provideDownSyncWorkerBuilder(downSyncScopeRepository: PeopleDownSyncScopeRepository): PeopleDownSyncWorkersBuilder =
        PeopleDownSyncWorkersBuilderImpl(downSyncScopeRepository)


    @Provides
    open fun providePeopleUpSyncWorkerBuilder(): PeopleUpSyncWorkersBuilder =
        PeopleUpSyncWorkersBuilderImpl()

    @Provides
    open fun providePeopleUpSyncDao(database: PeopleSyncStatusDatabase): PeopleUpSyncOperationLocalDataSource =
        database.upSyncOperationLocalDataSource

    @Provides
    open fun providePeopleDownSyncDao(database: PeopleSyncStatusDatabase): PeopleDownSyncOperationLocalDataSource =
        database.downSyncOperationOperationDataSource

    @Provides
    open fun providePeopleUpSyncManager(ctx: Context,
                                        peopleUpSyncWorkersBuilder: PeopleUpSyncWorkersBuilder): PeopleUpSyncExecutor =
        PeopleUpSyncExecutorImpl(ctx, peopleUpSyncWorkersBuilder)


    @Provides
    open fun provideUpSyncScopeRepository(loginInfoManager: LoginInfoManager,
                                          operationLocalDataSource: PeopleUpSyncOperationLocalDataSource): PeopleUpSyncScopeRepository =
        PeopleUpSyncScopeRepositoryImpl(loginInfoManager, operationLocalDataSource)

    @Provides
    open fun providePeopleSyncProgressCache(builder: EncryptedSharedPreferencesBuilder): PeopleSyncCache =
        PeopleSyncCacheImpl(
            builder.buildEncryptedSharedPreferences(FILENAME_FOR_PROGRESSES_SHARED_PREFS),
            builder.buildEncryptedSharedPreferences(FILENAME_FOR_LAST_SYNC_TIME_SHARED_PREFS)
        )

}
