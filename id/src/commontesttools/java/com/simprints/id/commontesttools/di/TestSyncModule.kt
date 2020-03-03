package com.simprints.id.commontesttools.di

import android.content.Context
import androidx.work.WorkManager
import com.simprints.id.data.db.people_sync.PeopleSyncStatusDatabase
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperationFactory
import com.simprints.id.data.db.people_sync.down.local.PeopleDownSyncOperationLocalDataSource
import com.simprints.id.data.db.people_sync.up.PeopleUpSyncScopeRepository
import com.simprints.id.data.db.people_sync.up.local.PeopleUpSyncOperationLocalDataSource
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.SyncModule
import com.simprints.id.services.scheduledSync.SyncManager
import com.simprints.id.services.scheduledSync.imageUpSync.ImageUpSyncScheduler
import com.simprints.id.services.scheduledSync.people.down.controllers.PeopleDownSyncWorkersBuilder
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderTask
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncManager
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncStateProcessor
import com.simprints.id.services.scheduledSync.people.master.internal.PeopleSyncCache
import com.simprints.id.services.scheduledSync.people.up.controllers.PeopleUpSyncExecutor
import com.simprints.id.services.scheduledSync.people.up.controllers.PeopleUpSyncWorkersBuilder
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManager
import com.simprints.id.tools.TimeHelper
import com.simprints.testtools.common.di.DependencyRule
import javax.inject.Singleton

class TestSyncModule(
    private val peopleDownSyncScopeRepositoryRule: DependencyRule = DependencyRule.RealRule,
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
    private val peopleUpSyncScopeRepositoryRule: DependencyRule = DependencyRule.RealRule
) : SyncModule() {

    @Singleton
    override fun provideDownSyncScopeRepository(
        loginInfoManager: LoginInfoManager,
        preferencesManager: PreferencesManager,
        syncStatusDatabase: PeopleSyncStatusDatabase,
        peopleDownSyncOperationFactory: PeopleDownSyncOperationFactory
    ): PeopleDownSyncScopeRepository = peopleDownSyncScopeRepositoryRule.resolveDependency {
        super.provideDownSyncScopeRepository(
            loginInfoManager,
            preferencesManager,
            syncStatusDatabase,
            peopleDownSyncOperationFactory)
    }

    @Singleton
    override fun providePeopleDownSyncDownloaderTask(
        personLocalDataSource: PersonLocalDataSource,
        personRemoteDataSource: PersonRemoteDataSource,
        downSyncScopeRepository: PeopleDownSyncScopeRepository,
        peopleSyncCache: PeopleSyncCache,
        timeHelper: TimeHelper
    ): PeopleDownSyncDownloaderTask = peopleDownSyncDownloaderTaskRule.resolveDependency {
        super.providePeopleDownSyncDownloaderTask(
            personLocalDataSource,
            personRemoteDataSource,
            downSyncScopeRepository,
            peopleSyncCache,
            timeHelper
        )
    }

    @Singleton
    override fun provideSessionEventsSyncManager(workManager: WorkManager): SessionEventsSyncManager =
        peopleSessionEventsSyncManager.resolveDependency { super.provideSessionEventsSyncManager(workManager) }

    @Singleton
    override fun providePeopleSyncStateProcessor(
        ctx: Context,
        peopleSyncCache: PeopleSyncCache,
        personRepository: PersonRepository
    ): PeopleSyncStateProcessor = peopleSyncStateProcessor.resolveDependency {
        super.providePeopleSyncStateProcessor(ctx, peopleSyncCache, personRepository)
    }

    @Singleton
    override fun providePeopleSyncManager(
        ctx: Context,
        peopleSyncStateProcessor: PeopleSyncStateProcessor,
        peopleUpSyncScopeRepository: PeopleUpSyncScopeRepository,
        peopleDownSyncScopeRepository: PeopleDownSyncScopeRepository,
        peopleSyncCache: PeopleSyncCache
    ): PeopleSyncManager = peopleSyncManagerRule.resolveDependency {
        super.providePeopleSyncManager(ctx, peopleSyncStateProcessor, peopleUpSyncScopeRepository, peopleDownSyncScopeRepository, peopleSyncCache)
    }

    @Singleton
    override fun provideSyncManager(
        sessionEventsSyncManager: SessionEventsSyncManager,
        peopleSyncManager: PeopleSyncManager,
        imageUpSyncScheduler: ImageUpSyncScheduler
    ): SyncManager = syncManagerRule.resolveDependency {
        super.provideSyncManager(
            sessionEventsSyncManager,
            peopleSyncManager,
            imageUpSyncScheduler
        )
    }

    @Singleton
    override fun provideDownSyncWorkerBuilder(
        downSyncScopeRepository: PeopleDownSyncScopeRepository
    ): PeopleDownSyncWorkersBuilder = peopleDownSyncWorkersBuilderRule.resolveDependency {
        super.provideDownSyncWorkerBuilder(downSyncScopeRepository)
    }

    @Singleton
    override fun providePeopleUpSyncWorkerBuilder(): PeopleUpSyncWorkersBuilder =
        peopleUpSyncWorkersBuilderRule.resolveDependency {
            super.providePeopleUpSyncWorkerBuilder()
        }

    @Singleton
    override fun providePeopleUpSyncDao(database: PeopleSyncStatusDatabase): PeopleUpSyncOperationLocalDataSource =
        peopleUpSyncDaoRule.resolveDependency { super.providePeopleUpSyncDao(database) }

    @Singleton
    override fun providePeopleDownSyncDao(
        database: PeopleSyncStatusDatabase
    ): PeopleDownSyncOperationLocalDataSource = peopleDownSyncDaoRule.resolveDependency {
        super.providePeopleDownSyncDao(database)
    }

    @Singleton
    override fun providePeopleUpSyncManager(
        ctx: Context,
        peopleUpSyncWorkersBuilder: PeopleUpSyncWorkersBuilder
    ): PeopleUpSyncExecutor = peopleUpSyncManagerRule.resolveDependency {
        super.providePeopleUpSyncManager(ctx, peopleUpSyncWorkersBuilder)
    }

    @Singleton
    override fun provideUpSyncScopeRepository(
        loginInfoManager: LoginInfoManager,
        operationLocalDataSource: PeopleUpSyncOperationLocalDataSource
    ): PeopleUpSyncScopeRepository = peopleUpSyncScopeRepositoryRule.resolveDependency {
        super.provideUpSyncScopeRepository(loginInfoManager, operationLocalDataSource)
    }

}
