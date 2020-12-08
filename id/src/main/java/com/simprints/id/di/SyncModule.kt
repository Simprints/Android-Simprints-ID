package com.simprints.id.di

import android.content.Context
import androidx.work.WorkManager
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.data.db.events_sync.EventSyncStatusDatabase
import com.simprints.id.data.db.events_sync.down.EventDownSyncScopeRepository
import com.simprints.id.data.db.events_sync.down.EventDownSyncScopeRepositoryImpl
import com.simprints.id.data.db.events_sync.down.local.DbEventDownSyncOperationStateDao
import com.simprints.id.data.db.events_sync.up.EventUpSyncScopeRepository
import com.simprints.id.data.db.events_sync.up.EventUpSyncScopeRepositoryImpl
import com.simprints.id.data.db.events_sync.up.local.DbEventUpSyncOperationStateDao
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.domain.SubjectFactory
import com.simprints.id.data.db.subject.domain.SubjectFactoryImpl
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.secure.EncryptedSharedPreferencesBuilder
import com.simprints.id.services.sync.SyncManager
import com.simprints.id.services.sync.SyncSchedulerImpl
import com.simprints.id.services.sync.events.down.EventDownSyncHelper
import com.simprints.id.services.sync.events.down.EventDownSyncHelperImpl
import com.simprints.id.services.sync.events.down.EventDownSyncWorkersBuilder
import com.simprints.id.services.sync.events.down.EventDownSyncWorkersBuilderImpl
import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.id.services.sync.events.master.EventSyncManagerImpl
import com.simprints.id.services.sync.events.master.EventSyncStateProcessor
import com.simprints.id.services.sync.events.master.EventSyncStateProcessorImpl
import com.simprints.id.services.sync.events.master.internal.EventSyncCache
import com.simprints.id.services.sync.events.master.internal.EventSyncCache.Companion.FILENAME_FOR_LAST_SYNC_TIME_SHARED_PREFS
import com.simprints.id.services.sync.events.master.internal.EventSyncCache.Companion.FILENAME_FOR_PROGRESSES_SHARED_PREFS
import com.simprints.id.services.sync.events.master.internal.EventSyncCacheImpl
import com.simprints.id.services.sync.events.master.workers.EventSyncSubMasterWorkersBuilder
import com.simprints.id.services.sync.events.master.workers.EventSyncSubMasterWorkersBuilderImpl
import com.simprints.id.services.sync.events.up.EventUpSyncHelper
import com.simprints.id.services.sync.events.up.EventUpSyncHelperImpl
import com.simprints.id.services.sync.events.up.EventUpSyncWorkersBuilder
import com.simprints.id.services.sync.events.up.EventUpSyncWorkersBuilderImpl
import com.simprints.id.services.sync.images.up.ImageUpSyncScheduler
import com.simprints.id.tools.time.TimeHelper
import com.simprints.id.tools.utils.EncodingUtils
import com.simprints.id.tools.utils.EncodingUtilsImpl
import dagger.Module
import dagger.Provides

@Module
open class SyncModule {

    @Provides
    open fun provideWorkManager(ctx: Context): WorkManager =
        WorkManager.getInstance(ctx)

    @Provides
    open fun providePeopleSyncStateProcessor(ctx: Context,
                                             eventSyncCache: EventSyncCache): EventSyncStateProcessor =
        EventSyncStateProcessorImpl(ctx, eventSyncCache)

    @Provides
    open fun provideEventUpSyncScopeRepo(loginInfoManager: LoginInfoManager,
                                         dbEventUpSyncOperationStateDao: DbEventUpSyncOperationStateDao
    ): EventUpSyncScopeRepository = EventUpSyncScopeRepositoryImpl(loginInfoManager, dbEventUpSyncOperationStateDao)

    @Provides
    open fun providePeopleSyncManager(ctx: Context,
                                      eventSyncStateProcessor: EventSyncStateProcessor,
                                      downSyncScopeRepository: EventDownSyncScopeRepository,
                                      upSyncScopeRepo: EventUpSyncScopeRepository,
                                      eventSyncCache: EventSyncCache): EventSyncManager =
        EventSyncManagerImpl(ctx, eventSyncStateProcessor, downSyncScopeRepository, upSyncScopeRepo, eventSyncCache)

    @Provides
    open fun provideSyncManager(
        eventSyncManager: EventSyncManager,
        imageUpSyncScheduler: ImageUpSyncScheduler
    ): SyncManager = SyncSchedulerImpl(
        eventSyncManager,
        imageUpSyncScheduler
    )

    @Provides
    open fun provideEventDownSyncScopeRepo(
        loginInfoManager: LoginInfoManager,
        preferencesManager: PreferencesManager,
        downSyncOperationStateDao: DbEventDownSyncOperationStateDao
    ): EventDownSyncScopeRepository =
        EventDownSyncScopeRepositoryImpl(loginInfoManager, preferencesManager, downSyncOperationStateDao)

    @Provides
    open fun provideDownSyncWorkerBuilder(downSyncScopeRepository: EventDownSyncScopeRepository,
                                          jsonHelper: JsonHelper): EventDownSyncWorkersBuilder =
        EventDownSyncWorkersBuilderImpl(downSyncScopeRepository, jsonHelper)


    @Provides
    open fun providePeopleUpSyncWorkerBuilder(upSyncScopeRepository: EventUpSyncScopeRepository,
                                              jsonHelper: JsonHelper): EventUpSyncWorkersBuilder =
        EventUpSyncWorkersBuilderImpl(upSyncScopeRepository, jsonHelper)

    @Provides
    open fun providePeopleUpSyncDao(database: EventSyncStatusDatabase): DbEventUpSyncOperationStateDao =
        database.upSyncOperationsDaoDb

    @Provides
    open fun providePeopleDownSyncDao(database: EventSyncStatusDatabase): DbEventDownSyncOperationStateDao =
        database.downSyncOperationsDao

    @Provides
    open fun providePeopleSyncProgressCache(builder: EncryptedSharedPreferencesBuilder): EventSyncCache =
        EventSyncCacheImpl(
            builder.buildEncryptedSharedPreferences(FILENAME_FOR_PROGRESSES_SHARED_PREFS),
            builder.buildEncryptedSharedPreferences(FILENAME_FOR_LAST_SYNC_TIME_SHARED_PREFS)
        )

    @Provides
    open fun provideEncodingUtils(): EncodingUtils = EncodingUtilsImpl()

    @Provides
    open fun provideSubjectFactory(encodingUtils: EncodingUtils): SubjectFactory =
        SubjectFactoryImpl(encodingUtils)

    @Provides
    open fun provideEventDownSyncHelper(subjectRepository: SubjectRepository,
                                        eventRepository: EventRepository,
                                        eventDownSyncScopeRepository: EventDownSyncScopeRepository,
                                        subjectFactory: SubjectFactory,
                                        preferencesManager: PreferencesManager,
                                        timeHelper: TimeHelper): EventDownSyncHelper =
        EventDownSyncHelperImpl(subjectRepository, eventRepository, eventDownSyncScopeRepository, subjectFactory, preferencesManager, timeHelper)

    @Provides
    open fun provideEventUpSyncHelper(eventRepository: EventRepository,
                                      eventUpSyncScopeRepo: EventUpSyncScopeRepository,
                                      timerHelper: TimeHelper): EventUpSyncHelper =
        EventUpSyncHelperImpl(eventRepository, eventUpSyncScopeRepo, timerHelper)

    @Provides
    open fun providePeopleSyncSubMasterWorkersBuilder(): EventSyncSubMasterWorkersBuilder =
        EventSyncSubMasterWorkersBuilderImpl()
}
