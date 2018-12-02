package com.simprints.id.di

import android.bluetooth.BluetoothAdapter
import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.simprints.id.Application
import com.simprints.id.data.DataManager
import com.simprints.id.data.DataManagerImpl
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.AnalyticsManagerImpl
import com.simprints.id.data.analytics.eventData.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventData.controllers.domain.SessionEventsManagerImpl
import com.simprints.id.data.analytics.eventData.controllers.local.RealmSessionEventsDbManagerImpl
import com.simprints.id.data.analytics.eventData.controllers.local.SessionEventsLocalDbManager
import com.simprints.id.data.consent.LongConsentManager
import com.simprints.id.data.consent.LongConsentManagerImpl
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.DbManagerImpl
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.realm.RealmDbManagerImpl
import com.simprints.id.data.db.remote.FirebaseManagerImpl
import com.simprints.id.data.db.remote.RemoteDbManager
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
import com.simprints.id.scanner.ScannerManager
import com.simprints.id.scanner.ScannerManagerImpl
import com.simprints.id.secure.SecureApiInterface
import com.simprints.id.services.scheduledSync.peopleDownSync.SyncStatusDatabase
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilderImpl
import com.simprints.id.services.scheduledSync.peopleUpsync.PeopleUpSyncMaster
import com.simprints.id.services.scheduledSync.peopleUpsync.periodicFlusher.PeopleUpSyncPeriodicFlusherMaster
import com.simprints.id.services.scheduledSync.peopleUpsync.uploader.PeopleUpSyncUploaderMaster
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManager
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManagerImpl
import com.simprints.id.tools.RandomGenerator
import com.simprints.id.tools.RandomGeneratorImpl
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.TimeHelperImpl
import com.simprints.id.tools.extensions.deviceId
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
    open fun providePeopleUpSyncMaster() =
        PeopleUpSyncMaster(
            PeopleUpSyncUploaderMaster(),
            PeopleUpSyncPeriodicFlusherMaster()
        )

    @Provides
    @Singleton
    open fun provideDbManager(localDbManager: LocalDbManager,
                              remoteDbManager: RemoteDbManager,
                              secureDataManager: SecureDataManager,
                              loginInfoManager: LoginInfoManager,
                              preferencesManager: PreferencesManager,
                              sessionEventsManager: SessionEventsManager,
                              timeHelper: TimeHelper,
                              peopleUpSyncMaster: PeopleUpSyncMaster): DbManager =
        DbManagerImpl(localDbManager, remoteDbManager, secureDataManager, loginInfoManager, preferencesManager, sessionEventsManager, timeHelper, peopleUpSyncMaster)

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
    open fun provideSimNetworkUtils(ctx: Context): SimNetworkUtils = SimNetworkUtilsImpl(ctx)

    @Provides
    @Singleton
    open fun provideBluetoothComponentAdapter(): BluetoothComponentAdapter =
        AndroidBluetoothAdapter(BluetoothAdapter.getDefaultAdapter())

    @Provides
    open fun provideSecureApiInterface(): SecureApiInterface = SimApiClient(SecureApiInterface::class.java, SecureApiInterface.baseUrl).api

    @Provides
    @Singleton
    open fun provideScannerManager(preferencesManager: PreferencesManager, analyticsManager: AnalyticsManager, bluetoothComponentAdapter: BluetoothComponentAdapter): ScannerManager =
        ScannerManagerImpl(preferencesManager, analyticsManager, bluetoothComponentAdapter)

    @Provides
    @Singleton
    fun provideTimeHelper(): TimeHelper = TimeHelperImpl()

    @Provides
    fun provideAndroidResourcesHelper(ctx: Context): AndroidResourcesHelper =
        AndroidResourcesHelperImpl(ctx)

    @Provides
    @Singleton
    open fun provideSessionEventsLocalDbManager(ctx: Context,
                                                secureDataManager: SecureDataManager): SessionEventsLocalDbManager =
        RealmSessionEventsDbManagerImpl(ctx, secureDataManager)

    @Provides
    @Singleton
    open fun provideSessionEventsManager(ctx: Context,
                                         sessionEventsSyncManager: SessionEventsSyncManager,
                                         sessionEventsLocalDbManager: SessionEventsLocalDbManager,
                                         preferencesManager: PreferencesManager,
                                         timeHelper: TimeHelper,
                                         analyticsManager: AnalyticsManager): SessionEventsManager =
        SessionEventsManagerImpl(ctx.deviceId, sessionEventsSyncManager, sessionEventsLocalDbManager, preferencesManager, timeHelper, analyticsManager)

    @Provides
    @Singleton
    open fun provideScheduledSessionsSyncManager(): SessionEventsSyncManager =
        SessionEventsSyncManagerImpl()

    @Provides
    @Singleton
    open fun provideNewSyncStatusDatabase(): SyncStatusDatabase =
        SyncStatusDatabase.getDatabase(provideContext())

    @Provides
    @Singleton
    open fun provideSyncScopesBuilder(loginInfoManager: LoginInfoManager, preferencesManager: PreferencesManager): SyncScopesBuilder =
        SyncScopesBuilderImpl(loginInfoManager, preferencesManager)
}
