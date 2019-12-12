package com.simprints.id.di

import android.content.Context
import com.simprints.id.data.db.down_sync_info.DownSyncScopeRepository
import com.simprints.id.data.db.down_sync_info.DownSyncScopeRepositoryImpl
import com.simprints.id.data.db.down_sync_info.local.SyncStatusDatabase
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.SyncSchedulerHelper
import com.simprints.id.services.scheduledSync.SyncSchedulerHelperImpl
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.DownSyncManager
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.DownSyncManagerImpl
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
    @Singleton
    open fun provideDownSyncScopeRepository(loginInfoManager: LoginInfoManager,
                                            preferencesManager: PreferencesManager,
                                            syncStatusDatabase: SyncStatusDatabase): DownSyncScopeRepository =
        DownSyncScopeRepositoryImpl(loginInfoManager, preferencesManager, syncStatusDatabase.downSyncOperationDao)

    @Provides
    open fun provideDownSyncTask(personLocalDataSource: PersonLocalDataSource,
                                 personRemoteDataSource: PersonRemoteDataSource,
                                 downSyncScopeRepository: DownSyncScopeRepository,
                                 timeHelper: TimeHelper): DownSyncTask = DownSyncTaskImpl(personLocalDataSource, personRemoteDataSource, downSyncScopeRepository, timeHelper)


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
    open fun provideDownSyncManager(ctx: Context): DownSyncManager =
        DownSyncManagerImpl(ctx)

    @Provides
    @Singleton
    open fun provideSyncSchedulerHelper(preferencesManager: PreferencesManager,
                                        loginInfoManager: LoginInfoManager,
                                        sessionEventsSyncManager: SessionEventsSyncManager,
                                        downSyncManager: DownSyncManager): SyncSchedulerHelper =
        SyncSchedulerHelperImpl(preferencesManager, loginInfoManager, sessionEventsSyncManager, downSyncManager)
}
