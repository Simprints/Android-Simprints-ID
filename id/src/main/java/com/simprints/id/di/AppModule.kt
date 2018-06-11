package com.simprints.id.di

import android.bluetooth.BluetoothAdapter
import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.simprints.id.Application
import com.simprints.id.controllers.Setup
import com.simprints.id.data.DataManager
import com.simprints.id.data.DataManagerImpl
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.AnalyticsManagerImpl
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.DbManagerImpl
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.realm.RealmDbManagerImpl
import com.simprints.id.data.db.remote.FirebaseManagerImpl
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.sync.SyncManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.loginInfo.LoginInfoManagerImpl
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.events.RecentEventsPreferencesManager
import com.simprints.id.data.prefs.events.RecentEventsPreferencesManagerImpl
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.data.secure.SecureDataManagerImpl
import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.data.secure.keystore.KeystoreManagerImpl
import com.simprints.id.services.sync.SyncClient
import com.simprints.id.services.sync.SyncService
import com.simprints.id.tools.*
import com.simprints.id.tools.utils.AndroidResourcesHelper
import com.simprints.id.tools.utils.AndroidResourcesHelperImpl
import com.simprints.id.tools.utils.NetworkUtils
import com.simprints.libscanner.bluetooth.BluetoothComponentAdapter
import com.simprints.libscanner.bluetooth.android.AndroidBluetoothAdapter
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

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
    open fun provideLoginInfoManager(improvedSharedPreferences: ImprovedSharedPreferences): LoginInfoManager = LoginInfoManagerImpl(improvedSharedPreferences)

    @Provides
    @Singleton
    open fun provideDbManager(localDbManager: LocalDbManager,
                              remoteDbManager: RemoteDbManager,
                              secureDataManager: SecureDataManager,
                              loginInfoManager: LoginInfoManager,
                              preferencesManager: PreferencesManager): DbManager =
        DbManagerImpl(localDbManager, remoteDbManager, secureDataManager, loginInfoManager, preferencesManager)

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
    open fun provideAnalyticsManager(loginInfoManager: LoginInfoManager,
                                     preferencesManager: PreferencesManager,
                                     firebaseAnalytics: FirebaseAnalytics): AnalyticsManager = AnalyticsManagerImpl(loginInfoManager, preferencesManager, firebaseAnalytics)

    @Provides
    @Singleton
    open fun provideKeystoreManager(): KeystoreManager = KeystoreManagerImpl(app)

    @Provides
    @Singleton
    open fun provideRandomGenerator(): RandomGenerator = RandomGeneratorImpl()

    @Provides
    @Singleton
    open fun provideSecureDataManager(preferencesManager: PreferencesManager, keystoreManager: KeystoreManager, randomGenerator: RandomGenerator): SecureDataManager =
        SecureDataManagerImpl(keystoreManager, preferencesManager, randomGenerator)

    @Provides
    @Singleton
    open fun provideDataManager(preferencesManager: PreferencesManager,
                                loginInfoManager: LoginInfoManager,
                                analyticsManager: AnalyticsManager,
                                remoteDbManager: RemoteDbManager): DataManager =
        DataManagerImpl(preferencesManager, loginInfoManager, analyticsManager, remoteDbManager)

    @Provides
    @Singleton
    fun provideAppState(): AppState = AppState()

    @Provides
    @Singleton
    fun provideNetworkUtils(): NetworkUtils = NetworkUtils(app)

    @Provides
    @Singleton
    open fun provideBluetoothComponentAdapter(): BluetoothComponentAdapter = AndroidBluetoothAdapter(BluetoothAdapter.getDefaultAdapter())

    @Provides
    @Singleton
    fun provideSetup(preferencesManager: PreferencesManager,
                     dbManager: DbManager,
                     loginInfoManager: LoginInfoManager,
                     analyticsManager: AnalyticsManager,
                     appState: AppState,
                     networkUtils: NetworkUtils,
                     bluetoothComponentAdapter: BluetoothComponentAdapter): Setup = Setup(preferencesManager, dbManager, loginInfoManager, analyticsManager, appState, networkUtils, bluetoothComponentAdapter)

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

    @Provides
    fun provideAndroidResourcesHelper(ctx: Context): AndroidResourcesHelper =
        AndroidResourcesHelperImpl(ctx)

    @Provides
    fun provideSyncClient(app: Application): SyncClient =
        SyncService.getClient(app)

    @Provides
    fun provideSyncManager(analyticsManager: AnalyticsManager, syncClient: SyncClient): SyncManager =
        SyncManager(analyticsManager, syncClient)
}
