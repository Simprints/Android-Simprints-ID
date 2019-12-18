package com.simprints.id.commontesttools.di

import android.content.Context
import com.simprints.id.data.db.people_sync.PeopleSyncStatusDatabase
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.people_sync.down.local.PeopleDownSyncDao
import com.simprints.id.data.db.people_sync.up.PeopleUpSyncScopeRepository
import com.simprints.id.data.db.people_sync.up.local.PeopleUpSyncDao
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.SyncModule
import com.simprints.id.services.scheduledSync.SyncManager
import com.simprints.id.services.scheduledSync.people.down.controllers.PeopleDownSyncWorkersBuilder
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderTask
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncManager
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncStateProcessor
import com.simprints.id.services.scheduledSync.people.up.controllers.PeopleUpSyncManager
import com.simprints.id.services.scheduledSync.people.up.controllers.PeopleUpSyncWorkersBuilder
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManager
import com.simprints.id.tools.TimeHelper
import com.simprints.testtools.common.di.DependencyRule

class TestSyncModule(private val peopleDownSyncScopeRepositoryRule: DependencyRule = DependencyRule.RealRule,
                     private val peopleDownSyncDownloaderTaskRule: DependencyRule = DependencyRule.RealRule,
                     private val peopleSessionEventsSyncManager: DependencyRule = DependencyRule.RealRule,
                     private val peopleSyncStateProcessor: DependencyRule = DependencyRule.RealRule,
                     private val peopleSyncManagerRule: DependencyRule = DependencyRule.RealRule,
                     private val syncManagerRule: DependencyRule = DependencyRule.RealRule,
                     private val peopleDownSyncWorkersBuilderRule: DependencyRule = DependencyRule.RealRule,
                     private val peopleUpSyncWorkersBuilderRule: DependencyRule = DependencyRule.RealRule,
                     private val peopleUpSyncDaoRule: DependencyRule = DependencyRule.RealRule,
                     private val peopleDownSyncDaoRule: DependencyRule = DependencyRule.RealRule,
                     private val peopleUpSyncManagerRule: DependencyRule = DependencyRule.RealRule,
                     private val peopleUpSyncScopeRepositoryRule: DependencyRule = DependencyRule.RealRule) : SyncModule() {

    override fun provideDownSyncScopeRepository(loginInfoManager: LoginInfoManager,
                                                preferencesManager: PreferencesManager,
                                                syncStatusDatabase: PeopleSyncStatusDatabase): PeopleDownSyncScopeRepository =
        peopleDownSyncScopeRepositoryRule.resolveDependency { super.provideDownSyncScopeRepository(loginInfoManager, preferencesManager, syncStatusDatabase) }

    override fun providePeopleDownSyncDownloaderTask(personLocalDataSource: PersonLocalDataSource,
                                                     personRemoteDataSource: PersonRemoteDataSource,
                                                     downSyncScopeRepository: PeopleDownSyncScopeRepository,
                                                     timeHelper: TimeHelper): PeopleDownSyncDownloaderTask =
        peopleDownSyncDownloaderTaskRule.resolveDependency { super.providePeopleDownSyncDownloaderTask(personLocalDataSource, personRemoteDataSource, downSyncScopeRepository, timeHelper) }


    override fun provideSessionEventsSyncManager(): SessionEventsSyncManager =
        peopleSessionEventsSyncManager.resolveDependency { super.provideSessionEventsSyncManager() }

    override fun providePeopleSyncStateProcessor(ctx: Context,
                                                 personRepository: PersonRepository): PeopleSyncStateProcessor =
        peopleSyncStateProcessor.resolveDependency { super.providePeopleSyncStateProcessor(ctx, personRepository) }


    override fun providePeopleSyncManager(ctx: Context,
                                          peopleSyncStateProcessor: PeopleSyncStateProcessor): PeopleSyncManager =
        peopleSyncManagerRule.resolveDependency { super.providePeopleSyncManager(ctx, peopleSyncStateProcessor) }

    override fun provideSyncManager(preferencesManager: PreferencesManager,
                                    sessionEventsSyncManager: SessionEventsSyncManager,
                                    peopleSyncManager: PeopleSyncManager,
                                    peopleUpSyncDao: PeopleUpSyncDao,
                                    peopleDownSyncDao: PeopleDownSyncDao): SyncManager =
        syncManagerRule.resolveDependency { super.provideSyncManager(preferencesManager, sessionEventsSyncManager, peopleSyncManager, peopleUpSyncDao, peopleDownSyncDao) }

    override fun provideDownSyncWorkerBuilder(downSyncScopeRepository: PeopleDownSyncScopeRepository): PeopleDownSyncWorkersBuilder =
        peopleDownSyncWorkersBuilderRule.resolveDependency { super.provideDownSyncWorkerBuilder(downSyncScopeRepository) }

    override fun providePeopleUpSyncWorkerBuilder(): PeopleUpSyncWorkersBuilder =
        peopleUpSyncWorkersBuilderRule.resolveDependency { super.providePeopleUpSyncWorkerBuilder() }

    override fun providePeopleUpSyncDao(database: PeopleSyncStatusDatabase): PeopleUpSyncDao =
        peopleUpSyncDaoRule.resolveDependency { super.providePeopleUpSyncDao(database) }

    override fun providePeopleDownSyncDao(database: PeopleSyncStatusDatabase): PeopleDownSyncDao =
        peopleDownSyncDaoRule.resolveDependency { super.providePeopleDownSyncDao(database) }

    override fun providePeopleUpSyncManager(ctx: Context,
                                            peopleUpSyncWorkersBuilder: PeopleUpSyncWorkersBuilder): PeopleUpSyncManager =
        peopleUpSyncManagerRule.resolveDependency { super.providePeopleUpSyncManager(ctx, peopleUpSyncWorkersBuilder) }

    override fun provideUpSyncScopeRepository(loginInfoManager: LoginInfoManager,
                                              dao: PeopleUpSyncDao): PeopleUpSyncScopeRepository =
        peopleUpSyncScopeRepositoryRule.resolveDependency { super.provideUpSyncScopeRepository(loginInfoManager, dao) }

}
