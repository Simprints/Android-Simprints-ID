package com.simprints.id.di

import android.content.Context
import com.simprints.id.data.db.people_sync.down.DownSyncScopeRepositoryImpl
import com.simprints.id.data.db.people_sync.SyncStatusDatabase
import com.simprints.id.data.db.people_sync.down.DownSyncScopeRepository
import com.simprints.id.data.db.people_sync.down.local.PeopleDownSyncDao
import com.simprints.id.data.db.people_sync.up.local.PeopleUpSyncDao
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.SyncManager
import com.simprints.id.services.scheduledSync.SyncSchedulerImpl
import com.simprints.id.services.scheduledSync.people.down.controllers.PeopleDownSyncWorkersBuilder
import com.simprints.id.services.scheduledSync.people.down.controllers.PeopleDownSyncWorkersBuilderImpl
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderTask
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderTaskImpl
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncManager
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncManagerImpl
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncStateProcessor
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncStateProcessorImpl
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
                                            syncStatusDatabase: SyncStatusDatabase): DownSyncScopeRepository =
        DownSyncScopeRepositoryImpl(loginInfoManager, preferencesManager, syncStatusDatabase.downSyncOperationDao)

    @Provides
    open fun provideDownSyncTask(personLocalDataSource: PersonLocalDataSource,
                                 personRemoteDataSource: PersonRemoteDataSource,
                                 downSyncScopeRepository: DownSyncScopeRepository,
                                 timeHelper: TimeHelper): PeopleDownSyncDownloaderTask =
        PeopleDownSyncDownloaderTaskImpl(personLocalDataSource, personRemoteDataSource, downSyncScopeRepository, timeHelper)

    @Provides
    open fun provideScheduledSessionsSyncManager(): SessionEventsSyncManager =
        SessionEventsSyncManagerImpl()


    @Provides
    open fun providePeopleSyncStateProcessor(ctx: Context,
                                             personRepository: PersonRepository): PeopleSyncStateProcessor =
        PeopleSyncStateProcessorImpl(ctx, personRepository)

    @Provides
    open fun provideDownSyncManager(ctx: Context,
                                    peopleSyncStateProcessor: PeopleSyncStateProcessor): PeopleSyncManager =
        PeopleSyncManagerImpl(ctx, peopleSyncStateProcessor)

    @Provides
    open fun provideSyncSchedulerHelper(preferencesManager: PreferencesManager,
                                        sessionEventsSyncManager: SessionEventsSyncManager,
                                        peopleSyncManager: PeopleSyncManager,
                                        peopleUpSyncDao: PeopleUpSyncDao,
                                        peopleDownSyncDao: PeopleDownSyncDao): SyncManager =
        SyncSchedulerImpl(preferencesManager, sessionEventsSyncManager, peopleSyncManager, peopleUpSyncDao, peopleDownSyncDao)


    @Provides
    open fun provideDownSyncWorkerBuilder(downSyncScopeRepository: DownSyncScopeRepository): PeopleDownSyncWorkersBuilder =
        PeopleDownSyncWorkersBuilderImpl(downSyncScopeRepository)


    @Provides
    open fun providePeopleUpSyncWorkerBuilder(downSyncScopeRepository: DownSyncScopeRepository): PeopleUpSyncWorkersBuilder =
        PeopleUpSyncWorkersBuilderImpl()

    @Provides
    open fun providePeopleUpSyncDao(database: SyncStatusDatabase): PeopleUpSyncDao =
        database.upSyncDao

    @Provides
    open fun providePeopleDownSyncDao(database: SyncStatusDatabase): PeopleDownSyncDao =
        database.downSyncOperationDao

    @Provides
    open fun providePeopleUpSyncManager(ctx: Context,
                                        peopleUpSyncWorkersBuilder: PeopleUpSyncWorkersBuilder): PeopleUpSyncManager =
        PeopleUpSyncManagerImpl(ctx, peopleUpSyncWorkersBuilder)
}
