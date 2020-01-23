package com.simprints.id.commontesttools.di

import android.content.Context
import android.content.SharedPreferences
import com.simprints.id.Application
import com.simprints.id.commontesttools.state.setupFakeEncryptedSharedPreferences
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.controllers.local.SessionEventsLocalDbManager
import com.simprints.id.data.analytics.eventdata.controllers.remote.RemoteSessionsManager
import com.simprints.id.data.consent.LongConsentManager
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.people_sync.PeopleSyncStatusDatabase
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.secure.EncryptedSharedPreferencesBuilder
import com.simprints.id.data.secure.LegacyLocalDbKeyProvider
import com.simprints.id.data.secure.SecureLocalDbKeyProvider
import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.di.AppModule
import com.simprints.id.secure.SecureApiInterface
import com.simprints.id.secure.SignerManager
import com.simprints.id.services.scheduledSync.SyncManager
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncManager
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManager
import com.simprints.id.tools.RandomGenerator
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.device.DeviceManager
import com.simprints.id.tools.utils.SimNetworkUtils
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.di.DependencyRule.RealRule
import io.mockk.every
import io.mockk.mockk

class TestAppModule(
    app: Application,
    var remoteDbManagerRule: DependencyRule = RealRule,
    var remoteSessionsManagerRule: DependencyRule = RealRule,
    var dbManagerRule: DependencyRule = RealRule,
    var secureDataManagerRule: DependencyRule = RealRule,
    var loginInfoManagerRule: DependencyRule = RealRule,
    var randomGeneratorRule: DependencyRule = RealRule,
    var keystoreManagerRule: DependencyRule = RealRule,
    var crashReportManagerRule: DependencyRule = RealRule,
    var sessionEventsManagerRule: DependencyRule = RealRule,
    var sessionEventsLocalDbManagerRule: DependencyRule = RealRule,
    var simNetworkUtilsRule: DependencyRule = RealRule,
    var secureApiInterfaceRule: DependencyRule = RealRule,
    var longConsentManagerRule: DependencyRule = RealRule,
    var syncStatusDatabaseRule: DependencyRule = RealRule,
    private var encryptedSharedPreferencesRule: DependencyRule = DependencyRule.ReplaceRule { setupFakeEncryptedSharedPreferences(app) },
    var deviceManagerRule: DependencyRule = RealRule
) : AppModule() {

    override fun provideCrashManager(): CrashReportManager =
        crashReportManagerRule.resolveDependency { super.provideCrashManager() }

    override fun provideLoginInfoManager(improvedSharedPreferences: ImprovedSharedPreferences): LoginInfoManager =
        loginInfoManagerRule.resolveDependency { super.provideLoginInfoManager(improvedSharedPreferences) }

    override fun provideRandomGenerator(): RandomGenerator =
        randomGeneratorRule.resolveDependency { super.provideRandomGenerator() }

    override fun provideRemoteDbManager(loginInfoManager: LoginInfoManager): RemoteDbManager =
        remoteDbManagerRule.resolveDependency { super.provideRemoteDbManager(loginInfoManager) }

    override fun provideSignerManager(
        projectRepository: ProjectRepository,
        remoteDbManager: RemoteDbManager,
        loginInfoManager: LoginInfoManager,
        preferencesManager: PreferencesManager,
        peopleSyncManager: PeopleSyncManager,
        syncManager: SyncManager
    ): SignerManager = dbManagerRule.resolveDependency {
        super.provideSignerManager(
            projectRepository,
            remoteDbManager,
            loginInfoManager,
            preferencesManager,
            peopleSyncManager,
            syncManager
        )
    }

    override fun provideSecureLocalDbKeyProvider(builder: EncryptedSharedPreferencesBuilder,
                                                 randomGenerator: RandomGenerator,
                                                 unsecuredLocalDbKeyProvider: LegacyLocalDbKeyProvider): SecureLocalDbKeyProvider =
        secureDataManagerRule.resolveDependency { super.provideSecureLocalDbKeyProvider(builder, randomGenerator, unsecuredLocalDbKeyProvider) }

    override fun provideLegacyLocalDbKeyProvider(preferencesManager: PreferencesManager,
                                                 keystoreManager: KeystoreManager): LegacyLocalDbKeyProvider =
        secureDataManagerRule.resolveDependency { super.provideLegacyLocalDbKeyProvider(preferencesManager, keystoreManager) }

    override fun provideKeystoreManager(): KeystoreManager =
        keystoreManagerRule.resolveDependency { super.provideKeystoreManager() }

    override fun provideSecureApiInterface(): SecureApiInterface =
        secureApiInterfaceRule.resolveDependency { super.provideSecureApiInterface() }

    override fun provideSessionEventsManager(
        ctx: Context,
        sessionEventsSyncManager: SessionEventsSyncManager,
        sessionEventsLocalDbManager: SessionEventsLocalDbManager,
        preferencesManager: PreferencesManager,
        timeHelper: TimeHelper,
        crashReportManager: CrashReportManager
    ): SessionEventsManager = sessionEventsManagerRule.resolveDependency {
        super.provideSessionEventsManager(
            ctx,
            sessionEventsSyncManager,
            sessionEventsLocalDbManager,
            preferencesManager,
            timeHelper,
            crashReportManager
        )
    }

    override fun provideSessionEventsLocalDbManager(ctx: Context,
                                                    secureDataManager: SecureLocalDbKeyProvider): SessionEventsLocalDbManager =
        sessionEventsLocalDbManagerRule.resolveDependency { super.provideSessionEventsLocalDbManager(ctx, secureDataManager) }

    override fun provideSimNetworkUtils(ctx: Context): SimNetworkUtils =
        simNetworkUtilsRule.resolveDependency { super.provideSimNetworkUtils(ctx) }

    override fun provideLongConsentManager(ctx: Context, loginInfoManager: LoginInfoManager, crashReportManager: CrashReportManager): LongConsentManager =
        longConsentManagerRule.resolveDependency { super.provideLongConsentManager(ctx, loginInfoManager, crashReportManager) }

    override fun provideRemoteSessionsManager(remoteDbManager: RemoteDbManager): RemoteSessionsManager =
        remoteSessionsManagerRule.resolveDependency { super.provideRemoteSessionsManager(remoteDbManager) }

    override fun provideSyncStatusDatabase(ctx: Context): PeopleSyncStatusDatabase =
        syncStatusDatabaseRule.resolveDependency { super.provideSyncStatusDatabase(ctx) }

    // Android keystore is not available in unit tests - so it returns a mock that builds the standard shared prefs.
    override fun provideEncryptedSharedPreferencesBuilder(app: Application): EncryptedSharedPreferencesBuilder =
        mockk<EncryptedSharedPreferencesBuilder>().apply {
            every { this@apply.buildEncryptedSharedPreferences(any()) } answers { app.getSharedPreferences(args[0] as String, Context.MODE_PRIVATE) }
        }

    override fun provideEncryptedSharedPreferences(builder: EncryptedSharedPreferencesBuilder): SharedPreferences =
        encryptedSharedPreferencesRule.resolveDependency { super.provideEncryptedSharedPreferences(builder) }

    override fun provideDeviceManager(context: Context): DeviceManager =

        deviceManagerRule.resolveDependency { super.provideDeviceManager(context) }
}
