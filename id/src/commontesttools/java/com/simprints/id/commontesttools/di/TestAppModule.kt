@file:Suppress("DEPRECATION")

package com.simprints.id.commontesttools.di

import android.content.Context
import android.content.SharedPreferences
import com.simprints.id.Application
import com.simprints.id.activities.qrcapture.tools.*
import com.simprints.id.commontesttools.state.setupFakeEncryptedSharedPreferences
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.data.db.event.domain.validators.SessionEventValidatorsBuilder
import com.simprints.id.data.db.event.local.EventDatabaseFactory
import com.simprints.id.data.db.event.local.EventLocalDataSource
import com.simprints.id.data.db.project.local.ProjectLocalDataSource
import com.simprints.id.data.db.subjects_sync.SubjectsSyncStatusDatabase
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.events.RecentEventsPreferencesManager
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.data.secure.EncryptedSharedPreferencesBuilder
import com.simprints.id.data.secure.LegacyLocalDbKeyProvider
import com.simprints.id.data.secure.SecureLocalDbKeyProvider
import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.di.AppModule
import com.simprints.id.network.BaseUrlProvider
import com.simprints.id.network.SimApiClientFactory
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManager
import com.simprints.id.tools.LocationManager
import com.simprints.id.tools.RandomGenerator
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.device.ConnectivityHelper
import com.simprints.id.tools.device.DeviceManager
import com.simprints.id.tools.utils.SimNetworkUtils
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.di.DependencyRule.RealRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi

class TestAppModule(
    app: Application,
    private val remoteDbManagerRule: DependencyRule = RealRule,
    private val dbManagerRule: DependencyRule = RealRule,
    private val secureDataManagerRule: DependencyRule = RealRule,
    private val legacyLocalDbKeyProviderRule: DependencyRule = RealRule,
    private val loginInfoManagerRule: DependencyRule = RealRule,
    private val randomGeneratorRule: DependencyRule = RealRule,
    private val keystoreManagerRule: DependencyRule = RealRule,
    private val crashReportManagerRule: DependencyRule = RealRule,
    private val sessionEventsManagerRule: DependencyRule = RealRule,
    private val sessionEventsLocalDbManagerRule: DependencyRule = RealRule,
    private val sessionEventsRemoteDbManagerRule: DependencyRule = RealRule,
    private val simNetworkUtilsRule: DependencyRule = RealRule,
    private val longConsentManagerRule: DependencyRule = RealRule,
    private val secureApiInterfaceRule: DependencyRule = RealRule,
    private val syncStatusDatabaseRule: DependencyRule = RealRule,
    private val deviceManagerRule: DependencyRule = RealRule,
    private val recentEventsPreferencesManagerRule: DependencyRule = RealRule,
    private val remoteProjectInfoProviderRule: DependencyRule = RealRule,
    private val locationManagerRule: DependencyRule = RealRule,
    private val baseUrlProviderRule: DependencyRule = RealRule,
    private val encryptedSharedPreferencesRule: DependencyRule = DependencyRule.ReplaceRule {
        setupFakeEncryptedSharedPreferences(
            app
        )
    },
    private val cameraHelperRule: DependencyRule = RealRule,
    private val qrPreviewBuilderRule: DependencyRule = RealRule,
    private val qrCodeDetectorRule: DependencyRule = RealRule,
    private val qrCodeProducerRule: DependencyRule = RealRule
) : AppModule() {

    override fun provideCrashManager(): CrashReportManager =
        crashReportManagerRule.resolveDependency { super.provideCrashManager() }

    override fun provideLoginInfoManager(
        improvedSharedPreferences: ImprovedSharedPreferences
    ): LoginInfoManager = loginInfoManagerRule.resolveDependency {
        super.provideLoginInfoManager(
            improvedSharedPreferences
        )
    }

    override fun provideRandomGenerator(): RandomGenerator =
        randomGeneratorRule.resolveDependency { super.provideRandomGenerator() }

    override fun provideRemoteDbManager(loginInfoManager: LoginInfoManager): RemoteDbManager =
        remoteDbManagerRule.resolveDependency { super.provideRemoteDbManager(loginInfoManager) }

    override fun provideSecureLocalDbKeyProvider(
        builder: EncryptedSharedPreferencesBuilder,
        randomGenerator: RandomGenerator,
        unsecuredLocalDbKeyProvider: LegacyLocalDbKeyProvider
    ): SecureLocalDbKeyProvider =
        secureDataManagerRule.resolveDependency {
            super.provideSecureLocalDbKeyProvider(
                builder,
                randomGenerator,
                unsecuredLocalDbKeyProvider
            )
        }

    override fun provideLegacyLocalDbKeyProvider(
        preferencesManager: PreferencesManager,
        keystoreManager: KeystoreManager
    ): LegacyLocalDbKeyProvider =
        legacyLocalDbKeyProviderRule.resolveDependency {
            super.provideLegacyLocalDbKeyProvider(
                preferencesManager,
                keystoreManager
            )
        }

    override fun provideKeystoreManager(): KeystoreManager =
        keystoreManagerRule.resolveDependency { super.provideKeystoreManager() }

    override fun provideSessionEventsManager(
        ctx: Context,
        sessionEventsSyncManager: SessionEventsSyncManager,
        eventLocalDataSource: EventLocalDataSource,
        eventRemoteDataSource: EventRemoteDataSource,
        preferencesManager: PreferencesManager,
        loginInfoManager: LoginInfoManager,
        timeHelper: TimeHelper,
        crashReportManager: CrashReportManager
    ): EventRepository = sessionEventsManagerRule.resolveDependency {
        super.provideSessionEventsManager(
            ctx,
            sessionEventsSyncManager,
            eventLocalDataSource,
            eventRemoteDataSource,
            preferencesManager,
            loginInfoManager,
            timeHelper,
            crashReportManager
        )
    }

    override fun provideSessionEventsLocalDbManager(
        ctx: Context,
        secureDataManager: SecureLocalDbKeyProvider,
        timeHelper: TimeHelper,
        factory: EventDatabaseFactory,
        sessionEventValidatorsBuilder: SessionEventValidatorsBuilder,
        loginInfoManager: LoginInfoManager
    ): EventLocalDataSource =
        sessionEventsLocalDbManagerRule.resolveDependency {
            super.provideSessionEventsLocalDbManager(
                ctx,
                secureDataManager,
                timeHelper,
                factory,
                sessionEventValidatorsBuilder,
                loginInfoManager
            )
        }

    override fun provideSessionEventsRemoteDbManager(
        simApiClientFactory: SimApiClientFactory
    ): EventRemoteDataSource =
        sessionEventsRemoteDbManagerRule.resolveDependency {
            super.provideSessionEventsRemoteDbManager(
                simApiClientFactory
            )
        }

    override fun provideSimNetworkUtils(ctx: Context): SimNetworkUtils =
        simNetworkUtilsRule.resolveDependency { super.provideSimNetworkUtils(ctx) }

    override fun provideLocationManager(ctx: Context): LocationManager =
        locationManagerRule.resolveDependency {
            super.provideLocationManager(ctx)
        }

    override fun provideSyncStatusDatabase(ctx: Context): SubjectsSyncStatusDatabase =
        syncStatusDatabaseRule.resolveDependency { super.provideSyncStatusDatabase(ctx) }

    // Android keystore is not available in unit tests - so it returns a mock that builds the standard shared prefs.
    override fun provideEncryptedSharedPreferencesBuilder(
        app: Application
    ): EncryptedSharedPreferencesBuilder = mockk<EncryptedSharedPreferencesBuilder>().apply {
        every { this@apply.buildEncryptedSharedPreferences(any()) } answers {
            app.getSharedPreferences(
                args[0] as String,
                Context.MODE_PRIVATE
            )
        }
    }

    override fun provideEncryptedSharedPreferences(
        builder: EncryptedSharedPreferencesBuilder
    ): SharedPreferences = encryptedSharedPreferencesRule.resolveDependency {
        super.provideEncryptedSharedPreferences(
            builder
        )
    }

    override fun provideDeviceManager(
        connectivityHelper: ConnectivityHelper
    ): DeviceManager = deviceManagerRule.resolveDependency {
        super.provideDeviceManager(connectivityHelper)
    }

    override fun provideRecentEventsPreferencesManager(
        prefs: ImprovedSharedPreferences
    ): RecentEventsPreferencesManager = recentEventsPreferencesManagerRule.resolveDependency {
        super.provideRecentEventsPreferencesManager(prefs)
    }

    override fun provideCameraHelper(
        context: Context,
        previewBuilder: QrPreviewBuilder,
        cameraFocusManager: CameraFocusManager
    ): CameraHelper = cameraHelperRule.resolveDependency {
        super.provideCameraHelper(context, previewBuilder, cameraFocusManager)
    }

    override fun provideQrPreviewBuilder(): QrPreviewBuilder {
        return qrPreviewBuilderRule.resolveDependency {
            super.provideQrPreviewBuilder()
        }
    }

    @ExperimentalCoroutinesApi
    override fun provideQrCodeProducer(
        qrCodeDetector: QrCodeDetector,
        crashReportManager: CrashReportManager
    ): QrCodeProducer = qrCodeProducerRule.resolveDependency {
        super.provideQrCodeProducer(qrCodeDetector, crashReportManager)
    }

    override fun provideQrCodeDetector(
        crashReportManager: CrashReportManager
    ): QrCodeDetector = qrCodeDetectorRule.resolveDependency {
        super.provideQrCodeDetector(crashReportManager)
    }

    override fun provideBaseUrlProvider(
        settingsPreferencesManager: SettingsPreferencesManager,
        projectLocalDataSource: ProjectLocalDataSource,
        loginInfoManager: LoginInfoManager
    ): BaseUrlProvider = baseUrlProviderRule.resolveDependency {
        super.provideBaseUrlProvider(
            settingsPreferencesManager,
            projectLocalDataSource,
            loginInfoManager
        )
    }

}
