package com.simprints.id.di

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.simprints.id.Application
import com.simprints.id.activities.consent.ConsentViewModelFactory
import com.simprints.id.activities.coreexitform.CoreExitFormViewModelFactory
import com.simprints.id.activities.enrollast.EnrolLastBiometricsViewModelFactory
import com.simprints.id.activities.fetchguid.FetchGuidViewModelFactory
import com.simprints.id.activities.fingerprintexitform.FingerprintExitFormViewModelFactory
import com.simprints.id.activities.longConsent.PrivacyNoticeViewModelFactory
import com.simprints.id.activities.qrcapture.tools.*
import com.simprints.id.activities.settings.fragments.moduleselection.ModuleViewModelFactory
import com.simprints.id.activities.settings.syncinformation.SyncInformationViewModelFactory
import com.simprints.id.activities.setup.SetupViewModelFactory
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.AnalyticsManagerImpl
import com.simprints.id.data.analytics.crashreport.CoreCrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportManagerImpl
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.data.db.common.FirebaseManagerImpl
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.data.db.event.EventRepositoryImpl
import com.simprints.id.data.db.event.domain.validators.SessionEventValidatorsBuilder
import com.simprints.id.data.db.event.domain.validators.SessionEventValidatorsBuilderImpl
import com.simprints.id.data.db.event.local.DbEventDatabaseFactory
import com.simprints.id.data.db.event.local.DbEventDatabaseFactoryImpl
import com.simprints.id.data.db.event.local.EventLocalDataSource
import com.simprints.id.data.db.event.local.EventLocalDataSourceImpl
import com.simprints.id.data.db.event.remote.SessionRemoteDataSource
import com.simprints.id.data.db.event.remote.SessionRemoteDataSourceImpl
import com.simprints.id.data.db.project.local.ProjectLocalDataSource
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
import com.simprints.id.data.db.subjects_sync.SubjectsSyncStatusDatabase
import com.simprints.id.data.db.subjects_sync.down.SubjectsDownSyncScopeRepository
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.loginInfo.LoginInfoManagerImpl
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.events.RecentEventsPreferencesManager
import com.simprints.id.data.prefs.events.RecentEventsPreferencesManagerImpl
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.data.secure.*
import com.simprints.id.data.secure.SecureLocalDbKeyProvider.Companion.FILENAME_FOR_REALM_KEY_SHARED_PREFS
import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.data.secure.keystore.KeystoreManagerImpl
import com.simprints.id.exitformhandler.ExitFormHelper
import com.simprints.id.exitformhandler.ExitFormHelperImpl
import com.simprints.id.moduleselection.ModuleRepository
import com.simprints.id.moduleselection.ModuleRepositoryImpl
import com.simprints.id.network.BaseUrlProvider
import com.simprints.id.network.BaseUrlProviderImpl
import com.simprints.id.network.SimApiClientFactory
import com.simprints.id.network.SimApiClientFactoryImpl
import com.simprints.id.orchestrator.EnrolmentHelper
import com.simprints.id.orchestrator.EnrolmentHelperImpl
import com.simprints.id.orchestrator.cache.HotCache
import com.simprints.id.orchestrator.cache.HotCacheImpl
import com.simprints.id.orchestrator.cache.StepEncoder
import com.simprints.id.orchestrator.cache.StepEncoderImpl
import com.simprints.id.services.guidselection.GuidSelectionManager
import com.simprints.id.services.guidselection.GuidSelectionManagerImpl
import com.simprints.id.services.scheduledSync.imageUpSync.ImageUpSyncScheduler
import com.simprints.id.services.scheduledSync.imageUpSync.ImageUpSyncSchedulerImpl
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManager
import com.simprints.id.tools.*
import com.simprints.id.tools.device.ConnectivityHelper
import com.simprints.id.tools.device.ConnectivityHelperImpl
import com.simprints.id.tools.device.DeviceManager
import com.simprints.id.tools.device.DeviceManagerImpl
import com.simprints.id.tools.extensions.deviceId
import com.simprints.id.tools.extensions.packageVersionName
import com.simprints.id.tools.utils.SimNetworkUtils
import com.simprints.id.tools.utils.SimNetworkUtilsImpl
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Named
import javax.inject.Singleton

@Module
open class AppModule {

    @Provides
    @Singleton
    fun provideContext(app: Application): Context = app

    @Provides
    @Singleton
    open fun provideRemoteDbManager(loginInfoManager: LoginInfoManager): RemoteDbManager =
        FirebaseManagerImpl(loginInfoManager)

    @Provides
    @Singleton
    open fun provideLoginInfoManager(improvedSharedPreferences: ImprovedSharedPreferences): LoginInfoManager =
        LoginInfoManagerImpl(improvedSharedPreferences)

    @Provides
    @Singleton
    @Suppress("MissingPermission")
    fun provideFirebaseAnalytics(app: Application): FirebaseAnalytics =
        FirebaseAnalytics.getInstance(app).apply {
            setMinimumSessionDuration(0)
        }

    @Provides
    @Singleton
    open fun provideRecentEventsPreferencesManager(prefs: ImprovedSharedPreferences): RecentEventsPreferencesManager =
        RecentEventsPreferencesManagerImpl(prefs)

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
    open fun provideSimApiClientFactory(
        ctx: Context,
        remoteDbManager: RemoteDbManager,
        baseUrlProvider: BaseUrlProvider,
        gson: Gson
    ): SimApiClientFactory = SimApiClientFactoryImpl(
        baseUrlProvider,
        ctx.deviceId,
        remoteDbManager,
        gson
    )

    @Provides
    @Singleton
    fun provideTimeHelper(): TimeHelper = TimeHelperImpl()


    @Provides
    open fun provideSessionEventValidatorsBuilder(): SessionEventValidatorsBuilder =
        SessionEventValidatorsBuilderImpl()

    @Provides
    open fun provideDbEventDatabaseFactory(ctx: Context,
                                            secureDataManager: SecureLocalDbKeyProvider): DbEventDatabaseFactory =
        DbEventDatabaseFactoryImpl(ctx, secureDataManager)

    @Provides
    @Singleton
    open fun provideSessionEventsLocalDbManager(
        ctx: Context,
        secureDataManager: SecureLocalDbKeyProvider,
        timeHelper: TimeHelper,
        dbFactory: DbEventDatabaseFactory,
        sessionEventValidatorsBuilder: SessionEventValidatorsBuilder,
        loginInfoManager: LoginInfoManager
    ): EventLocalDataSource =
        EventLocalDataSourceImpl(dbFactory, loginInfoManager, ctx.deviceId, timeHelper, sessionEventValidatorsBuilder.build())

    @Provides
    @Singleton
    open fun provideSessionEventsRemoteDbManager(
        simApiClientFactory: SimApiClientFactory
    ): SessionRemoteDataSource = SessionRemoteDataSourceImpl(
        simApiClientFactory
    )

    @Provides
    @Singleton
    open fun provideSessionEventsManager(
        ctx: Context,
        sessionEventsSyncManager: SessionEventsSyncManager,
        eventLocalDataSource: EventLocalDataSource,
        sessionRemoteDataSource: SessionRemoteDataSource,
        preferencesManager: PreferencesManager,
        loginInfoManager: LoginInfoManager,
        timeHelper: TimeHelper,
        crashReportManager: CrashReportManager
    ): EventRepository =
        EventRepositoryImpl(
            ctx.deviceId,
            ctx.packageVersionName,
            loginInfoManager.getSignedInProjectIdOrEmpty(),
            sessionEventsSyncManager,
            eventLocalDataSource,
            sessionRemoteDataSource,
            preferencesManager,
            crashReportManager,
            timeHelper
        )

    @Provides
    @Singleton
    open fun provideSyncStatusDatabase(ctx: Context): SubjectsSyncStatusDatabase =
        SubjectsSyncStatusDatabase.getDatabase(ctx)


    @Provides
    fun provideModuleViewModelFactory(repository: ModuleRepository) =
        ModuleViewModelFactory(repository)

    @Provides
    fun provideModuleRepository(
        preferencesManager: PreferencesManager,
        crashReportManager: CrashReportManager
    ): ModuleRepository = ModuleRepositoryImpl(preferencesManager, crashReportManager)

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
    open fun provideConsentViewModelFactory(eventRepository: EventRepository) =
        ConsentViewModelFactory(eventRepository)

    @Provides
    open fun provideCoreExitFormViewModelFactory(eventRepository: EventRepository) =
        CoreExitFormViewModelFactory(eventRepository)

    @Provides
    open fun provideFingerprintExitFormViewModelFactory(eventRepository: EventRepository) =
        FingerprintExitFormViewModelFactory(eventRepository)

    @Provides
    open fun provideExitFormHandler(): ExitFormHelper = ExitFormHelperImpl()

    @Provides
    open fun provideFetchGuidViewModelFactory(personRepository: SubjectRepository,
                                              deviceManager: DeviceManager,
                                              eventRepository: EventRepository,
                                              timeHelper: TimeHelper) =
        FetchGuidViewModelFactory(personRepository, deviceManager, eventRepository, timeHelper)

    @Provides
    open fun provideSyncInformationViewModelFactory(
        personRepository: SubjectRepository,
        subjectLocalDataSource: SubjectLocalDataSource,
        preferencesManager: PreferencesManager,
        loginInfoManager: LoginInfoManager,
        subjectsDownSyncScopeRepository: SubjectsDownSyncScopeRepository
    ) = SyncInformationViewModelFactory(
        personRepository, subjectLocalDataSource, preferencesManager,
        loginInfoManager.getSignedInProjectIdOrEmpty(), subjectsDownSyncScopeRepository
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
    open fun providePrivacyNoticeViewModelFactory(
        longConsentRepository: LongConsentRepository,
        preferencesManager: PreferencesManager
    ) = PrivacyNoticeViewModelFactory(longConsentRepository, preferencesManager)

    @Provides
    fun provideHotCache(
        @Named("EncryptedSharedPreferences") sharedPrefs: SharedPreferences,
        stepEncoder: StepEncoder
    ): HotCache = HotCacheImpl(sharedPrefs, stepEncoder)

    @Provides
    fun provideStepEncoder(): StepEncoder = StepEncoderImpl()

    @Provides
    fun provideEnrolmentHelper(
        repository: SubjectRepository,
        eventRepository: EventRepository,
        timeHelper: TimeHelper
    ): EnrolmentHelper = EnrolmentHelperImpl(repository, eventRepository, timeHelper)

    @Provides
    open fun provideEnrolLastBiometricsViewModel(
        enrolmentHelper: EnrolmentHelper,
        timeHelper: TimeHelper
    ) = EnrolLastBiometricsViewModelFactory(enrolmentHelper, timeHelper)

    @ExperimentalCoroutinesApi
    @Provides
    open fun provideSetupViewModelFactory(
        deviceManager: DeviceManager,
        crashReportManager: CrashReportManager
    ) = SetupViewModelFactory(deviceManager, crashReportManager)
}

