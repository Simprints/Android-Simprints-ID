package com.simprints.id.shared

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.simprints.id.Application
import com.simprints.id.data.DataManager
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.eventData.SessionEventsLocalDbManager
import com.simprints.id.data.analytics.eventData.SessionEventsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.di.AppModule
import com.simprints.id.services.scheduledSync.peopleSync.ScheduledPeopleSyncManager
import com.simprints.id.services.scheduledSync.sessionSync.ScheduledSessionsSyncManager
import com.simprints.id.shared.DependencyRule.RealRule
import com.simprints.id.tools.RandomGenerator
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.utils.SimNetworkUtils
import com.simprints.libscanner.bluetooth.BluetoothComponentAdapter

open class AppModuleForAnyTests(app: Application,
                                open var localDbManagerRule: DependencyRule = RealRule,
                                open var remoteDbManagerRule: DependencyRule = RealRule,
                                open var dbManagerRule: DependencyRule = RealRule,
                                open var secureDataManagerRule: DependencyRule = RealRule,
                                open var dataManagerRule: DependencyRule = RealRule,
                                open var loginInfoManagerRule: DependencyRule = RealRule,
                                open var randomGeneratorRule: DependencyRule = RealRule,
                                open var keystoreManagerRule: DependencyRule = RealRule,
                                open var analyticsManagerRule: DependencyRule = RealRule,
                                open var bluetoothComponentAdapterRule: DependencyRule = RealRule,
                                open var sessionEventsManagerRule: DependencyRule = RealRule,
                                open var sessionEventsLocalDbManagerRule: DependencyRule = RealRule,
                                open var scheduledPeopleSyncManagerRule: DependencyRule = RealRule,
                                open var scheduledSessionsSyncManagerRule: DependencyRule = RealRule,
                                open var simNetworkUtilsRule: DependencyRule = RealRule) : AppModule(app) {

    override fun provideLocalDbManager(ctx: Context): LocalDbManager =
        localDbManagerRule.resolveDependency { super.provideLocalDbManager(ctx) }

    override fun provideAnalyticsManager(loginInfoManager: LoginInfoManager,
                                         preferencesManager: PreferencesManager,
                                         firebaseAnalytics: FirebaseAnalytics): AnalyticsManager =
        analyticsManagerRule.resolveDependency { super.provideAnalyticsManager(loginInfoManager, preferencesManager, firebaseAnalytics) }

    override fun provideRemoteDbManager(ctx: Context): RemoteDbManager =
        remoteDbManagerRule.resolveDependency { super.provideRemoteDbManager(ctx) }

    override fun provideLoginInfoManager(improvedSharedPreferences: ImprovedSharedPreferences): LoginInfoManager =
        loginInfoManagerRule.resolveDependency { super.provideLoginInfoManager(improvedSharedPreferences) }

    override fun provideRandomGenerator(): RandomGenerator =
        randomGeneratorRule.resolveDependency { super.provideRandomGenerator() }

    override fun provideDbManager(localDbManager: LocalDbManager,
                                  remoteDbManager: RemoteDbManager,
                                  secureDataManager: SecureDataManager,
                                  loginInfoManager: LoginInfoManager,
                                  preferencesManager: PreferencesManager,
                                  sessionEventsManager: SessionEventsManager,
                                  timeHelper: TimeHelper): DbManager =
        dbManagerRule.resolveDependency { super.provideDbManager(localDbManager, remoteDbManager, secureDataManager, loginInfoManager, preferencesManager, sessionEventsManager, timeHelper) }

    override fun provideSecureDataManager(preferencesManager: PreferencesManager,
                                          keystoreManager: KeystoreManager,
                                          randomGenerator: RandomGenerator): SecureDataManager =
        secureDataManagerRule.resolveDependency { super.provideSecureDataManager(preferencesManager, keystoreManager, randomGenerator) }

    override fun provideDataManager(preferencesManager: PreferencesManager,
                                    loginInfoManager: LoginInfoManager,
                                    analyticsManager: AnalyticsManager,
                                    remoteDbManager: RemoteDbManager): DataManager =
        dataManagerRule.resolveDependency { super.provideDataManager(preferencesManager, loginInfoManager, analyticsManager, remoteDbManager) }

    override fun provideKeystoreManager(): KeystoreManager =
        keystoreManagerRule.resolveDependency { super.provideKeystoreManager() }

    override fun provideBluetoothComponentAdapter(): BluetoothComponentAdapter =
        bluetoothComponentAdapterRule.resolveDependency { super.provideBluetoothComponentAdapter() }

    override fun provideScheduledPeopleSyncManager(preferencesManager: PreferencesManager): ScheduledPeopleSyncManager =
        scheduledPeopleSyncManagerRule.resolveDependency { super.provideScheduledPeopleSyncManager(preferencesManager) }

    override fun provideScheduledSessionsSyncManager(preferencesManager: PreferencesManager): ScheduledSessionsSyncManager =
        scheduledSessionsSyncManagerRule.resolveDependency { super.provideScheduledSessionsSyncManager(preferencesManager) }

    override fun provideSessionEventsManager(ctx: Context,
                                             loginInfoManager: LoginInfoManager,
                                             sessionEventsLocalDbManager: SessionEventsLocalDbManager,
                                             preferencesManager: PreferencesManager,
                                             timeHelper: TimeHelper,
                                             remoteDbManager: RemoteDbManager,
                                             analyticsManager: AnalyticsManager): SessionEventsManager =

        sessionEventsManagerRule.resolveDependency { super.provideSessionEventsManager(ctx, loginInfoManager, sessionEventsLocalDbManager, preferencesManager, timeHelper, remoteDbManager, analyticsManager) }

    override fun provideSessionEventsLocalDbManager(ctx: Context,
                                            secureDataManager: SecureDataManager): SessionEventsLocalDbManager =
        sessionEventsLocalDbManagerRule.resolveDependency { super.provideSessionEventsLocalDbManager(ctx, secureDataManager) }

    override fun provideSimNetworkUtils(ctx: Context): SimNetworkUtils =
        simNetworkUtilsRule.resolveDependency { super.provideSimNetworkUtils(ctx) }
}
