package com.simprints.id.commontesttools.di

import android.content.Context
import com.simprints.id.data.db.people_sync.down.DownSyncScopeRepository
import com.simprints.id.data.db.people_sync.SyncStatusDatabase
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.SyncModule
import com.simprints.id.services.scheduledSync.SyncManager
import com.simprints.id.services.scheduledSync.sync.peopleDownSync.controllers.DownSyncManager
import com.simprints.id.services.scheduledSync.sync.peopleDownSync.workers.downsync.DownSyncTask
import com.simprints.id.services.scheduledSync.sync.peopleUpsync.PeopleUpSyncMaster
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManager
import com.simprints.id.tools.TimeHelper
import com.simprints.testtools.common.di.DependencyRule

class TestSyncModule(private val downSyncScopeRepositoryRule: DependencyRule = DependencyRule.RealRule,
                     private val downSyncTaskRule: DependencyRule = DependencyRule.RealRule,
                     private val peopleUpSyncMasterRule: DependencyRule = DependencyRule.RealRule,
                     private val sessionEventsSyncManagerRule: DependencyRule = DependencyRule.RealRule,
                     private val downSyncManagerRule: DependencyRule = DependencyRule.RealRule,
                     private val syncSchedulerHelper: DependencyRule = DependencyRule.RealRule) : SyncModule() {

    override fun provideDownSyncScopeRepository(loginInfoManager: LoginInfoManager,
                                                preferencesManager: PreferencesManager,
                                                syncStatusDatabase: SyncStatusDatabase): DownSyncScopeRepository =
        downSyncScopeRepositoryRule.resolveDependency { super.provideDownSyncScopeRepository(loginInfoManager, preferencesManager, syncStatusDatabase) }

    override fun provideDownSyncTask(personLocalDataSource: PersonLocalDataSource,
                                     personRemoteDataSource: PersonRemoteDataSource,
                                     downSyncScopeRepository: DownSyncScopeRepository,
                                     timeHelper: TimeHelper): DownSyncTask =
        downSyncTaskRule.resolveDependency { super.provideDownSyncTask(personLocalDataSource, personRemoteDataSource, downSyncScopeRepository, timeHelper) }


    override fun providePeopleUpSyncMaster(): PeopleUpSyncMaster =
        peopleUpSyncMasterRule.resolveDependency { super.providePeopleUpSyncMaster() }

    override fun provideScheduledSessionsSyncManager(): SessionEventsSyncManager =
        sessionEventsSyncManagerRule.resolveDependency {
            super.provideScheduledSessionsSyncManager()
        }


    override fun provideDownSyncManager(ctx: Context): DownSyncManager =
        downSyncManagerRule.resolveDependency { super.provideDownSyncManager(ctx) }

    override fun provideSyncSchedulerHelper(preferencesManager: PreferencesManager,
                                            loginInfoManager: LoginInfoManager,
                                            sessionEventsSyncManager: SessionEventsSyncManager,
                                            downSyncManager: DownSyncManager): SyncManager =
        syncSchedulerHelper.resolveDependency { super.provideSyncSchedulerHelper(preferencesManager, loginInfoManager, sessionEventsSyncManager, downSyncManager) }
}
