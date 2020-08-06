package com.simprints.id.commontesttools.di

import android.content.Context
import androidx.work.WorkManager
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subjects_sync.SubjectsSyncStatusDatabase
import com.simprints.id.data.db.subjects_sync.down.SubjectsDownSyncScopeRepository
import com.simprints.id.data.db.subjects_sync.down.domain.EventsDownSyncOperationFactory
import com.simprints.id.data.db.subjects_sync.down.local.EventDownSyncOperationLocalDataSource
import com.simprints.id.data.db.subjects_sync.up.SubjectsUpSyncScopeRepository
import com.simprints.id.data.db.subjects_sync.up.local.SubjectsUpSyncOperationLocalDataSource
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.SyncModule
import com.simprints.id.services.sync.SyncManager
import com.simprints.id.services.sync.images.up.ImageUpSyncScheduler
import com.simprints.id.services.sync.sessionSync.SessionEventsSyncManager
import com.simprints.id.services.sync.events.down.EventDownSyncWorkersBuilder
import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.id.services.sync.events.master.SubjectsSyncStateProcessor
import com.simprints.id.services.sync.events.master.internal.EventSyncCache
import com.simprints.id.services.sync.subjects.up.controllers.SubjectsUpSyncExecutor
import com.simprints.id.services.sync.events.up.EventUpSyncWorkersBuilder
import com.simprints.testtools.common.di.DependencyRule
import javax.inject.Singleton

class TestSyncModule(
    private val peopleDownSyncScopeRepositoryRule: DependencyRule = DependencyRule.RealRule,
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
        syncStatusDatabase: SubjectsSyncStatusDatabase,
        EventsDownSyncOperationFactory: EventsDownSyncOperationFactory
    ): SubjectsDownSyncScopeRepository = peopleDownSyncScopeRepositoryRule.resolveDependency {
        super.provideDownSyncScopeRepository(
            loginInfoManager,
            preferencesManager,
            syncStatusDatabase,
            EventsDownSyncOperationFactory)
    }

    @Singleton
    override fun provideSessionEventsSyncManager(workManager: WorkManager): SessionEventsSyncManager =
        peopleSessionEventsSyncManager.resolveDependency { super.provideSessionEventsSyncManager(workManager) }

    @Singleton
    override fun providePeopleSyncStateProcessor(
        ctx: Context,
        eventSyncCache: EventSyncCache,
        personRepository: SubjectRepository
    ): SubjectsSyncStateProcessor = peopleSyncStateProcessor.resolveDependency {
        super.providePeopleSyncStateProcessor(ctx, eventSyncCache, personRepository)
    }

    @Singleton
    override fun providePeopleSyncManager(
        ctx: Context,
        subjectsSyncStateProcessor: SubjectsSyncStateProcessor,
        subjectsUpSyncScopeRepository: SubjectsUpSyncScopeRepository,
        subjectsDownSyncScopeRepository: SubjectsDownSyncScopeRepository,
        eventSyncCache: EventSyncCache
    ): EventSyncManager = peopleSyncManagerRule.resolveDependency {
        super.providePeopleSyncManager(ctx, subjectsSyncStateProcessor, subjectsUpSyncScopeRepository, subjectsDownSyncScopeRepository, eventSyncCache)
    }

    @Singleton
    override fun provideSyncManager(
        sessionEventsSyncManager: SessionEventsSyncManager,
        eventSyncManager: EventSyncManager,
        imageUpSyncScheduler: ImageUpSyncScheduler
    ): SyncManager = syncManagerRule.resolveDependency {
        super.provideSyncManager(
            sessionEventsSyncManager,
            eventSyncManager,
            imageUpSyncScheduler
        )
    }

    @Singleton
    override fun provideDownSyncWorkerBuilder(
        downSyncScopeRepository: SubjectsDownSyncScopeRepository,
        jsonHelper: JsonHelper
    ): EventDownSyncWorkersBuilder = peopleDownSyncWorkersBuilderRule.resolveDependency {
        super.provideDownSyncWorkerBuilder(downSyncScopeRepository, jsonHelper)
    }

    @Singleton
    override fun providePeopleUpSyncWorkerBuilder(): EventUpSyncWorkersBuilder =
        peopleUpSyncWorkersBuilderRule.resolveDependency {
            super.providePeopleUpSyncWorkerBuilder()
        }

    @Singleton
    override fun providePeopleUpSyncDao(database: SubjectsSyncStatusDatabase): SubjectsUpSyncOperationLocalDataSource =
        peopleUpSyncDaoRule.resolveDependency { super.providePeopleUpSyncDao(database) }

    @Singleton
    override fun providePeopleDownSyncDao(
        database: SubjectsSyncStatusDatabase
    ): EventDownSyncOperationLocalDataSource = peopleDownSyncDaoRule.resolveDependency {
        super.providePeopleDownSyncDao(database)
    }

    @Singleton
    override fun providePeopleUpSyncManager(
        ctx: Context,
        subjectsUpSyncWorkersBuilder: EventUpSyncWorkersBuilder
    ): SubjectsUpSyncExecutor = peopleUpSyncManagerRule.resolveDependency {
        super.providePeopleUpSyncManager(ctx, subjectsUpSyncWorkersBuilder)
    }

    @Singleton
    override fun provideUpSyncScopeRepository(
        loginInfoManager: LoginInfoManager,
        operationLocalDataSource: SubjectsUpSyncOperationLocalDataSource
    ): SubjectsUpSyncScopeRepository = peopleUpSyncScopeRepositoryRule.resolveDependency {
        super.provideUpSyncScopeRepository(loginInfoManager, operationLocalDataSource)
    }

}
