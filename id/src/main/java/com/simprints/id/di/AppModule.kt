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
import com.simprints.id.data.analytics.eventData.SessionEventsLocalDbManager
import com.simprints.id.data.analytics.eventData.SessionEventsManager
import com.simprints.id.data.analytics.eventData.SessionEventsManagerImpl
import com.simprints.id.data.analytics.eventData.realm.RealmSessionEventsDbManagerImpl
import com.simprints.id.data.consent.LongConsentManager
import com.simprints.id.data.consent.LongConsentManagerImpl
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.DbManagerImpl
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.realm.RealmDbManagerImpl
import com.simprints.id.data.db.remote.FirebaseManagerImpl
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.people.RemotePeopleManager
import com.simprints.id.data.db.remote.people.RemotePeopleManagerImpl
import com.simprints.id.data.db.remote.project.RemoteProjectManager
import com.simprints.id.data.db.remote.project.RemoteProjectManagerImpl
import com.simprints.id.data.db.remote.sessions.RemoteSessionsManager
import com.simprints.id.data.db.remote.sessions.RemoteSessionsManagerImpl
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
import com.simprints.id.network.SimApiClient
import com.simprints.id.secure.SecureApiInterface
import com.simprints.id.services.progress.notifications.NotificationFactory
import com.simprints.id.services.scheduledSync.peopleSync.ScheduledPeopleSyncManager
import com.simprints.id.services.scheduledSync.sessionSync.ScheduledSessionsSyncManager
import com.simprints.id.services.sync.SyncClient
import com.simprints.id.services.sync.SyncService
import com.simprints.id.tools.*
import com.simprints.id.tools.utils.AndroidResourcesHelper
import com.simprints.id.tools.utils.AndroidResourcesHelperImpl
import com.simprints.id.tools.utils.SimNetworkUtils
import com.simprints.id.tools.utils.SimNetworkUtilsImpl
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
                              preferencesManager: PreferencesManager,
                              sessionEventsManager: SessionEventsManager,
                              remotePeopleManager: RemotePeopleManager,
                              remoteProjectManager: RemoteProjectManager,
                              timeHelper: TimeHelper): DbManager =
        DbManagerImpl(localDbManager, remoteDbManager, secureDataManager, loginInfoManager, preferencesManager, sessionEventsManager, remotePeopleManager, remoteProjectManager, timeHelper)

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
    open fun provideLongConsentManager(ctx: Context, loginInfoManager: LoginInfoManager, analyticsManager: AnalyticsManager):
        LongConsentManager = LongConsentManagerImpl(ctx, loginInfoManager, analyticsManager)

    @Provides
    @Singleton
    fun provideAppState(): AppState = AppState()

    @Provides
    @Singleton
    open fun provideSimNetworkUtils(ctx: Context): SimNetworkUtils = SimNetworkUtilsImpl(ctx)

    @Provides
    @Singleton
    open fun provideBluetoothComponentAdapter(): BluetoothComponentAdapter = AndroidBluetoothAdapter(BluetoothAdapter.getDefaultAdapter())

    @Provides
    open fun provideSecureApiInterface(): SecureApiInterface = SimApiClient(SecureApiInterface::class.java, SecureApiInterface.baseUrl).api

    @Provides
    @Singleton
    fun provideSetup(preferencesManager: PreferencesManager,
                     dbManager: DbManager,
                     loginInfoManager: LoginInfoManager,
                     analyticsManager: AnalyticsManager,
                     appState: AppState,
                     simNetworkUtils: SimNetworkUtils,
                     bluetoothComponentAdapter: BluetoothComponentAdapter,
                     sessionEventsManager: SessionEventsManager,
                     timeHelper: TimeHelper): Setup =
        Setup(preferencesManager, dbManager, loginInfoManager, analyticsManager, appState, simNetworkUtils, bluetoothComponentAdapter, sessionEventsManager, timeHelper)

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

    @Provides
    @Singleton
    open fun provideSessionEventsLocalDbManager(ctx: Context,
                                                secureDataManager: SecureDataManager): SessionEventsLocalDbManager =
        RealmSessionEventsDbManagerImpl(ctx, secureDataManager)

    @Provides
    @Singleton
    open fun provideSessionEventsManager(ctx: Context,
                                         loginInfoManager: LoginInfoManager,
                                         sessionEventsLocalDbManager: SessionEventsLocalDbManager,
                                         preferencesManager: PreferencesManager,
                                         timeHelper: TimeHelper,
                                         sessionEventsManager: RemoteSessionsManager,
                                         analyticsManager: AnalyticsManager): SessionEventsManager =
        SessionEventsManagerImpl(ctx, sessionEventsLocalDbManager, loginInfoManager, preferencesManager, timeHelper, sessionEventsManager, analyticsManager)

    @Provides
    open fun provideScheduledPeopleSyncManager(preferencesManager: PreferencesManager): ScheduledPeopleSyncManager =
        ScheduledPeopleSyncManager(preferencesManager)

    @Provides
    open fun provideScheduledSessionsSyncManager(): ScheduledSessionsSyncManager =
        ScheduledSessionsSyncManager()

    @Provides
    @Singleton
    open fun provideRemotePeopleManager(remoteDbManager: RemoteDbManager): RemotePeopleManager = RemotePeopleManagerImpl(remoteDbManager)

    @Provides
    @Singleton
    open fun provideRemoteProjectManager(remoteDbManager: RemoteDbManager): RemoteProjectManager = RemoteProjectManagerImpl(remoteDbManager)

    @Provides
    @Singleton
    open fun provideRemoteSessionsManager(remoteDbManager: RemoteDbManager): RemoteSessionsManager = RemoteSessionsManagerImpl(remoteDbManager)
}
