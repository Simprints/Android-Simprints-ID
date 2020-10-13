package com.simprints.id.di

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.simprints.core.tools.coroutines.DefaultDispatcherProvider
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.id.Application
import com.simprints.id.activities.consent.ConsentViewModelFactory
import com.simprints.id.activities.coreexitform.CoreExitFormViewModelFactory
import com.simprints.id.activities.enrollast.EnrolLastBiometricsViewModelFactory
import com.simprints.id.activities.fetchguid.FetchGuidViewModelFactory
import com.simprints.id.activities.fingerprintexitform.FingerprintExitFormViewModelFactory
import com.simprints.id.activities.longConsent.PrivacyNoticeViewModelFactory
import com.simprints.id.activities.qrcapture.tools.*
import com.simprints.id.activities.settings.fingerselection.FingerSelectionViewModelFactory
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
import com.simprints.id.data.db.project.local.ProjectLocalDataSource
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.data.db.session.SessionRepositoryImpl
import com.simprints.id.data.db.session.domain.models.SessionEventValidatorsBuilder
import com.simprints.id.data.db.session.domain.models.SessionEventValidatorsBuilderImpl
import com.simprints.id.data.db.session.local.SessionLocalDataSource
import com.simprints.id.data.db.session.local.SessionLocalDataSourceImpl
import com.simprints.id.data.db.session.local.SessionRealmConfigBuilder
import com.simprints.id.data.db.session.local.SessionRealmConfigBuilderImpl
import com.simprints.id.data.db.session.remote.SessionRemoteDataSource
import com.simprints.id.data.db.session.remote.SessionRemoteDataSourceImpl
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
import com.simprints.id.data.db.subjects_sync.SubjectsSyncStatusDatabase
import com.simprints.id.data.db.subjects_sync.down.SubjectsDownSyncScopeRepository
import com.simprints.id.data.images.repository.ImageRepository
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
import com.simprints.id.services.scheduledSync.subjects.master.SubjectsSyncManager
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
    open fun provideSessionRealmConfigBuilder(): SessionRealmConfigBuilder =
        SessionRealmConfigBuilderImpl()

    @Provides
    @Singleton
    open fun provideSessionEventValidatorsBuilder(): SessionEventValidatorsBuilder =
        SessionEventValidatorsBuilderImpl()


    @Provides
    @Singleton
    open fun provideSessionEventsLocalDbManager(
        ctx: Context,
        secureDataManager: SecureLocalDbKeyProvider,
        timeHelper: TimeHelper,
        sessionRealmConfigBuilder: SessionRealmConfigBuilder,
        sessionEventValidatorsBuilder: SessionEventValidatorsBuilder
    ): SessionLocalDataSource =
        SessionLocalDataSourceImpl(
            ctx,
            secureDataManager,
            timeHelper,
            sessionRealmConfigBuilder,
            sessionEventValidatorsBuilder.build()
        )

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
        sessionLocalDataSource: SessionLocalDataSource,
        sessionRemoteDataSource: SessionRemoteDataSource,
        preferencesManager: PreferencesManager,
        loginInfoManager: LoginInfoManager,
        timeHelper: TimeHelper,
        crashReportManager: CrashReportManager
    ): SessionRepository =
        SessionRepositoryImpl(
            ctx.deviceId,
            ctx.packageVersionName,
            loginInfoManager.getSignedInProjectIdOrEmpty(),
            sessionEventsSyncManager,
            sessionLocalDataSource,
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
        crashReportManager: CrashReportManager,
        subjectLocalDataSource: SubjectLocalDataSource
    ): ModuleRepository = ModuleRepositoryImpl(
        preferencesManager,
        crashReportManager,
        subjectLocalDataSource
    )

    @Provides
    open fun provideGuidSelectionManager(
        context: Context,
        loginInfoManager: LoginInfoManager,
        analyticsManager: AnalyticsManager,
        crashReportManager: CrashReportManager,
        timeHelper: TimeHelper,
        sessionRepository: SessionRepository
    ): GuidSelectionManager =
        GuidSelectionManagerImpl(
            context.deviceId,
            loginInfoManager,
            analyticsManager,
            crashReportManager,
            timeHelper,
            sessionRepository
        )

    @Provides
    @Singleton
    open fun provideImageUpSyncScheduler(
        context: Context
    ): ImageUpSyncScheduler = ImageUpSyncSchedulerImpl(context)

    @Provides
    open fun provideConsentViewModelFactory(sessionRepository: SessionRepository) =
        ConsentViewModelFactory(sessionRepository)

    @Provides
    open fun provideCoreExitFormViewModelFactory(sessionRepository: SessionRepository) =
        CoreExitFormViewModelFactory(sessionRepository)

    @Provides
    open fun provideFingerprintExitFormViewModelFactory(sessionRepository: SessionRepository) =
        FingerprintExitFormViewModelFactory(sessionRepository)

    @Provides
    open fun provideExitFormHandler(): ExitFormHelper = ExitFormHelperImpl()

    @Provides
    open fun provideFetchGuidViewModelFactory(
        personRepository: SubjectRepository,
        deviceManager: DeviceManager,
        sessionRepository: SessionRepository,
        timeHelper: TimeHelper
    ) =
        FetchGuidViewModelFactory(personRepository, deviceManager, sessionRepository, timeHelper)

    @Provides
    open fun provideSyncInformationViewModelFactory(
        personRepository: SubjectRepository,
        subjectLocalDataSource: SubjectLocalDataSource,
        preferencesManager: PreferencesManager,
        loginInfoManager: LoginInfoManager,
        subjectsDownSyncScopeRepository: SubjectsDownSyncScopeRepository,
        imageRepository: ImageRepository,
        subjectsSyncManager: SubjectsSyncManager
    ) = SyncInformationViewModelFactory(
        personRepository, subjectLocalDataSource, preferencesManager,
        loginInfoManager.getSignedInProjectIdOrEmpty(), subjectsDownSyncScopeRepository,
        imageRepository, subjectsSyncManager
    )

    @Provides
    open fun provideFingerSelectionViewModelFactory(
        preferencesManager: PreferencesManager,
        crashReportManager: CrashReportManager
    ) = FingerSelectionViewModelFactory(preferencesManager, crashReportManager)

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
        sessionRepository: SessionRepository,
        timeHelper: TimeHelper
    ): EnrolmentHelper = EnrolmentHelperImpl(repository, sessionRepository, timeHelper)

    @Provides
    open fun provideEnrolLastBiometricsViewModel(
        enrolmentHelper: EnrolmentHelper,
        timeHelper: TimeHelper,
        preferencesManager: PreferencesManager
    ) = EnrolLastBiometricsViewModelFactory(enrolmentHelper, timeHelper, preferencesManager)

    @ExperimentalCoroutinesApi
    @Provides
    open fun provideSetupViewModelFactory(
        deviceManager: DeviceManager,
        crashReportManager: CrashReportManager
    ) = SetupViewModelFactory(deviceManager, crashReportManager)

    @Provides
    open fun provideDispatcher(): DispatcherProvider = DefaultDispatcherProvider()
}

