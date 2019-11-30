package com.simprints.id.commontesttools.di

import android.content.Context
import android.content.SharedPreferences
import com.simprints.id.Application
import com.simprints.id.commontesttools.state.setupFakeEncryptedSharedPreferences
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.controllers.local.SessionEventsLocalDbManager
import com.simprints.id.data.analytics.eventdata.controllers.remote.RemoteSessionsManager
import com.simprints.id.data.consent.LongConsentManager
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.db.syncinfo.local.SyncInfoLocalDataSource
import com.simprints.id.data.db.syncstatus.SyncStatusDatabase
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.secure.LegacyLocalDbKeyProvider
import com.simprints.id.data.secure.SecureLocalDbKeyProvider
import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.di.AppModule
import com.simprints.id.secure.SecureApiInterface
import com.simprints.id.secure.SignerManager
import com.simprints.id.services.scheduledSync.SyncSchedulerHelper
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.DownSyncManager
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.tasks.CountTask
import com.simprints.id.services.scheduledSync.peopleDownSync.tasks.DownSyncTask
import com.simprints.id.services.scheduledSync.peopleUpsync.PeopleUpSyncMaster
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManager
import com.simprints.id.tools.RandomGenerator
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.utils.SimNetworkUtils
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.di.DependencyRule.RealRule

class TestAppModule(app: Application,
                    var remoteDbManagerRule: DependencyRule = RealRule,
                    var remoteSessionsManagerRule: DependencyRule = RealRule,
                    var dbManagerRule: DependencyRule = RealRule,
                    var secureDataManagerRule: DependencyRule = RealRule,
                    var loginInfoManagerRule: DependencyRule = RealRule,
                    var randomGeneratorRule: DependencyRule = RealRule,
                    var keystoreManagerRule: DependencyRule = RealRule,
                    var crashReportManagerRule: DependencyRule = RealRule,
                    var sessionEventsManagerRule: DependencyRule = RealRule,
                    var sessionEventsLocalDbManagerRule: DependencyRule = RealRule,
                    var scheduledSessionsSyncManagerRule: DependencyRule = RealRule,
                    var simNetworkUtilsRule: DependencyRule = RealRule,
                    var secureApiInterfaceRule: DependencyRule = RealRule,
                    var longConsentManagerRule: DependencyRule = RealRule,
                    var peopleUpSyncMasterRule: DependencyRule = RealRule,
                    var syncStatusDatabaseRule: DependencyRule = RealRule,
                    var syncScopesBuilderRule: DependencyRule = RealRule,
                    var countTaskRule: DependencyRule = RealRule,
                    var downSyncTaskRule: DependencyRule = RealRule,
                    var syncSchedulerHelperRule: DependencyRule = RealRule,
                    var downSyncManagerRule: DependencyRule = RealRule,
                    var encryptedSharedPreferencesRule: DependencyRule = DependencyRule.ReplaceRule { setupFakeEncryptedSharedPreferences(app) }) : AppModule() {

    override fun provideCrashManager(): CrashReportManager =
        crashReportManagerRule.resolveDependency { super.provideCrashManager() }

    override fun provideLoginInfoManager(improvedSharedPreferences: ImprovedSharedPreferences): LoginInfoManager =
        loginInfoManagerRule.resolveDependency { super.provideLoginInfoManager(improvedSharedPreferences) }

    override fun provideRandomGenerator(): RandomGenerator =
        randomGeneratorRule.resolveDependency { super.provideRandomGenerator() }

    override fun provideRemoteDbManager(loginInfoManager: LoginInfoManager): RemoteDbManager =
        remoteDbManagerRule.resolveDependency { super.provideRemoteDbManager(loginInfoManager) }

    override fun provideDbManager(projectRepository: ProjectRepository,
                                  remoteDbManager: RemoteDbManager,
                                  loginInfoManager: LoginInfoManager,
                                  preferencesManager: PreferencesManager,
                                  peopleUpSyncMaster: PeopleUpSyncMaster,
                                  database: SyncStatusDatabase): SignerManager =
        dbManagerRule.resolveDependency { super.provideDbManager(projectRepository, remoteDbManager, loginInfoManager, preferencesManager, peopleUpSyncMaster, database) }

    override fun provideSecureLocalDbKeyProvider(encryptedSharedPrefs: SharedPreferences,
                                                 randomGenerator: RandomGenerator,
                                                 unsecuredLocalDbKeyProvider: LegacyLocalDbKeyProvider): SecureLocalDbKeyProvider =
        secureDataManagerRule.resolveDependency { super.provideSecureLocalDbKeyProvider(encryptedSharedPrefs, randomGenerator, unsecuredLocalDbKeyProvider) }

    override fun provideUnsecureLocalDbKeyProvider(preferencesManager: PreferencesManager,
                                                   keystoreManager: KeystoreManager,
                                                   randomGenerator: RandomGenerator): LegacyLocalDbKeyProvider =
        secureDataManagerRule.resolveDependency { super.provideUnsecureLocalDbKeyProvider(preferencesManager, keystoreManager, randomGenerator) }

    override fun provideKeystoreManager(ctx: Context): KeystoreManager =
        keystoreManagerRule.resolveDependency { super.provideKeystoreManager(ctx) }

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
                                                    secureDataManager: SecureLocalDbKeyProvider): SessionEventsLocalDbManager =
        sessionEventsLocalDbManagerRule.resolveDependency { super.provideSessionEventsLocalDbManager(ctx, secureDataManager) }

    override fun provideSimNetworkUtils(ctx: Context): SimNetworkUtils =
        simNetworkUtilsRule.resolveDependency { super.provideSimNetworkUtils(ctx) }

    override fun provideLongConsentManager(ctx: Context, loginInfoManager: LoginInfoManager, crashReportManager: CrashReportManager): LongConsentManager =
        longConsentManagerRule.resolveDependency { super.provideLongConsentManager(ctx, loginInfoManager, crashReportManager) }

    override fun provideRemoteSessionsManager(remoteDbManager: RemoteDbManager): RemoteSessionsManager =
        remoteSessionsManagerRule.resolveDependency { super.provideRemoteSessionsManager(remoteDbManager) }

    override fun providePeopleUpSyncMaster(): PeopleUpSyncMaster =
        peopleUpSyncMasterRule.resolveDependency { super.providePeopleUpSyncMaster() }

    override fun provideSyncStatusDatabase(ctx: Context): SyncStatusDatabase =
        syncStatusDatabaseRule.resolveDependency { super.provideSyncStatusDatabase(ctx) }

    override fun provideSyncScopesBuilder(loginInfoManager: LoginInfoManager, preferencesManager: PreferencesManager): SyncScopesBuilder =
        syncScopesBuilderRule.resolveDependency { super.provideSyncScopesBuilder(loginInfoManager, preferencesManager) }

    override fun provideCountTask(personRepository: PersonRepository): CountTask =
        countTaskRule.resolveDependency { super.provideCountTask(personRepository) }

    override fun provideDownSyncTask(personLocalDataSource: PersonLocalDataSource,
                                     syncInfoLocalDataSource: SyncInfoLocalDataSource,
                                     personRemoteDataSource: PersonRemoteDataSource,
                                     timeHelper: TimeHelper,
                                     syncStatusDatabase: SyncStatusDatabase): DownSyncTask =
        downSyncTaskRule.resolveDependency { super.provideDownSyncTask(personLocalDataSource, syncInfoLocalDataSource, personRemoteDataSource, timeHelper, syncStatusDatabase) }

    override fun provideSyncSchedulerHelper(preferencesManager: PreferencesManager, loginInfoManager: LoginInfoManager, sessionEventsSyncManager: SessionEventsSyncManager, downSyncManager: DownSyncManager): SyncSchedulerHelper =
        syncSchedulerHelperRule.resolveDependency { super.provideSyncSchedulerHelper(preferencesManager, loginInfoManager, sessionEventsSyncManager, downSyncManager) }

    override fun provideDownSyncManager(syncScopesBuilder: SyncScopesBuilder): DownSyncManager =
        downSyncManagerRule.resolveDependency { super.provideDownSyncManager(syncScopesBuilder) }

    override fun provideEncryptedSharedPreferences(app: Application): SharedPreferences =
        encryptedSharedPreferencesRule.resolveDependency { super.provideEncryptedSharedPreferences(app) }

}
