package com.simprints.id.shared

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.simprints.id.Application
import com.simprints.id.data.DataManager
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.events.SessionEventsLocalDbManager
import com.simprints.id.data.analytics.events.SessionEventsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.di.AppModule
import com.simprints.id.shared.DependencyRule.*
import com.simprints.id.tools.RandomGenerator
import com.simprints.id.tools.TimeHelper
import com.simprints.libscanner.bluetooth.BluetoothComponentAdapter
import org.mockito.Mockito.spy

open class AppModuleForAnyTests(app: Application,
                                open var localDbManagerRule: DependencyRule = RealRule(),
                                open var remoteDbManagerRule: DependencyRule = RealRule(),
                                open var dbManagerRule: DependencyRule = RealRule(),
                                open var secureDataManagerRule: DependencyRule = RealRule(),
                                open var dataManagerRule: DependencyRule = RealRule(),
                                open var loginInfoManagerRule: DependencyRule = RealRule(),
                                open var randomGeneratorRule: DependencyRule = RealRule(),
                                open var keystoreManagerRule: DependencyRule = RealRule(),
                                open var analyticsManagerRule: DependencyRule = RealRule(),
                                open var bluetoothComponentAdapterRule: DependencyRule = RealRule(),
                                open var sessionEventsManagerRule: DependencyRule = RealRule(),
                                open var sessionEventsLocalDbManagerRule: DependencyRule = RealRule()) : AppModule(app) {

    override fun provideLocalDbManager(ctx: Context): LocalDbManager =
        resolveDependencyRule(localDbManagerRule) { super.provideLocalDbManager(ctx) }

    override fun provideAnalyticsManager(loginInfoManager: LoginInfoManager,
                                         preferencesManager: PreferencesManager,
                                         firebaseAnalytics: FirebaseAnalytics): AnalyticsManager =
        resolveDependencyRule(analyticsManagerRule) { super.provideAnalyticsManager(loginInfoManager, preferencesManager, firebaseAnalytics) }

    override fun provideRemoteDbManager(ctx: Context): RemoteDbManager =
        resolveDependencyRule(remoteDbManagerRule) { super.provideRemoteDbManager(ctx) }

    override fun provideLoginInfoManager(improvedSharedPreferences: ImprovedSharedPreferences): LoginInfoManager =
        resolveDependencyRule(loginInfoManagerRule) { super.provideLoginInfoManager(improvedSharedPreferences) }

    override fun provideRandomGenerator(): RandomGenerator =
        resolveDependencyRule(randomGeneratorRule) { super.provideRandomGenerator() }

    override fun provideDbManager(localDbManager: LocalDbManager,
                                  remoteDbManager: RemoteDbManager,
                                  secureDataManager: SecureDataManager,
                                  loginInfoManager: LoginInfoManager,
                                  preferencesManager: PreferencesManager,
                                  sessionEventsManager: SessionEventsManager,
                                  timeHelper: TimeHelper): DbManager =
        resolveDependencyRule(dbManagerRule) { super.provideDbManager(localDbManager, remoteDbManager, secureDataManager, loginInfoManager, preferencesManager, sessionEventsManager, timeHelper) }

    override fun provideSecureDataManager(preferencesManager: PreferencesManager,
                                          keystoreManager: KeystoreManager,
                                          randomGenerator: RandomGenerator): SecureDataManager =
        resolveDependencyRule(secureDataManagerRule) { super.provideSecureDataManager(preferencesManager, keystoreManager, randomGenerator) }

    override fun provideDataManager(preferencesManager: PreferencesManager,
                                    loginInfoManager: LoginInfoManager,
                                    analyticsManager: AnalyticsManager,
                                    remoteDbManager: RemoteDbManager): DataManager =
        resolveDependencyRule(dataManagerRule) { super.provideDataManager(preferencesManager, loginInfoManager, analyticsManager, remoteDbManager) }

    override fun provideKeystoreManager(): KeystoreManager =
        resolveDependencyRule(keystoreManagerRule) { super.provideKeystoreManager() }

    override fun provideBluetoothComponentAdapter(): BluetoothComponentAdapter =
        resolveDependencyRule(bluetoothComponentAdapterRule) { super.provideBluetoothComponentAdapter() }

    override fun provideSessionEventsManager(ctx: Context,
                                             loginInfoManager: LoginInfoManager,
                                             sessionEventsLocalDbManager: SessionEventsLocalDbManager,
                                             preferencesManager: PreferencesManager,
                                             timeHelper: TimeHelper): SessionEventsManager =
        resolveDependencyRule(sessionEventsManagerRule) { super.provideSessionEventsManager(ctx, loginInfoManager, sessionEventsLocalDbManager, preferencesManager, timeHelper) }

    override fun provideLocalEventDbManager(ctx: Context,
                                            secureDataManager: SecureDataManager): SessionEventsLocalDbManager =
        resolveDependencyRule(sessionEventsLocalDbManagerRule) { super.provideLocalEventDbManager(ctx, secureDataManager) }

    private inline fun <reified T> resolveDependencyRule(dependencyRule: DependencyRule, provider: () -> T): T =
        when (dependencyRule) {
            is RealRule -> provider()
            is MockRule -> mock()
            is SpyRule -> spy(provider())
            is ReplaceRule<*> -> dependencyRule.replacementProvider() as T
        }
}
