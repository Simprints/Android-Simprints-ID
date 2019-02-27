package com.simprints.id.commontesttools.di

import android.content.Context
import com.simprints.id.Application
import com.simprints.id.commontesttools.di.DependencyRule.RealRule
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.controllers.local.SessionEventsLocalDbManager
import com.simprints.id.data.consent.LongConsentManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.room.SyncStatusDatabase
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.people.RemotePeopleManager
import com.simprints.id.data.db.remote.project.RemoteProjectManager
import com.simprints.id.data.db.remote.sessions.RemoteSessionsManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.di.AppModule
import com.simprints.id.scanner.ScannerManager
import com.simprints.id.secure.SecureApiInterface
import com.simprints.id.services.scheduledSync.SyncSchedulerHelper
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.DownSyncManager
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.tasks.CountTask
import com.simprints.id.services.scheduledSync.peopleDownSync.tasks.DownSyncTask
import com.simprints.id.services.scheduledSync.peopleUpsync.PeopleUpSyncMaster
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManager
import com.simprints.id.tools.RandomGenerator
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.utils.LocationProvider
import com.simprints.id.tools.utils.SimNetworkUtils
import com.simprints.libscanner.bluetooth.BluetoothComponentAdapter

class TestAppModule(app: Application,
                    var localDbManagerRule: DependencyRule = RealRule,
                    var remoteDbManagerRule: DependencyRule = RealRule,
                    var remotePeopleManagerRule: DependencyRule = RealRule,
                    var remoteProjectManagerRule: DependencyRule = RealRule,
                    var remoteSessionsManagerRule: DependencyRule = RealRule,
                    var dbManagerRule: DependencyRule = RealRule,
                    var secureDataManagerRule: DependencyRule = RealRule,
                    var dataManagerRule: DependencyRule = RealRule,
                    var loginInfoManagerRule: DependencyRule = RealRule,
                    var randomGeneratorRule: DependencyRule = RealRule,
                    var keystoreManagerRule: DependencyRule = RealRule,
                    var crashReportManagerRule: DependencyRule = RealRule,
                    var bluetoothComponentAdapterRule: DependencyRule = RealRule,
                    var sessionEventsManagerRule: DependencyRule = RealRule,
                    var sessionEventsLocalDbManagerRule: DependencyRule = RealRule,
                    var scheduledSessionsSyncManagerRule: DependencyRule = RealRule,
                    var simNetworkUtilsRule: DependencyRule = RealRule,
                    var secureApiInterfaceRule: DependencyRule = RealRule,
                    var longConsentManagerRule: DependencyRule = RealRule,
                    var scannerManagerRule: DependencyRule = RealRule,
                    var peopleUpSyncMasterRule: DependencyRule = RealRule,
                    var syncStatusDatabaseRule: DependencyRule = RealRule,
                    var syncScopesBuilderRule: DependencyRule = RealRule,
                    var countTaskRule: DependencyRule = RealRule,
                    var downSyncTaskRule: DependencyRule = RealRule,
                    var syncSchedulerHelperRule: DependencyRule = RealRule,
                    var downSyncManagerRule: DependencyRule = RealRule,
                    var locationProverRule: DependencyRule = RealRule) : AppModule(app) {

    override fun provideLocalDbManager(ctx: Context): LocalDbManager =
        localDbManagerRule.resolveDependency { super.provideLocalDbManager(ctx) }

    override fun provideCrashManager(): CrashReportManager =
        crashReportManagerRule.resolveDependency { super.provideCrashManager() }

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
                                  remotePeopleManager: RemotePeopleManager,
                                  remoteProjectManager: RemoteProjectManager,
                                  timeHelper: TimeHelper,
                                  peopleUpSyncMaster: PeopleUpSyncMaster,
                                  database: SyncStatusDatabase): DbManager =
        dbManagerRule.resolveDependency { super.provideDbManager(localDbManager, remoteDbManager, secureDataManager, loginInfoManager, preferencesManager, sessionEventsManager, remotePeopleManager, remoteProjectManager, timeHelper, peopleUpSyncMaster, database) }

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

    override fun provideSecureApiInterface(): SecureApiInterface =
        secureApiInterfaceRule.resolveDependency { super.provideSecureApiInterface() }

    override fun provideScheduledSessionsSyncManager(): SessionEventsSyncManager =
        scheduledSessionsSyncManagerRule.resolveDependency { super.provideScheduledSessionsSyncManager() }

    override fun provideSessionEventsManager(ctx: Context,
                                             sessionEventsSyncManager: SessionEventsSyncManager,
                                             sessionEventsLocalDbManager: SessionEventsLocalDbManager,
                                             preferencesManager: PreferencesManager,
                                             timeHelper: TimeHelper,
                                             crashReportManager: CrashReportManager): SessionEventsManager =

        sessionEventsManagerRule.resolveDependency { super.provideSessionEventsManager(ctx, sessionEventsSyncManager, sessionEventsLocalDbManager, preferencesManager, timeHelper, crashReportManager) }

    override fun provideSessionEventsLocalDbManager(ctx: Context,
                                                    secureDataManager: SecureDataManager): SessionEventsLocalDbManager =
        sessionEventsLocalDbManagerRule.resolveDependency { super.provideSessionEventsLocalDbManager(ctx, secureDataManager) }

    override fun provideSimNetworkUtils(ctx: Context): SimNetworkUtils =
        simNetworkUtilsRule.resolveDependency { super.provideSimNetworkUtils(ctx) }

    override fun provideLongConsentManager(ctx: Context, loginInfoManager: LoginInfoManager, crashReportManager: CrashReportManager): LongConsentManager =
        longConsentManagerRule.resolveDependency { super.provideLongConsentManager(ctx, loginInfoManager, crashReportManager) }

    override fun provideScannerManager(preferencesManager: PreferencesManager,
                                       analyticsManager: AnalyticsManager,
                                       crashReportManager: CrashReportManager,
                                       bluetoothComponentAdapter: BluetoothComponentAdapter): ScannerManager =

        scannerManagerRule.resolveDependency { super.provideScannerManager(preferencesManager, analyticsManager, crashReportManager, bluetoothComponentAdapter) }

    override fun provideRemotePeopleManager(remoteDbManager: RemoteDbManager): RemotePeopleManager =
        remotePeopleManagerRule.resolveDependency { super.provideRemotePeopleManager(remoteDbManager) }

    override fun provideRemoteProjectManager(remoteDbManager: RemoteDbManager): RemoteProjectManager =
        remoteProjectManagerRule.resolveDependency { super.provideRemoteProjectManager(remoteDbManager) }

    override fun provideRemoteSessionsManager(remoteDbManager: RemoteDbManager): RemoteSessionsManager =
        remoteSessionsManagerRule.resolveDependency { super.provideRemoteSessionsManager(remoteDbManager) }

    override fun providePeopleUpSyncMaster(): PeopleUpSyncMaster =
        peopleUpSyncMasterRule.resolveDependency { super.providePeopleUpSyncMaster() }

    override fun provideSyncStatusDatabase(): SyncStatusDatabase =
        syncStatusDatabaseRule.resolveDependency { super.provideSyncStatusDatabase() }

    override fun provideSyncScopesBuilder(loginInfoManager: LoginInfoManager, preferencesManager: PreferencesManager): SyncScopesBuilder =
        syncScopesBuilderRule.resolveDependency { super.provideSyncScopesBuilder(loginInfoManager, preferencesManager) }

    override fun provideCountTask(dbManager: DbManager, syncStatusDatabase: SyncStatusDatabase): CountTask =
        countTaskRule.resolveDependency { super.provideCountTask(dbManager, syncStatusDatabase) }

    override fun provideDownSyncTask(localDbManager: LocalDbManager, remotePeopleManager: RemotePeopleManager, timeHelper: TimeHelper, syncStatusDatabase: SyncStatusDatabase): DownSyncTask =
        downSyncTaskRule.resolveDependency { super.provideDownSyncTask(localDbManager, remotePeopleManager, timeHelper, syncStatusDatabase) }

    override fun provideSyncSchedulerHelper(preferencesManager: PreferencesManager, loginInfoManager: LoginInfoManager, sessionEventsSyncManager: SessionEventsSyncManager, downSyncManager: DownSyncManager): SyncSchedulerHelper =
        syncSchedulerHelperRule.resolveDependency { super.provideSyncSchedulerHelper(preferencesManager, loginInfoManager, sessionEventsSyncManager, downSyncManager) }

    override fun provideDownSyncManager(syncScopesBuilder: SyncScopesBuilder): DownSyncManager =
        downSyncManagerRule.resolveDependency { super.provideDownSyncManager(syncScopesBuilder) }

    override fun provideLocationProvider(ctx: Context): LocationProvider =
        locationProverRule.resolveDependency { super.provideLocationProvider(ctx) }
}
