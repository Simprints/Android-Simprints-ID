package com.simprints.id

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.simprints.id.data.DataManager
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.prefs.loginInfo.LoginInfoManager
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.di.AppModule
import com.simprints.id.shared.mock
import com.simprints.id.testUtils.roboletric.setupFakeKeyStore
import org.mockito.Mockito.spy

open class TestAppModule(app: Application,
                         var localDbManagerSpy: Boolean? = null,
                         var remoteDbManagerSpy: Boolean? = null,
                         var dbManagerSpy: Boolean? = null,
                         val secureDataManagerSpy: Boolean? = null,
                         var dataManagerSpy: Boolean? = null,
                         var loginInfoManagerSpy: Boolean? = null,
                         var analyticsManagerSpy: Boolean? = null) : AppModule(app) {

    override fun provideLocalDbManager(ctx: Context): LocalDbManager =
        buildSpyIsRequired(localDbManagerSpy, { super.provideLocalDbManager(ctx) })

    override fun provideAnalyticsManager(firebaseAnalytics: FirebaseAnalytics): AnalyticsManager =
        buildSpyIsRequired(analyticsManagerSpy, { super.provideAnalyticsManager(firebaseAnalytics) })

    override fun provideRemoteDbManager(ctx: Context): RemoteDbManager =
        buildSpyIsRequired(remoteDbManagerSpy, { super.provideRemoteDbManager(ctx) })

    override fun provideDbManager(localDbManager: LocalDbManager, remoteDbManager: RemoteDbManager, secureDataManager: SecureDataManager, loginInfoManager: LoginInfoManager): DbManager =
        buildSpyIsRequired(dbManagerSpy, { super.provideDbManager(localDbManager, remoteDbManager, secureDataManager, loginInfoManager) })

    override fun provideSecureDataManager(preferencesManager: PreferencesManager, keystoreManager: KeystoreManager): SecureDataManager =
        buildSpyIsRequired(secureDataManagerSpy, { super.provideSecureDataManager(preferencesManager, keystoreManager) })

    override fun provideLoginInfoManager(improvedSharedPreferences: ImprovedSharedPreferences): LoginInfoManager =
        buildSpyIsRequired(loginInfoManagerSpy, { super.provideLoginInfoManager(improvedSharedPreferences) })

    override fun provideDataManager(app: Application,
                                    preferencesManager: PreferencesManager,
                                    dbManager: DbManager,
                                    analyticsManager: AnalyticsManager,
                                    loginInfoManager: LoginInfoManager): DataManager =
        buildSpyIsRequired(dataManagerSpy, { super.provideDataManager(app, preferencesManager, dbManager, analyticsManager, loginInfoManager) })

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
