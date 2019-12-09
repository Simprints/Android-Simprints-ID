package com.simprints.id.di

import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.data.db.syncinfo.local.SyncInfoLocalDataSource
import com.simprints.id.data.db.syncstatus.SyncStatusDatabase
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.SyncSchedulerHelper
import com.simprints.id.services.scheduledSync.SyncSchedulerHelperImpl
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.DownSyncManager
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.DownSyncManagerImpl
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilderImpl
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.downsync.DownSyncTask
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.downsync.DownSyncTaskImpl
import com.simprints.id.services.scheduledSync.peopleUpsync.PeopleUpSyncMaster
import com.simprints.id.services.scheduledSync.peopleUpsync.PeopleUpSyncMasterImpl
import com.simprints.id.services.scheduledSync.peopleUpsync.periodicFlusher.PeopleUpSyncPeriodicFlusherMaster
import com.simprints.id.services.scheduledSync.peopleUpsync.uploader.PeopleUpSyncUploaderMaster
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManager
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManagerImpl
import com.simprints.id.tools.TimeHelper
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
open class SyncModule {

    @Provides
    open fun provideDownSyncTask(personLocalDataSource: PersonLocalDataSource,
                                 syncInfoLocalDataSource: SyncInfoLocalDataSource,
                                 personRemoteDataSource: PersonRemoteDataSource,
                                 timeHelper: TimeHelper,
                                 syncStatusDatabase: SyncStatusDatabase): DownSyncTask = DownSyncTaskImpl(personLocalDataSource, syncInfoLocalDataSource, personRemoteDataSource, timeHelper, syncStatusDatabase.downSyncDao)


    @Provides
    @Singleton
    open fun providePeopleUpSyncMaster(): PeopleUpSyncMaster =
        PeopleUpSyncMasterImpl(
            PeopleUpSyncUploaderMaster(),
            PeopleUpSyncPeriodicFlusherMaster()
        )

    @Provides
    @Singleton
    open fun provideScheduledSessionsSyncManager(): SessionEventsSyncManager =
        SessionEventsSyncManagerImpl()

    @Provides
    @Singleton
    open fun provideSyncScopesBuilder(loginInfoManager: LoginInfoManager, preferencesManager: PreferencesManager): SyncScopesBuilder =
        SyncScopesBuilderImpl(loginInfoManager, preferencesManager)

    @Provides
    @Singleton
    open fun provideDownSyncManager(syncScopesBuilder: SyncScopesBuilder): DownSyncManager =
        DownSyncManagerImpl(syncScopesBuilder)

    @Provides
    @Singleton
    open fun provideSyncSchedulerHelper(preferencesManager: PreferencesManager,
                                        loginInfoManager: LoginInfoManager,
                                        sessionEventsSyncManager: SessionEventsSyncManager,
                                        downSyncManager: DownSyncManager): SyncSchedulerHelper =
        SyncSchedulerHelperImpl(preferencesManager, loginInfoManager, sessionEventsSyncManager, downSyncManager)

    @Provides
    open fun provideCountTask(personRepository: PersonRepository): CountTask = CountTaskImpl(personRepository)


}
