package com.simprints.id.di

import android.content.Context
import android.content.SharedPreferences
import com.lyft.kronos.AndroidClockFactory
import com.simprints.core.domain.workflow.WorkflowCacheClearer
import com.simprints.core.tools.coroutines.DefaultDispatcherProvider
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.core.tools.utils.SimNetworkUtils
import com.simprints.core.tools.utils.SimNetworkUtilsImpl
import com.simprints.eventsystem.EventSystemApplication
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.EventRepositoryImpl
import com.simprints.eventsystem.event.domain.validators.SessionEventValidatorsFactory
import com.simprints.eventsystem.event.domain.validators.SessionEventValidatorsFactoryImpl
import com.simprints.eventsystem.event.local.*
import com.simprints.eventsystem.event.remote.EventRemoteDataSource
import com.simprints.eventsystem.events_sync.down.EventDownSyncScopeRepository
import com.simprints.id.Application
import com.simprints.id.BuildConfig.VERSION_NAME
import com.simprints.id.activities.fetchguid.FetchGuidHelper
import com.simprints.id.activities.fetchguid.FetchGuidHelperImpl
import com.simprints.id.activities.qrcapture.tools.*
import com.simprints.id.exitformhandler.ExitFormHelper
import com.simprints.id.exitformhandler.ExitFormHelperImpl
import com.simprints.id.moduleselection.ModuleRepository
import com.simprints.id.moduleselection.ModuleRepositoryImpl
import com.simprints.id.orchestrator.EnrolmentHelper
import com.simprints.id.orchestrator.EnrolmentHelperImpl
import com.simprints.id.orchestrator.PersonCreationEventHelper
import com.simprints.id.orchestrator.PersonCreationEventHelperImpl
import com.simprints.id.orchestrator.cache.HotCache
import com.simprints.id.orchestrator.cache.HotCacheImpl
import com.simprints.id.orchestrator.cache.StepEncoder
import com.simprints.id.orchestrator.cache.StepEncoderImpl
import com.simprints.id.services.guidselection.GuidSelectionManager
import com.simprints.id.services.guidselection.GuidSelectionManagerImpl
import com.simprints.id.services.sync.events.down.EventDownSyncHelper
import com.simprints.id.tools.LocationManager
import com.simprints.id.tools.LocationManagerImpl
import com.simprints.id.tools.device.ConnectivityHelper
import com.simprints.id.tools.device.ConnectivityHelperImpl
import com.simprints.id.tools.device.DeviceManager
import com.simprints.id.tools.device.DeviceManagerImpl
import com.simprints.id.tools.extensions.deviceId
import com.simprints.id.tools.extensions.packageVersionName
import com.simprints.id.tools.time.KronosTimeHelperImpl
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import com.simprints.infra.login.LoginManager
import com.simprints.infra.security.SecurityManager
import com.simprints.infra.security.SecurityManager.Companion.GLOBAL_SHARED_PREFS_FILENAME
import dagger.Module
import dagger.Provides
import dagger.hilt.migration.DisableInstallInCheck
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

// TODO: Remove after hilt migration
@DisableInstallInCheck
@Module
open class AppModule {

    @Provides
    @Singleton
    fun provideContext(app: Application): Context = app

    @Provides
    open fun provideSessionDataCache(app: EventSystemApplication): SessionDataCache =
        SessionDataCacheImpl(app)

    @Provides
    open fun provideEventSystemApplication(): EventSystemApplication = EventSystemApplication()

    @Provides
    @Singleton
    open fun provideSimNetworkUtils(ctx: Context): SimNetworkUtils = SimNetworkUtilsImpl(ctx)

    @Provides
    @Singleton
    // https://github.com/lyft/Kronos-Android
    fun provideTimeHelper(app: Context): TimeHelper = KronosTimeHelperImpl(
        AndroidClockFactory.createKronosClock(
            app,
            requestTimeoutMs = TimeUnit.SECONDS.toMillis(60),
            minWaitTimeBetweenSyncMs = TimeUnit.MINUTES.toMillis(30),
            cacheExpirationMs = TimeUnit.MINUTES.toMillis(30)
        )
    )


    @Provides
    open fun provideSessionEventValidatorsBuilder(): SessionEventValidatorsFactory =
        SessionEventValidatorsFactoryImpl()

    @Provides
    open fun provideDbEventDatabaseFactory(
        ctx: Context,
        secureDataManager: SecurityManager,
    ): EventDatabaseFactory = DbEventDatabaseFactoryImpl(ctx, secureDataManager)

    @Provides
    @Singleton
    open fun provideSessionEventsLocalDbManager(
        factory: EventDatabaseFactory
    ): EventLocalDataSource = EventLocalDataSourceImpl(factory)

    @Provides
    @Singleton
    open fun provideEventRepository(
        ctx: Context,
        eventLocalDataSource: EventLocalDataSource,
        eventRemoteDataSource: EventRemoteDataSource,
        configManager: ConfigManager,
        loginManager: LoginManager,
        timeHelper: TimeHelper,
        validatorFactory: SessionEventValidatorsFactory,
        sessionDataCache: SessionDataCache
    ): EventRepository = EventRepositoryImpl(
        ctx.deviceId,
        ctx.packageVersionName,
        loginManager,
        eventLocalDataSource,
        eventRemoteDataSource,
        timeHelper,
        validatorFactory,
        VERSION_NAME,
        sessionDataCache,
        configManager,
    )

    @Provides
    fun provideModuleRepository(
        configManager: ConfigManager,
        enrolmentRecordManager: EnrolmentRecordManager,
        eventDownSyncScopeRepository: EventDownSyncScopeRepository
    ): ModuleRepository = ModuleRepositoryImpl(
        configManager, enrolmentRecordManager, eventDownSyncScopeRepository
    )

    @Provides
    open fun provideGuidSelectionManager(
        context: Context,
        loginManager: LoginManager,
        timeHelper: TimeHelper,
        eventRepository: EventRepository
    ): GuidSelectionManager = GuidSelectionManagerImpl(
        context.deviceId, loginManager, timeHelper, eventRepository
    )

    @Provides
    open fun provideExitFormHandler(): ExitFormHelper = ExitFormHelperImpl()

    @Provides
    open fun provideGuidFetchGuidHelper(
        downSyncHelper: EventDownSyncHelper,
        enrolmentRecordManager: EnrolmentRecordManager,
        configManager: ConfigManager
    ): FetchGuidHelper = FetchGuidHelperImpl(
        downSyncHelper, enrolmentRecordManager, configManager
    )

    @Provides
    open fun provideDeviceManager(connectivityHelper: ConnectivityHelper): DeviceManager =
        DeviceManagerImpl(connectivityHelper)

    @Provides
    open fun provideConnectivityHelper(ctx: Context): ConnectivityHelper =
        ConnectivityHelperImpl(ctx)

    @Provides
    open fun provideLocationManager(ctx: Context): LocationManager = LocationManagerImpl(ctx)

    @Provides
    open fun provideCameraHelper(
        context: Context, previewBuilder: QrPreviewBuilder, cameraFocusManager: CameraFocusManager
    ): CameraHelper = CameraHelperImpl(context, previewBuilder, cameraFocusManager)

    @Provides
    open fun provideCameraFocusManager(): CameraFocusManager = CameraFocusManagerImpl()

    @Provides
    open fun provideQrPreviewBuilder(): QrPreviewBuilder = QrPreviewBuilderImpl()

    @Provides
    @ExperimentalCoroutinesApi
    open fun provideQrCodeProducer(
        qrCodeDetector: QrCodeDetector,
    ): QrCodeProducer = QrCodeProducerImpl(qrCodeDetector)

    @Provides
    open fun provideQrCodeDetector(): QrCodeDetector = QrCodeDetectorImpl()


    @Provides
    @Named("EncryptedSharedPreferences")
    open fun provideEncryptedSharedPreferences(builder: SecurityManager): SharedPreferences =
        builder.buildEncryptedSharedPreferences(GLOBAL_SHARED_PREFS_FILENAME)

    @Provides
    fun provideHotCache(
        @Named("EncryptedSharedPreferences") sharedPrefs: SharedPreferences,
        stepEncoder: StepEncoder
    ): HotCache = HotCacheImpl(sharedPrefs, stepEncoder)

    @Provides
    fun provWorkflowCacheClearer(
        @Named("EncryptedSharedPreferences") sharedPrefs: SharedPreferences,
        stepEncoder: StepEncoder
    ): WorkflowCacheClearer = HotCacheImpl(sharedPrefs, stepEncoder)

    @Provides
    fun provideStepEncoder(): StepEncoder = StepEncoderImpl()

    @Provides
    fun provideEnrolmentHelper(
        enrolmentRecordManager: EnrolmentRecordManager,
        eventRepository: EventRepository,
        timeHelper: TimeHelper
    ): EnrolmentHelper = EnrolmentHelperImpl(enrolmentRecordManager, eventRepository, timeHelper)

    @Provides
    fun providePersonCreationEventHelper(
        eventRepository: EventRepository, timeHelper: TimeHelper, encodingUtils: EncodingUtils
    ): PersonCreationEventHelper =
        PersonCreationEventHelperImpl(eventRepository, timeHelper, encodingUtils)

    @Provides
    open fun provideDispatcher(): DispatcherProvider = DefaultDispatcherProvider()

}

