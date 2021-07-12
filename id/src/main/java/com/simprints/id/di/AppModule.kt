package com.simprints.id.di

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.analytics.FirebaseAnalytics
import com.lyft.kronos.AndroidClockFactory
import com.simprints.core.analytics.CoreCrashReportManager
import com.simprints.core.analytics.CrashReportManager
import com.simprints.core.domain.modality.toMode
import com.simprints.core.login.LoginInfoManager
import com.simprints.core.network.SimApiClientFactory
import com.simprints.core.security.SecureLocalDbKeyProvider
import com.simprints.core.security.SecureLocalDbKeyProvider.Companion.FILENAME_FOR_REALM_KEY_SHARED_PREFS
import com.simprints.core.sharedpreferences.ImprovedSharedPreferences
import com.simprints.core.sharedpreferences.PreferencesManager
import com.simprints.core.sharedpreferences.RecentEventsPreferencesManager
import com.simprints.core.tools.coroutines.DefaultDispatcherProvider
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.core.tools.utils.SimNetworkUtils
import com.simprints.eventsystem.EventSystemApplication
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.EventRepositoryImpl
import com.simprints.eventsystem.event.domain.validators.SessionEventValidatorsFactory
import com.simprints.eventsystem.event.domain.validators.SessionEventValidatorsFactoryImpl
import com.simprints.eventsystem.event.local.DbEventDatabaseFactoryImpl
import com.simprints.eventsystem.event.local.EventDatabaseFactory
import com.simprints.eventsystem.event.local.EventLocalDataSource
import com.simprints.eventsystem.event.local.EventLocalDataSourceImpl
import com.simprints.eventsystem.event.local.SessionDataCache
import com.simprints.eventsystem.event.local.SessionDataCacheImpl
import com.simprints.eventsystem.event.remote.EventRemoteDataSource
import com.simprints.id.Application
import com.simprints.id.activities.fetchguid.FetchGuidHelper
import com.simprints.id.activities.fetchguid.FetchGuidHelperImpl
import com.simprints.id.activities.qrcapture.tools.CameraFocusManager
import com.simprints.id.activities.qrcapture.tools.CameraFocusManagerImpl
import com.simprints.id.activities.qrcapture.tools.CameraHelper
import com.simprints.id.activities.qrcapture.tools.CameraHelperImpl
import com.simprints.id.activities.qrcapture.tools.QrCodeDetector
import com.simprints.id.activities.qrcapture.tools.QrCodeDetectorImpl
import com.simprints.id.activities.qrcapture.tools.QrCodeProducer
import com.simprints.id.activities.qrcapture.tools.QrCodeProducerImpl
import com.simprints.id.activities.qrcapture.tools.QrPreviewBuilder
import com.simprints.id.activities.qrcapture.tools.QrPreviewBuilderImpl
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.AnalyticsManagerImpl
import com.simprints.id.data.analytics.crashreport.CrashReportManagerImpl
import com.simprints.id.data.db.common.FirebaseManagerImpl
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.project.local.ProjectLocalDataSource
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.loginInfo.LoginInfoManagerImpl
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.data.prefs.events.RecentEventsPreferencesManagerImpl
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.data.secure.EncryptedSharedPreferencesBuilder
import com.simprints.id.data.secure.EncryptedSharedPreferencesBuilderImpl
import com.simprints.id.data.secure.LegacyLocalDbKeyProvider
import com.simprints.id.data.secure.LegacyLocalDbKeyProviderImpl
import com.simprints.id.data.secure.SecureLocalDbKeyProviderImpl
import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.data.secure.keystore.KeystoreManagerImpl
import com.simprints.id.exitformhandler.ExitFormHelper
import com.simprints.id.exitformhandler.ExitFormHelperImpl
import com.simprints.id.moduleselection.ModuleRepository
import com.simprints.id.moduleselection.ModuleRepositoryImpl
import com.simprints.id.network.BaseUrlProvider
import com.simprints.id.network.BaseUrlProviderImpl
import com.simprints.id.network.SimApiClientFactoryImpl
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
import com.simprints.id.services.sync.images.up.ImageUpSyncScheduler
import com.simprints.id.services.sync.images.up.ImageUpSyncSchedulerImpl
import com.simprints.id.tools.LocationManager
import com.simprints.id.tools.LocationManagerImpl
import com.simprints.id.tools.RandomGenerator
import com.simprints.id.tools.RandomGeneratorImpl
import com.simprints.id.tools.device.ConnectivityHelper
import com.simprints.id.tools.device.ConnectivityHelperImpl
import com.simprints.id.tools.device.DeviceManager
import com.simprints.id.tools.device.DeviceManagerImpl
import com.simprints.id.tools.extensions.FirebasePerformanceTraceFactory
import com.simprints.id.tools.extensions.FirebasePerformanceTraceFactoryImpl
import com.simprints.id.tools.extensions.deviceId
import com.simprints.id.tools.extensions.packageVersionName
import com.simprints.id.tools.time.KronosTimeHelperImpl
import com.simprints.id.tools.utils.SimNetworkUtilsImpl
import com.simprints.libsimprints.BuildConfig.VERSION_NAME
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
open class AppModule {

    @Provides
    @Singleton
    fun provideContext(app: Application): Context = app

    @Provides
    @Singleton
    open fun provideRemoteDbManager(
        loginInfoManager: LoginInfoManager,
        context: Context
    ): RemoteDbManager = FirebaseManagerImpl(loginInfoManager, context)

    @Provides
    @Singleton
    open fun provideLoginInfoManager(improvedSharedPreferences: ImprovedSharedPreferences): LoginInfoManager =
        LoginInfoManagerImpl(improvedSharedPreferences)

    @Provides
    @Singleton
    @Suppress("MissingPermission")
    fun provideFirebaseAnalytics(app: Application): FirebaseAnalytics =
        FirebaseAnalytics.getInstance(app).apply {
            this.setSessionTimeoutDuration(0)
        }

    @Provides
    @Singleton
    open fun provideRecentEventsPreferencesManager(prefs: ImprovedSharedPreferences): RecentEventsPreferencesManager =
        RecentEventsPreferencesManagerImpl(prefs)

    @Provides
    open fun provideSessionDataCache(app: EventSystemApplication): SessionDataCache = SessionDataCacheImpl(app)

    @Provides
    open fun provideEventSystemApplication(): EventSystemApplication = EventSystemApplication()

    @Provides
    @Singleton
    open fun provideAnalyticsManager(
        loginInfoManager: LoginInfoManager,
        preferencesManager: PreferencesManager,
        firebaseAnalytics: FirebaseAnalytics
    ): AnalyticsManager = AnalyticsManagerImpl(loginInfoManager, firebaseAnalytics)

    @Provides
    @Singleton
    open fun provideCrashManager(): CrashReportManager = CrashReportManagerImpl()

    @Provides
    @Singleton
    open fun provideCoreCrashReportManager(crashReportManager: CrashReportManager): CoreCrashReportManager =
        crashReportManager

    @Provides
    @Singleton
    open fun provideKeystoreManager(): KeystoreManager = KeystoreManagerImpl()

    @Provides
    @Singleton
    open fun provideRandomGenerator(): RandomGenerator = RandomGeneratorImpl()

    @Provides
    @Singleton
    open fun provideSecureLocalDbKeyProvider(
        builder: EncryptedSharedPreferencesBuilder,
        randomGenerator: RandomGenerator,
        unsecuredLocalDbKeyProvider: LegacyLocalDbKeyProvider
    ): SecureLocalDbKeyProvider =
        SecureLocalDbKeyProviderImpl(
            builder.buildEncryptedSharedPreferences(FILENAME_FOR_REALM_KEY_SHARED_PREFS),
            randomGenerator,
            unsecuredLocalDbKeyProvider
        )

    @Provides
    @Singleton
    open fun provideLegacyLocalDbKeyProvider(
        preferencesManager: PreferencesManager,
        keystoreManager: KeystoreManager
    ): LegacyLocalDbKeyProvider =
        LegacyLocalDbKeyProviderImpl(keystoreManager, preferencesManager)

    @Provides
    @Singleton
    open fun provideSimNetworkUtils(ctx: Context): SimNetworkUtils = SimNetworkUtilsImpl(ctx)

    @Provides
    open fun provideBaseUrlProvider(
        settingsPreferencesManager: SettingsPreferencesManager,
        projectLocalDataSource: ProjectLocalDataSource,
        loginInfoManager: LoginInfoManager
    ): BaseUrlProvider = BaseUrlProviderImpl(
        settingsPreferencesManager,
        projectLocalDataSource,
        loginInfoManager
    )

    @Provides
    open fun provideFirebasePerformanceTraceFactory(): FirebasePerformanceTraceFactory =
        FirebasePerformanceTraceFactoryImpl()

    @Provides
    open fun provideSimApiClientFactory(
        ctx: Context,
        remoteDbManager: RemoteDbManager,
        baseUrlProvider: BaseUrlProvider,
        performanceTracer: FirebasePerformanceTraceFactory,
        jsonHelper: JsonHelper
    ): SimApiClientFactory = SimApiClientFactoryImpl(
        baseUrlProvider,
        ctx.deviceId,
        ctx.packageVersionName,
        remoteDbManager,
        performanceTracer,
        jsonHelper
    )

    @Provides
    @Singleton
    // https://github.com/lyft/Kronos-Android
    fun provideTimeHelper(app: Application): TimeHelper = KronosTimeHelperImpl(
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
        secureDataManager: SecureLocalDbKeyProvider,
        crashReportManager: CrashReportManager
    ): EventDatabaseFactory =
        DbEventDatabaseFactoryImpl(ctx, secureDataManager, crashReportManager)

    @Provides
    @Singleton
    open fun provideSessionEventsLocalDbManager(
        factory: EventDatabaseFactory
    ): EventLocalDataSource =
        EventLocalDataSourceImpl(factory)

    @Provides
    @Singleton
    open fun provideEventRepository(
        ctx: Context,
        eventLocalDataSource: EventLocalDataSource,
        eventRemoteDataSource: EventRemoteDataSource,
        idPreferencesManager: IdPreferencesManager,
        loginInfoManager: LoginInfoManager,
        timeHelper: TimeHelper,
        crashReportManager: CrashReportManager,
        validatorFactory: SessionEventValidatorsFactory,
        sessionDataCache: SessionDataCache
    ): EventRepository =
        EventRepositoryImpl(
            ctx.deviceId,
            ctx.packageVersionName,
            loginInfoManager,
            eventLocalDataSource,
            eventRemoteDataSource,
            crashReportManager,
            timeHelper,
            validatorFactory,
            VERSION_NAME,
            sessionDataCache,
            idPreferencesManager.language,
            idPreferencesManager.modalities.map { it.toMode() }
        )

    @Provides
    fun provideModuleRepository(
        preferencesManager: IdPreferencesManager,
        crashReportManager: CrashReportManager,
        subjectRepository: SubjectRepository
    ): ModuleRepository = ModuleRepositoryImpl(
        preferencesManager,
        crashReportManager,
        subjectRepository
    )

    @Provides
    open fun provideGuidSelectionManager(
        context: Context,
        loginInfoManager: LoginInfoManager,
        analyticsManager: AnalyticsManager,
        crashReportManager: CrashReportManager,
        timeHelper: TimeHelper,
        eventRepository: EventRepository
    ): GuidSelectionManager =
        GuidSelectionManagerImpl(
            context.deviceId,
            loginInfoManager,
            analyticsManager,
            crashReportManager,
            timeHelper,
            eventRepository
        )

    @Provides
    @Singleton
    open fun provideImageUpSyncScheduler(
        context: Context
    ): ImageUpSyncScheduler = ImageUpSyncSchedulerImpl(context)

    @Provides
    open fun provideExitFormHandler(): ExitFormHelper = ExitFormHelperImpl()

    @Provides
    open fun provideGuidFetchGuidHelper(
        downSyncHelper: EventDownSyncHelper,
        subjectRepository: SubjectRepository,
        preferencesManager: IdPreferencesManager,
        crashReportManager: CrashReportManager
    ): FetchGuidHelper =
        FetchGuidHelperImpl(
            downSyncHelper,
            subjectRepository,
            preferencesManager,
            crashReportManager
        )

    @Provides
    open fun provideEncryptedSharedPreferencesBuilder(app: Application): EncryptedSharedPreferencesBuilder =
        EncryptedSharedPreferencesBuilderImpl(app)

    @Provides
    @Named("EncryptedSharedPreferences")
    open fun provideEncryptedSharedPreferences(builder: EncryptedSharedPreferencesBuilder): SharedPreferences =
        builder.buildEncryptedSharedPreferences()

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
        context: Context,
        previewBuilder: QrPreviewBuilder,
        cameraFocusManager: CameraFocusManager
    ): CameraHelper = CameraHelperImpl(context, previewBuilder, cameraFocusManager)

    @Provides
    open fun provideCameraFocusManager(): CameraFocusManager = CameraFocusManagerImpl()

    @Provides
    open fun provideQrPreviewBuilder(): QrPreviewBuilder = QrPreviewBuilderImpl()

    @Provides
    @ExperimentalCoroutinesApi
    open fun provideQrCodeProducer(
        qrCodeDetector: QrCodeDetector,
        crashReportManager: CrashReportManager
    ): QrCodeProducer = QrCodeProducerImpl(qrCodeDetector, crashReportManager)

    @Provides
    open fun provideQrCodeDetector(
        crashReportManager: CrashReportManager
    ): QrCodeDetector = QrCodeDetectorImpl(crashReportManager)

    @Provides
    fun provideHotCache(
        @Named("EncryptedSharedPreferences") sharedPrefs: SharedPreferences,
        stepEncoder: StepEncoder
    ): HotCache = HotCacheImpl(sharedPrefs, stepEncoder)

    @Provides
    fun provideStepEncoder(): StepEncoder = StepEncoderImpl()

    @Provides
    fun provideEnrolmentHelper(
        subjectRepository: SubjectRepository,
        eventRepository: EventRepository,
        timeHelper: TimeHelper
    ): EnrolmentHelper = EnrolmentHelperImpl(subjectRepository, eventRepository, timeHelper)

    @Provides
    fun providePersonCreationEventHelper(
        eventRepository: EventRepository,
        timeHelper: TimeHelper,
        encodingUtils: EncodingUtils
    ): PersonCreationEventHelper =
        PersonCreationEventHelperImpl(eventRepository, timeHelper, encodingUtils)

    @Provides
    open fun provideDispatcher(): DispatcherProvider = DefaultDispatcherProvider()

}

