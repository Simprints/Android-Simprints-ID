package com.simprints.id.commontesttools.di

import android.content.Context
import com.simprints.id.Application
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.controllers.local.SessionEventsLocalDbManager
import com.simprints.id.data.analytics.eventdata.controllers.remote.RemoteSessionsManager
import com.simprints.id.data.consent.LongConsentManager
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.people_sync.PeopleSyncStatusDatabase
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.di.AppModule
import com.simprints.id.secure.SecureApiInterface
import com.simprints.id.secure.SignerManager
import com.simprints.id.services.scheduledSync.sync.peopleUpsync.PeopleUpSyncMaster
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
                    var simNetworkUtilsRule: DependencyRule = RealRule,
                    var secureApiInterfaceRule: DependencyRule = RealRule,
                    var longConsentManagerRule: DependencyRule = RealRule,
                    var syncStatusDatabaseRule: DependencyRule = RealRule) : AppModule() {

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
                                  downSyncScopeRepository: PeopleDownSyncScopeRepository,
                                  database: PeopleSyncStatusDatabase): SignerManager =
        dbManagerRule.resolveDependency { super.provideDbManager(projectRepository, remoteDbManager, loginInfoManager, preferencesManager, peopleUpSyncMaster, downSyncScopeRepository, database) }

    override fun provideSecureDataManager(preferencesManager: PreferencesManager,
                                          keystoreManager: KeystoreManager,
                                          randomGenerator: RandomGenerator): SecureDataManager =
        secureDataManagerRule.resolveDependency { super.provideSecureDataManager(preferencesManager, keystoreManager, randomGenerator) }

    override fun provideKeystoreManager(ctx: Context): KeystoreManager =
        keystoreManagerRule.resolveDependency { super.provideKeystoreManager(ctx) }

    override fun provideSecureApiInterface(): SecureApiInterface =
        secureApiInterfaceRule.resolveDependency { super.provideSecureApiInterface() }

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

    override fun provideRemoteSessionsManager(remoteDbManager: RemoteDbManager): RemoteSessionsManager =
        remoteSessionsManagerRule.resolveDependency { super.provideRemoteSessionsManager(remoteDbManager) }

    override fun provideSyncStatusDatabase(ctx: Context): PeopleSyncStatusDatabase =
        syncStatusDatabaseRule.resolveDependency { super.provideSyncStatusDatabase(ctx) }

}
