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
import org.mockito.Mockito.spy

open class AppModuleForAnyTests(app: Application,
                                open var localDbManagerSpy: Boolean? = null,
                                open var remoteDbManagerSpy: Boolean? = null,
                                open var dbManagerSpy: Boolean? = null,
                                open val secureDataManagerSpy: Boolean? = null,
                                open var dataManagerSpy: Boolean? = null,
                                open var loginInfoManagerSpy: Boolean? = null,
                                open var randomGeneratorSpy: Boolean? = null,
                                open var analyticsManagerSpy: Boolean? = null) : AppModule(app) {

    override fun provideLocalDbManager(ctx: Context): LocalDbManager =
        buildSpyIsRequired(localDbManagerSpy, { super.provideLocalDbManager(ctx) })

    override fun provideAnalyticsManager(loginInfoManager: LoginInfoManager,
                                     preferencesManager: PreferencesManager,
                                     firebaseAnalytics: FirebaseAnalytics): AnalyticsManager =
        buildSpyIsRequired(analyticsManagerSpy, { super.provideAnalyticsManager(loginInfoManager, preferencesManager, firebaseAnalytics) })

    override fun provideRemoteDbManager(ctx: Context): RemoteDbManager =
        buildSpyIsRequired(remoteDbManagerSpy, { super.provideRemoteDbManager(ctx) })

    override fun provideLoginInfoManager(improvedSharedPreferences: ImprovedSharedPreferences): LoginInfoManager =
        buildSpyIsRequired(loginInfoManagerSpy, { super.provideLoginInfoManager(improvedSharedPreferences) })

    override fun provideRandomGenerator(): RandomGenerator =
        buildSpyIsRequired(randomGeneratorSpy, { super.provideRandomGenerator() })

    override fun provideDbManager(localDbManager: LocalDbManager,
                                  remoteDbManager: RemoteDbManager,
                                  secureDataManager: SecureDataManager,
                                  loginInfoManager: LoginInfoManager,
                                  preferencesManager: PreferencesManager): DbManager =
        buildSpyIsRequired(dbManagerSpy, { super.provideDbManager(localDbManager, remoteDbManager, secureDataManager, loginInfoManager, preferencesManager) })

    override fun provideSecureDataManager(preferencesManager: PreferencesManager,
                                          keystoreManager: KeystoreManager,
                                          randomGenerator: RandomGenerator): SecureDataManager =
        buildSpyIsRequired(secureDataManagerSpy, { super.provideSecureDataManager(preferencesManager, keystoreManager, randomGenerator) })

    override fun provideDataManager(preferencesManager: PreferencesManager,
                                loginInfoManager: LoginInfoManager,
                                secureDataManager: SecureDataManager,
                                analyticsManager: AnalyticsManager,
                                dbManager: DbManager): DataManager =
        buildSpyIsRequired(dataManagerSpy, { super.provideDataManager(preferencesManager, loginInfoManager, secureDataManager, analyticsManager, dbManager) })

    override fun provideKeystoreManager(): KeystoreManager = setupFakeKeyStore()

    private inline fun <reified T> buildSpyIsRequired(spyRequired: Boolean?, builder: () -> T): T =
        spyRequired?.let {
            if (it) {
                spy(builder())
            } else {
                mock()
            }
        } ?: builder()
}
