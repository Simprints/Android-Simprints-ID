package com.simprints.id.di

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.analytics.FirebaseAnalytics
import com.simprints.core.network.SimApiClientFactory
import com.simprints.core.tools.LanguageHelper
import com.simprints.id.Application
import com.simprints.id.activities.consent.ConsentViewModelFactory
import com.simprints.id.activities.coreexitform.CoreExitFormViewModelFactory
import com.simprints.id.activities.fetchguid.FetchGuidViewModelFactory
import com.simprints.id.activities.fingerprintexitform.FingerprintExitFormViewModelFactory
import com.simprints.id.activities.qrcapture.tools.*
import com.simprints.id.activities.settings.fragments.moduleselection.ModuleViewModelFactory
import com.simprints.id.activities.settings.syncinformation.SyncInformationViewModelFactory
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.AnalyticsManagerImpl
import com.simprints.id.data.analytics.crashreport.CoreCrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportManagerImpl
import com.simprints.id.data.consent.LongConsentManager
import com.simprints.id.data.consent.LongConsentManagerImpl
import com.simprints.id.data.consent.shortconsent.ConsentLocalDataSource
import com.simprints.id.data.consent.shortconsent.ConsentLocalDataSourceImpl
import com.simprints.id.data.consent.shortconsent.ConsentRepository
import com.simprints.id.data.consent.shortconsent.ConsentRepositoryImpl
import com.simprints.id.data.db.common.FirebaseManagerImpl
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.people_sync.PeopleSyncStatusDatabase
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.db.session.domain.SessionEventsManager
import com.simprints.id.data.db.session.domain.SessionEventsManagerImpl
import com.simprints.id.data.db.session.local.RealmSessionEventsDbManagerImpl
import com.simprints.id.data.db.session.local.SessionEventsLocalDbManager
import com.simprints.id.data.db.session.remote.RemoteSessionsManager
import com.simprints.id.data.db.session.remote.RemoteSessionsManagerImpl
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.loginInfo.LoginInfoManagerImpl
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.data.prefs.events.RecentEventsPreferencesManager
import com.simprints.id.data.prefs.events.RecentEventsPreferencesManagerImpl
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.secure.*
import com.simprints.id.data.secure.SecureLocalDbKeyProvider.Companion.FILENAME_FOR_REALM_KEY_SHARED_PREFS
import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.data.secure.keystore.KeystoreManagerImpl
import com.simprints.id.exitformhandler.ExitFormHelper
import com.simprints.id.exitformhandler.ExitFormHelperImpl
import com.simprints.id.moduleselection.ModuleRepository
import com.simprints.id.moduleselection.ModuleRepositoryImpl
import com.simprints.id.secure.SecureApiInterface
import com.simprints.id.secure.SignerManager
import com.simprints.id.secure.SignerManagerImpl
import com.simprints.id.services.GuidSelectionManager
import com.simprints.id.services.GuidSelectionManagerImpl
import com.simprints.id.services.scheduledSync.SyncManager
import com.simprints.id.services.scheduledSync.imageUpSync.ImageUpSyncScheduler
import com.simprints.id.services.scheduledSync.imageUpSync.ImageUpSyncSchedulerImpl
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncManager
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
    open fun provideSignerManager(
        projectRepository: ProjectRepository,
        remoteDbManager: RemoteDbManager,
        loginInfoManager: LoginInfoManager,
        preferencesManager: PreferencesManager,
        peopleSyncManager: PeopleSyncManager,
        syncManager: SyncManager
    ): SignerManager = SignerManagerImpl(
        projectRepository,
        remoteDbManager,
        loginInfoManager,
        preferencesManager,
        peopleSyncManager,
        syncManager
    )

    @Provides
    @Singleton
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
    open fun provideSecureLocalDbKeyProvider(builder: EncryptedSharedPreferencesBuilder,
                                             randomGenerator: RandomGenerator,
                                             unsecuredLocalDbKeyProvider: LegacyLocalDbKeyProvider): SecureLocalDbKeyProvider =
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
    open fun provideLongConsentManager(
        ctx: Context,
        loginInfoManager: LoginInfoManager,
        crashReportManager: CrashReportManager
    ):
        LongConsentManager =
        LongConsentManagerImpl(ctx.filesDir.absolutePath, loginInfoManager, crashReportManager)

    @Provides
    @Singleton
    open fun provideSimNetworkUtils(ctx: Context): SimNetworkUtils = SimNetworkUtilsImpl(ctx)

    @Provides
    open fun provideSimApiClientFactory(ctx: Context) =
        SimApiClientFactory(ctx.deviceId)

    @Provides
    open fun provideSecureApiInterface(simApiClientFactory: SimApiClientFactory): SecureApiInterface =
        simApiClientFactory.build<SecureApiInterface>(null).api

    @Provides
    @Singleton
    fun provideTimeHelper(): TimeHelper = TimeHelperImpl()

    @Provides
    fun provideAndroidResourcesHelper(
        ctx: Context,
        preferencesManager: PreferencesManager
    ): AndroidResourcesHelper {
        val contextWithSpecificLanguage =
            LanguageHelper.contextWithSpecificLanguage(ctx, preferencesManager.language)
        return AndroidResourcesHelperImpl(contextWithSpecificLanguage)
    }

    @Provides
    @Singleton
    open fun provideSessionEventsLocalDbManager(
        ctx: Context,
        secureDataManager: SecureLocalDbKeyProvider
    ): SessionEventsLocalDbManager =
        RealmSessionEventsDbManagerImpl(ctx, secureDataManager)

    @Provides
    @Singleton
    open fun provideSessionEventsManager(
        ctx: Context,
        sessionEventsSyncManager: SessionEventsSyncManager,
        sessionEventsLocalDbManager: SessionEventsLocalDbManager,
        preferencesManager: PreferencesManager,
        timeHelper: TimeHelper,
        crashReportManager: CrashReportManager
    ): SessionEventsManager =
        SessionEventsManagerImpl(
            ctx.deviceId,
            ctx.packageVersionName,
            sessionEventsSyncManager,
            sessionEventsLocalDbManager,
            preferencesManager,
            timeHelper,
            crashReportManager
        )


    @Provides
    @Singleton
    open fun provideSyncStatusDatabase(ctx: Context): PeopleSyncStatusDatabase =
        PeopleSyncStatusDatabase.getDatabase(ctx)


    @Provides
    fun provideModuleViewModelFactory(repository: ModuleRepository) =
        ModuleViewModelFactory(repository)

    @Provides
    fun provideModuleRepository(
        preferencesManager: PreferencesManager,
        crashReportManager: CrashReportManager
    ): ModuleRepository = ModuleRepositoryImpl(preferencesManager, crashReportManager)

    @Provides
    @Singleton
    open fun provideRemoteSessionsManager(remoteDbManager: RemoteDbManager,
                                          simApiClientFactory: SimApiClientFactory): RemoteSessionsManager =
        RemoteSessionsManagerImpl(remoteDbManager, simApiClientFactory)

    @Provides
    open fun provideGuidSelectionManager(
        context: Context,
        loginInfoManager: LoginInfoManager,
        analyticsManager: AnalyticsManager,
        crashReportManager: CrashReportManager,
        timeHelper: TimeHelper,
        sessionEventsManager: SessionEventsManager
    ): GuidSelectionManager =
        GuidSelectionManagerImpl(
            context.deviceId,
            loginInfoManager,
            analyticsManager,
            crashReportManager,
            timeHelper,
            sessionEventsManager
        )

    @Provides
    @Singleton
    open fun provideImageUpSyncScheduler(
        context: Context
    ): ImageUpSyncScheduler = ImageUpSyncSchedulerImpl(context)

    @Provides
    open fun getConsentDataManager(
        prefs: ImprovedSharedPreferences,
        remoteConfigWrapper: RemoteConfigWrapper
    ): ConsentLocalDataSource =
        ConsentLocalDataSourceImpl(prefs, remoteConfigWrapper)

    @Provides
    open fun provideConsentTextManager(
        context: Context,
        consentLocalDataSource: ConsentLocalDataSource,
        crashReportManager: CrashReportManager,
        preferencesManager: PreferencesManager,
        androidResourcesHelper: AndroidResourcesHelper
    ): ConsentRepository =
        ConsentRepositoryImpl(
            consentLocalDataSource, crashReportManager, preferencesManager.programName,
            preferencesManager.organizationName, androidResourcesHelper,
            preferencesManager.modalities
        )

    @Provides
    open fun provideConsentViewModelFactory(
        consentTextManager: ConsentRepository,
        sessionEventsManager: SessionEventsManager,
        timeHelper: TimeHelper
    ) =
        ConsentViewModelFactory(consentTextManager, sessionEventsManager)

    @Provides
    open fun provideCoreExitFormViewModelFactory(sessionEventsManager: SessionEventsManager) =
        CoreExitFormViewModelFactory(sessionEventsManager)

    @Provides
    open fun provideFingerprintExitFormViewModelFactory(sessionEventsManager: SessionEventsManager) =
        FingerprintExitFormViewModelFactory(sessionEventsManager)

    @Provides
    open fun provideExitFormHandler(): ExitFormHelper = ExitFormHelperImpl()

    @Provides
    open fun provideFetchGuidViewModelFactory(personRepository: PersonRepository,
                                              deviceManager: DeviceManager,
                                              sessionEventsManager: SessionEventsManager,
                                              timeHelper: TimeHelper) =
        FetchGuidViewModelFactory(personRepository, deviceManager, sessionEventsManager, timeHelper)

    @Provides
    open fun provideSyncInformationViewModelFactory(
        personRepository: PersonRepository,
        personLocalDataSource: PersonLocalDataSource,
        preferencesManager: PreferencesManager,
        loginInfoManager: LoginInfoManager,
        peopleDownSyncScopeRepository: PeopleDownSyncScopeRepository
    ) =
        SyncInformationViewModelFactory(
            personRepository, personLocalDataSource, preferencesManager,
            loginInfoManager.getSignedInProjectIdOrEmpty(), peopleDownSyncScopeRepository
        )

    @Provides
    open fun provideEncryptedSharedPreferencesBuilder(app: Application): EncryptedSharedPreferencesBuilder =
        EncryptedSharedPreferencesBuilderImpl(app)

    @Provides
    @Named("EncryptedSharedPreferences")
    open fun provideEncryptedSharedPreferences(builder: EncryptedSharedPreferencesBuilder): SharedPreferences =
        builder.buildEncryptedSharedPreferences()

    @Provides
    open fun provideDeviceManager(connectivityHelper: ConnectivityHelper): DeviceManager = DeviceManagerImpl(connectivityHelper)

    @Provides
    open fun provideConnectivityHelper(ctx: Context): ConnectivityHelper = ConnectivityHelperImpl(ctx)

    @Provides
    open fun provideLocationManager(ctx: Context): LocationManager = LocationManagerImpl(ctx)

    @Provides
    open fun provideCameraHelper(
        context: Context,
        previewBuilder: QrPreviewBuilder,
        cameraFocusManager: CameraFocusManager,
        qrCodeProducer: QrCodeProducer
    ): CameraHelper = CameraHelperImpl(context, previewBuilder, cameraFocusManager, qrCodeProducer)

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

}

