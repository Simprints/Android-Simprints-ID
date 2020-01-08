package com.simprints.id.di

import android.content.Context
import com.simprints.id.data.db.people_sync.PeopleSyncStatusDatabase
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepositoryImpl
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperationBuilder
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperationBuilderImpl
import com.simprints.id.data.db.people_sync.down.local.DbPeopleDownSyncOperationDao
import com.simprints.id.data.db.people_sync.up.PeopleUpSyncScopeRepository
import com.simprints.id.data.db.people_sync.up.PeopleUpSyncScopeRepositoryImpl
import com.simprints.id.data.db.people_sync.up.local.PeopleUpSyncDao
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.SyncManager
import com.simprints.id.services.scheduledSync.SyncSchedulerImpl
import com.simprints.id.services.scheduledSync.imageUpSync.ImageUpSyncScheduler
import com.simprints.id.services.scheduledSync.people.down.controllers.PeopleDownSyncWorkersBuilder
import com.simprints.id.services.scheduledSync.people.down.controllers.PeopleDownSyncWorkersBuilderImpl
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderTask
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderTaskImpl
import com.simprints.id.services.scheduledSync.people.master.*
import com.simprints.id.services.scheduledSync.people.up.controllers.PeopleUpSyncManager
import com.simprints.id.services.scheduledSync.people.up.controllers.PeopleUpSyncManagerImpl
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
                                            peopleDownSyncOperationBuilder: PeopleDownSyncOperationBuilder): PeopleDownSyncScopeRepository =
        PeopleDownSyncScopeRepositoryImpl(loginInfoManager, preferencesManager, syncStatusDatabase.downSyncOperationOperationDaoDb, peopleDownSyncOperationBuilder)

    @Provides
    open fun providePeopleDownSyncOperationBuilder(): PeopleDownSyncOperationBuilder = PeopleDownSyncOperationBuilderImpl()

    @Provides
    open fun providePeopleDownSyncDownloaderTask(personLocalDataSource: PersonLocalDataSource,
                                                 personRemoteDataSource: PersonRemoteDataSource,
                                                 downSyncScopeRepository: PeopleDownSyncScopeRepository,
                                                 progressCache: PeopleSyncProgressCache,
                                                 timeHelper: TimeHelper): PeopleDownSyncDownloaderTask =
        PeopleDownSyncDownloaderTaskImpl(personLocalDataSource, personRemoteDataSource, downSyncScopeRepository, progressCache, timeHelper)

    @Provides
    open fun provideSessionEventsSyncManager(): SessionEventsSyncManager =
        SessionEventsSyncManagerImpl()


    @Provides
    open fun providePeopleSyncStateProcessor(ctx: Context,
                                             progressCache: PeopleSyncProgressCache,
                                             personRepository: PersonRepository): PeopleSyncStateProcessor =
        PeopleSyncStateProcessorImpl(ctx, personRepository, progressCache)

    @Provides
    open fun providePeopleSyncManager(ctx: Context,
                                      peopleSyncStateProcessor: PeopleSyncStateProcessor): PeopleSyncManager =
        PeopleSyncManagerImpl(ctx, peopleSyncStateProcessor)

    @Provides
    open fun provideSyncManager(
        preferencesManager: PreferencesManager,
        sessionEventsSyncManager: SessionEventsSyncManager,
        peopleSyncManager: PeopleSyncManager,
        peopleUpSyncScopeRepository: PeopleUpSyncScopeRepository,
        peopleDownSyncScopeRepository: PeopleDownSyncScopeRepository,
        imageUpSyncScheduler: ImageUpSyncScheduler
    ): SyncManager = SyncSchedulerImpl(
        preferencesManager,
        sessionEventsSyncManager,
        peopleSyncManager,
        peopleUpSyncScopeRepository,
        peopleDownSyncScopeRepository,
        imageUpSyncScheduler
    )

    @Provides
    open fun provideDownSyncWorkerBuilder(downSyncScopeRepository: PeopleDownSyncScopeRepository): PeopleDownSyncWorkersBuilder =
        PeopleDownSyncWorkersBuilderImpl(downSyncScopeRepository)


    @Provides
    open fun providePeopleUpSyncWorkerBuilder(): PeopleUpSyncWorkersBuilder =
        PeopleUpSyncWorkersBuilderImpl()

    @Provides
    open fun providePeopleUpSyncDao(database: PeopleSyncStatusDatabase): PeopleUpSyncDao =
        database.upSyncDao

    @Provides
    open fun providePeopleDownSyncDao(database: PeopleSyncStatusDatabase): DbPeopleDownSyncOperationDao =
        database.downSyncOperationOperationDaoDb

    @Provides
    open fun providePeopleUpSyncManager(ctx: Context,
                                        peopleUpSyncWorkersBuilder: PeopleUpSyncWorkersBuilder): PeopleUpSyncManager =
        PeopleUpSyncManagerImpl(ctx, peopleUpSyncWorkersBuilder)


    @Provides
    open fun provideUpSyncScopeRepository(loginInfoManager: LoginInfoManager,
                                          dao: PeopleUpSyncDao): PeopleUpSyncScopeRepository =
        PeopleUpSyncScopeRepositoryImpl(loginInfoManager, dao)

    @Provides
    open fun providePeopleSyncProgressCache(ctx: Context): PeopleSyncProgressCache =
        PeopleSyncProgressCacheImpl(ctx)

}
