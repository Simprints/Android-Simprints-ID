package com.simprints.id.shared

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.simprints.id.Application
import com.simprints.id.data.DataManager
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.di.AppModule
import com.simprints.id.tools.RandomGenerator
import com.simprints.id.shared.MockRule.*
import org.mockito.Mockito.spy

open class AppModuleForAnyTests(app: Application,
                                open var localDbManagerRule: MockRule = REAL,
                                open var remoteDbManagerRule: MockRule = REAL,
                                open var dbManagerRule: MockRule = REAL,
                                open var secureDataManagerRule: MockRule = REAL,
                                open var dataManagerRule: MockRule = REAL,
                                open var loginInfoManagerRule: MockRule = REAL,
                                open var randomGeneratorRule: MockRule = REAL,
                                open var analyticsManagerRule: MockRule = REAL) : AppModule(app) {

    override fun provideLocalDbManager(ctx: Context): LocalDbManager =
        resolveMockRule(localDbManagerRule, { super.provideLocalDbManager(ctx) })

    override fun provideAnalyticsManager(loginInfoManager: LoginInfoManager,
                                     preferencesManager: PreferencesManager,
                                     firebaseAnalytics: FirebaseAnalytics): AnalyticsManager =
        resolveMockRule(analyticsManagerRule, { super.provideAnalyticsManager(loginInfoManager, preferencesManager, firebaseAnalytics) })

    override fun provideRemoteDbManager(ctx: Context): RemoteDbManager =
        resolveMockRule(remoteDbManagerRule, { super.provideRemoteDbManager(ctx) })

    override fun provideLoginInfoManager(improvedSharedPreferences: ImprovedSharedPreferences): LoginInfoManager =
        resolveMockRule(loginInfoManagerRule, { super.provideLoginInfoManager(improvedSharedPreferences) })

    override fun provideRandomGenerator(): RandomGenerator =
        resolveMockRule(randomGeneratorRule, { super.provideRandomGenerator() })

    override fun provideDbManager(localDbManager: LocalDbManager,
                                  remoteDbManager: RemoteDbManager,
                                  secureDataManager: SecureDataManager,
                                  loginInfoManager: LoginInfoManager,
                                  preferencesManager: PreferencesManager): DbManager =
        resolveMockRule(dbManagerRule, { super.provideDbManager(localDbManager, remoteDbManager, secureDataManager, loginInfoManager, preferencesManager) })

    override fun provideSecureDataManager(preferencesManager: PreferencesManager,
                                          keystoreManager: KeystoreManager,
                                          randomGenerator: RandomGenerator): SecureDataManager =
        resolveMockRule(secureDataManagerRule, { super.provideSecureDataManager(preferencesManager, keystoreManager, randomGenerator) })

    override fun provideDataManager(preferencesManager: PreferencesManager,
                                loginInfoManager: LoginInfoManager,
                                analyticsManager: AnalyticsManager,
                                remoteDbManager: RemoteDbManager): DataManager =
        resolveMockRule(dataManagerRule, { super.provideDataManager(preferencesManager, loginInfoManager, analyticsManager, remoteDbManager) })

    override fun provideKeystoreManager(): KeystoreManager = setupFakeKeyStore()

    private inline fun <reified T> resolveMockRule(mockRule: MockRule, provider: () -> T): T =
        when (mockRule) {
            REAL -> provider()
            MOCK -> mock()
            SPY -> spy(provider())
        }
}
