package com.simprints.id.di

import android.content.Context
import androidx.work.WorkManager
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subjects_sync.SubjectsSyncStatusDatabase
import com.simprints.id.data.db.subjects_sync.down.SubjectsDownSyncScopeRepository
import com.simprints.id.data.db.subjects_sync.down.SubjectsDownSyncScopeRepositoryImpl
import com.simprints.id.data.db.subjects_sync.down.domain.EventsDownSyncOperationFactory
import com.simprints.id.data.db.subjects_sync.down.domain.EventsDownSyncOperationFactoryImpl
import com.simprints.id.data.db.subjects_sync.down.local.EventsDownSyncOperationLocalDataSource
import com.simprints.id.data.db.subjects_sync.up.SubjectsUpSyncScopeRepository
import com.simprints.id.data.db.subjects_sync.up.SubjectsUpSyncScopeRepositoryImpl
import com.simprints.id.data.db.subjects_sync.up.local.SubjectsUpSyncOperationLocalDataSource
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.secure.EncryptedSharedPreferencesBuilder
import com.simprints.id.services.sync.SyncManager
import com.simprints.id.services.sync.SyncSchedulerImpl
import com.simprints.id.services.sync.imageUpSync.ImageUpSyncScheduler
import com.simprints.id.services.sync.sessionSync.SessionEventsSyncManager
import com.simprints.id.services.sync.sessionSync.SessionEventsSyncManagerImpl
import com.simprints.id.services.sync.subjects.down.controllers.SubjectsDownSyncWorkersBuilder
import com.simprints.id.services.sync.subjects.down.controllers.SubjectsDownSyncWorkersBuilderImpl
import com.simprints.id.services.sync.subjects.master.SubjectsSyncManager
import com.simprints.id.services.sync.subjects.master.SubjectsSyncManagerImpl
import com.simprints.id.services.sync.subjects.master.SubjectsSyncStateProcessor
import com.simprints.id.services.sync.subjects.master.SubjectsSyncStateProcessorImpl
import com.simprints.id.services.sync.subjects.master.internal.SubjectsSyncCache
import com.simprints.id.services.sync.subjects.master.internal.SubjectsSyncCache.Companion.FILENAME_FOR_LAST_SYNC_TIME_SHARED_PREFS
import com.simprints.id.services.sync.subjects.master.internal.SubjectsSyncCache.Companion.FILENAME_FOR_PROGRESSES_SHARED_PREFS
import com.simprints.id.services.sync.subjects.master.internal.SubjectsSyncCacheImpl
import com.simprints.id.services.sync.subjects.master.workers.SubjectsSyncSubMasterWorkersBuilder
import com.simprints.id.services.sync.subjects.master.workers.SubjectsSyncSubMasterWorkersBuilderImpl
import com.simprints.id.services.sync.subjects.up.controllers.SubjectsUpSyncExecutor
import com.simprints.id.services.sync.subjects.up.controllers.SubjectsUpSyncExecutorImpl
import com.simprints.id.services.sync.subjects.up.controllers.SubjectsUpSyncWorkersBuilder
import com.simprints.id.services.sync.subjects.up.controllers.SubjectsUpSyncWorkersBuilderImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
open class SyncModule {

    @Provides
    @Singleton
    open fun provideDownSyncScopeRepository(loginInfoManager: LoginInfoManager,
                                            preferencesManager: PreferencesManager,
                                            syncStatusDatabase: SubjectsSyncStatusDatabase,
                                            EventsDownSyncOperationFactory: EventsDownSyncOperationFactory): SubjectsDownSyncScopeRepository =
        SubjectsDownSyncScopeRepositoryImpl(loginInfoManager, preferencesManager, syncStatusDatabase.downSyncOperationOperationDataSource, EventsDownSyncOperationFactory)

    @Provides
    open fun providePeopleDownSyncOperationBuilder(): EventsDownSyncOperationFactory = EventsDownSyncOperationFactoryImpl()

    @Provides
    open fun provideWorkManager(ctx: Context): WorkManager =
        WorkManager.getInstance(ctx)

    @Provides
    open fun provideSessionEventsSyncManager(workManager: WorkManager): SessionEventsSyncManager =
        SessionEventsSyncManagerImpl(workManager)

    @Provides
    open fun providePeopleSyncStateProcessor(ctx: Context,
                                             subjectsSyncCache: SubjectsSyncCache,
                                             personRepository: SubjectRepository): SubjectsSyncStateProcessor =
        SubjectsSyncStateProcessorImpl(ctx, personRepository, subjectsSyncCache)

    @Provides
    open fun providePeopleSyncManager(ctx: Context,
                                      subjectsSyncStateProcessor: SubjectsSyncStateProcessor,
                                      subjectsUpSyncScopeRepository: SubjectsUpSyncScopeRepository,
                                      subjectsDownSyncScopeRepository: SubjectsDownSyncScopeRepository,
                                      subjectsSyncCache: SubjectsSyncCache): SubjectsSyncManager =
        SubjectsSyncManagerImpl(ctx, subjectsSyncStateProcessor, subjectsUpSyncScopeRepository, subjectsDownSyncScopeRepository, subjectsSyncCache)

    @Provides
    open fun provideSyncManager(
        sessionEventsSyncManager: SessionEventsSyncManager,
        subjectsSyncManager: SubjectsSyncManager,
        imageUpSyncScheduler: ImageUpSyncScheduler
    ): SyncManager = SyncSchedulerImpl(
        sessionEventsSyncManager,
        subjectsSyncManager,
        imageUpSyncScheduler
    )

    @Provides
    open fun provideDownSyncWorkerBuilder(downSyncScopeRepository: SubjectsDownSyncScopeRepository,
                                          jsonHelper: JsonHelper): SubjectsDownSyncWorkersBuilder =
        SubjectsDownSyncWorkersBuilderImpl(downSyncScopeRepository, jsonHelper)


    @Provides
    open fun providePeopleUpSyncWorkerBuilder(): SubjectsUpSyncWorkersBuilder =
        SubjectsUpSyncWorkersBuilderImpl()

    @Provides
    open fun providePeopleUpSyncDao(database: SubjectsSyncStatusDatabase): SubjectsUpSyncOperationLocalDataSource =
        database.upSyncOperationLocalDataSource

    @Provides
    open fun providePeopleDownSyncDao(database: SubjectsSyncStatusDatabase): EventsDownSyncOperationLocalDataSource =
        database.downSyncOperationOperationDataSource

    @Provides
    open fun providePeopleUpSyncManager(ctx: Context,
                                        subjectsUpSyncWorkersBuilder: SubjectsUpSyncWorkersBuilder): SubjectsUpSyncExecutor =
        SubjectsUpSyncExecutorImpl(ctx, subjectsUpSyncWorkersBuilder)


    @Provides
    open fun provideUpSyncScopeRepository(loginInfoManager: LoginInfoManager,
                                          operationLocalDataSource: SubjectsUpSyncOperationLocalDataSource): SubjectsUpSyncScopeRepository =
        SubjectsUpSyncScopeRepositoryImpl(loginInfoManager, operationLocalDataSource)

    @Provides
    open fun providePeopleSyncProgressCache(builder: EncryptedSharedPreferencesBuilder): SubjectsSyncCache =
        SubjectsSyncCacheImpl(
            builder.buildEncryptedSharedPreferences(FILENAME_FOR_PROGRESSES_SHARED_PREFS),
            builder.buildEncryptedSharedPreferences(FILENAME_FOR_LAST_SYNC_TIME_SHARED_PREFS)
        )

    @Provides
    open fun providePeopleSyncSubMasterWorkersBuilder(): SubjectsSyncSubMasterWorkersBuilder =
        SubjectsSyncSubMasterWorkersBuilderImpl()
}
