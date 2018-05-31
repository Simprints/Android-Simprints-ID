package com.simprints.id.di

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.simprints.id.Application
import com.simprints.id.controllers.Setup
import com.simprints.id.data.DataManager
import com.simprints.id.data.DataManagerImpl
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.FirebaseAnalyticsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.DbManagerImpl
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.realm.RealmDbManagerImpl
import com.simprints.id.data.db.remote.FirebaseManagerImpl
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.events.RecentEventsPreferencesManager
import com.simprints.id.data.prefs.events.RecentEventsPreferencesManagerImpl
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.prefs.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.loginInfo.LoginInfoManagerImpl
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.data.secure.SecureDataManagerImpl
import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.data.secure.keystore.KeystoreManagerImpl
import com.simprints.id.tools.AppState
import com.simprints.id.tools.NotificationFactory
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.TimeHelperImpl
import com.simprints.id.tools.utils.NetworkUtils
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by fabiotuzza on 16/01/2018.
 */
@Module
open class AppModule(val app: Application) {

    @Provides
    @Singleton
    fun provideApplication(): Application = app

    @Provides
    @Singleton
    fun provideContext(): Context = app

    @Provides
    @Singleton
    open fun provideLocalDbManager(ctx: Context): LocalDbManager = RealmDbManagerImpl(ctx)

    @Provides
    @Singleton
    open fun provideRemoteDbManager(ctx: Context): RemoteDbManager = FirebaseManagerImpl(ctx)

    @Provides
    @Singleton
    open fun provideDbManager(localDbManager: LocalDbManager, remoteDbManager: RemoteDbManager, secureDataManager: SecureDataManager, loginInfoManager: LoginInfoManager): DbManager = DbManagerImpl(localDbManager, remoteDbManager, secureDataManager, loginInfoManager)

    @Provides
    @Singleton
    fun provideFirebaseAnalytics(app: Application): FirebaseAnalytics =
        FirebaseAnalytics.getInstance(app).apply {
            setMinimumSessionDuration(0)
        }

    @Provides
    @Singleton
    fun provideRecentEventsPreferencesManager(prefs: ImprovedSharedPreferences): RecentEventsPreferencesManager = RecentEventsPreferencesManagerImpl(prefs)

    @Provides
    @Singleton
    open fun provideAnalyticsManager(firebaseAnalytics: FirebaseAnalytics): AnalyticsManager = FirebaseAnalyticsManager(firebaseAnalytics)

    @Provides
    @Singleton
    open fun provideKeystoreManager(): KeystoreManager = KeystoreManagerImpl(app)

    @Provides
    @Singleton
    open fun provideSecureDataManager(preferencesManager: PreferencesManager, keystoreManager: KeystoreManager): SecureDataManager = SecureDataManagerImpl(keystoreManager, preferencesManager)

    @Provides
    @Singleton
    open fun provideLoginInfoManager(improvedSharedPreferences: ImprovedSharedPreferences): LoginInfoManager = LoginInfoManagerImpl(improvedSharedPreferences)

    @Provides
    @Singleton
    open fun provideDataManager(app: Application,
                                preferencesManager: PreferencesManager,
                                dbManager: DbManager,
                                analyticsManager: AnalyticsManager,
                                loginInfoManager: LoginInfoManager): DataManager =
        DataManagerImpl(app, preferencesManager, dbManager, analyticsManager, loginInfoManager)

    @Provides
    @Singleton
    fun provideAppState(): AppState = AppState()

    @Provides
    @Singleton
    fun provideNetworkUtils(): NetworkUtils = NetworkUtils(app)

    @Provides
    @Singleton
    fun provideSetup(dataManager: DataManager, appState: AppState, networkUtils: NetworkUtils): Setup = Setup.getInstance(dataManager, appState, networkUtils)

    @Provides
    @Singleton
    fun provideTimeHelper(): TimeHelper = TimeHelperImpl()

    @Provides
    @Singleton
    fun provideNotificationFactory(app: Application): NotificationFactory {
        val factory = NotificationFactory(app)
        factory.initSyncNotificationChannel()
        return factory
    }
}
