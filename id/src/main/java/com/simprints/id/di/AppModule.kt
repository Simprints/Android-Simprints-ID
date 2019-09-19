package com.simprints.id.di

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.simprints.core.network.SimApiClient
import com.simprints.core.tools.AndroidResourcesHelper
import com.simprints.core.tools.AndroidResourcesHelperImpl
import com.simprints.id.Application
import com.simprints.id.activities.consent.ConsentViewModelFactory
import com.simprints.id.activities.exitform.CoreExitFormViewModelFactory
import com.simprints.id.activities.fingerprintexitform.FingerprintExitFormViewModelFactory
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.AnalyticsManagerImpl
import com.simprints.id.data.analytics.crashreport.CoreCrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportManagerImpl
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManagerImpl
import com.simprints.id.data.analytics.eventdata.controllers.local.RealmSessionEventsDbManagerImpl
import com.simprints.id.data.analytics.eventdata.controllers.local.SessionEventsLocalDbManager
import com.simprints.id.data.analytics.eventdata.controllers.remote.RemoteSessionsManager
import com.simprints.id.data.analytics.eventdata.controllers.remote.RemoteSessionsManagerImpl
import com.simprints.id.data.consent.LongConsentManager
import com.simprints.id.data.consent.LongConsentManagerImpl
import com.simprints.id.data.consent.shortconsent.ConsentLocalDataSource
import com.simprints.id.data.consent.shortconsent.ConsentLocalDataSourceImpl
import com.simprints.id.data.consent.shortconsent.ConsentRepository
import com.simprints.id.data.consent.shortconsent.ConsentRepositoryImpl
import com.simprints.id.data.db.common.FirebaseManagerImpl
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.db.syncinfo.local.SyncInfoLocalDataSource
import com.simprints.id.data.db.syncstatus.SyncStatusDatabase
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.loginInfo.LoginInfoManagerImpl
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.data.prefs.events.RecentEventsPreferencesManager
import com.simprints.id.data.prefs.events.RecentEventsPreferencesManagerImpl
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.data.secure.SecureDataManagerImpl
import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.data.secure.keystore.KeystoreManagerImpl
import com.simprints.id.orchestrator.responsebuilders.AppResponseFactory
import com.simprints.id.orchestrator.responsebuilders.AppResponseFactoryImpl
import com.simprints.id.secure.SecureApiInterface
import com.simprints.id.secure.SignerManager
import com.simprints.id.secure.SignerManagerImpl
import com.simprints.id.services.GuidSelectionManager
import com.simprints.id.services.GuidSelectionManagerImpl
import com.simprints.id.services.scheduledSync.SyncSchedulerHelper
import com.simprints.id.services.scheduledSync.SyncSchedulerHelperImpl
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.DownSyncManager
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.DownSyncManagerImpl
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilderImpl
import com.simprints.id.services.scheduledSync.peopleDownSync.tasks.*
import com.simprints.id.services.scheduledSync.peopleUpsync.PeopleUpSyncMaster
import com.simprints.id.services.scheduledSync.peopleUpsync.PeopleUpSyncMasterImpl
import com.simprints.id.services.scheduledSync.peopleUpsync.periodicFlusher.PeopleUpSyncPeriodicFlusherMaster
import com.simprints.id.services.scheduledSync.peopleUpsync.uploader.PeopleUpSyncUploaderMaster
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManager
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManagerImpl
import com.simprints.id.tools.RandomGenerator
import com.simprints.id.tools.RandomGeneratorImpl
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.TimeHelperImpl
import com.simprints.id.tools.extensions.deviceId
import com.simprints.id.tools.extensions.packageVersionName
import com.simprints.id.tools.utils.SimNetworkUtils
import com.simprints.id.tools.utils.SimNetworkUtilsImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
open class AppModule {

    @Provides
    @Singleton
    fun provideContext(app: Application): Context = app

    @Provides
    @Singleton
    open fun provideRemoteDbManager(loginInfoManager: LoginInfoManager): RemoteDbManager = FirebaseManagerImpl(loginInfoManager)

    @Provides
    @Singleton
    open fun provideLoginInfoManager(improvedSharedPreferences: ImprovedSharedPreferences): LoginInfoManager = LoginInfoManagerImpl(improvedSharedPreferences)

    @Provides
    @Singleton
    open fun providePeopleUpSyncMaster(): PeopleUpSyncMaster =
        PeopleUpSyncMasterImpl(
            PeopleUpSyncUploaderMaster(),
            PeopleUpSyncPeriodicFlusherMaster()
        )

    @Provides
    @Singleton
    open fun provideDbManager(projectRepository: ProjectRepository,
                              remoteDbManager: RemoteDbManager,
                              loginInfoManager: LoginInfoManager,
                              preferencesManager: PreferencesManager,
                              peopleUpSyncMaster: PeopleUpSyncMaster,
                              database: SyncStatusDatabase): SignerManager =
        SignerManagerImpl(projectRepository, remoteDbManager, loginInfoManager, preferencesManager, peopleUpSyncMaster, database.downSyncDao, database.upSyncDao)

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
                                     firebaseAnalytics: FirebaseAnalytics): AnalyticsManager = AnalyticsManagerImpl(loginInfoManager, firebaseAnalytics)

    @Provides
    @Singleton
    open fun provideCrashManager(): CrashReportManager = CrashReportManagerImpl()

    @Provides
    @Singleton
    open fun provideCoreCrashReportManager(crashReportManager: CrashReportManager): CoreCrashReportManager = crashReportManager

    @Provides
    @Singleton
    open fun provideKeystoreManager(ctx: Context): KeystoreManager = KeystoreManagerImpl(ctx)

    @Provides
    @Singleton
    open fun provideRandomGenerator(): RandomGenerator = RandomGeneratorImpl()

    @Provides
    @Singleton
    open fun provideSecureDataManager(preferencesManager: PreferencesManager, keystoreManager: KeystoreManager, randomGenerator: RandomGenerator): SecureDataManager =
        SecureDataManagerImpl(keystoreManager, preferencesManager, randomGenerator)

    @Provides
    open fun provideLongConsentManager(ctx: Context, loginInfoManager: LoginInfoManager, crashReportManager: CrashReportManager):
        LongConsentManager = LongConsentManagerImpl(ctx.filesDir.absolutePath, loginInfoManager, crashReportManager)

    @Provides
    @Singleton
    open fun provideSimNetworkUtils(ctx: Context): SimNetworkUtils = SimNetworkUtilsImpl(ctx)

    @Provides
    open fun provideSecureApiInterface(): SecureApiInterface = SimApiClient(SecureApiInterface::class.java, SecureApiInterface.baseUrl).api

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
                                         crashReportManager: CrashReportManager): SessionEventsManager =
        SessionEventsManagerImpl(ctx.deviceId, ctx.packageVersionName, sessionEventsSyncManager, sessionEventsLocalDbManager, preferencesManager, timeHelper, crashReportManager)


    @Provides
    @Singleton
    open fun provideScheduledSessionsSyncManager(): SessionEventsSyncManager =
        SessionEventsSyncManagerImpl()

    @Provides
    @Singleton
    open fun provideSyncStatusDatabase(ctx: Context): SyncStatusDatabase =
        SyncStatusDatabase.getDatabase(ctx)

    @Provides
    @Singleton
    open fun provideSyncScopesBuilder(loginInfoManager: LoginInfoManager, preferencesManager: PreferencesManager): SyncScopesBuilder =
        SyncScopesBuilderImpl(loginInfoManager, preferencesManager)

    @Provides
    @Singleton
    open fun provideDownSyncManager(syncScopesBuilder: SyncScopesBuilder): DownSyncManager =
        DownSyncManagerImpl(syncScopesBuilder)

    @Provides
    @Singleton
    open fun provideSyncSchedulerHelper(preferencesManager: PreferencesManager,
                                        loginInfoManager: LoginInfoManager,
                                        sessionEventsSyncManager: SessionEventsSyncManager,
                                        downSyncManager: DownSyncManager): SyncSchedulerHelper =
        SyncSchedulerHelperImpl(preferencesManager, loginInfoManager, sessionEventsSyncManager, downSyncManager)

    @Provides
    open fun provideCountTask(personRepository: PersonRepository): CountTask = CountTaskImpl(personRepository)

    @Provides
    fun provideSaveCountsTask(syncStatusDatabase: SyncStatusDatabase): SaveCountsTask = SaveCountsTaskImpl(syncStatusDatabase)


    @Provides
    open fun provideDownSyncTask(personLocalDataSource: PersonLocalDataSource,
                                 syncInfoLocalDataSource: SyncInfoLocalDataSource,
                                 personRemoteDataSource: PersonRemoteDataSource,
                                 timeHelper: TimeHelper,
                                 syncStatusDatabase: SyncStatusDatabase): DownSyncTask = DownSyncTaskImpl(personLocalDataSource, syncInfoLocalDataSource, personRemoteDataSource, timeHelper, syncStatusDatabase.downSyncDao)

    @Provides
    @Singleton
    open fun provideRemoteSessionsManager(remoteDbManager: RemoteDbManager): RemoteSessionsManager = RemoteSessionsManagerImpl(remoteDbManager)

    @Provides
    open fun provideAppResponseBuilderFactory(): AppResponseFactory = AppResponseFactoryImpl()


    @Provides
    open fun provideGuidSelectionManager(context: Context,
                                         loginInfoManager: LoginInfoManager,
                                         analyticsManager: AnalyticsManager,
                                         crashReportManager: CrashReportManager,
                                         timeHelper: TimeHelper,
                                         sessionEventsManager: SessionEventsManager): GuidSelectionManager =
        GuidSelectionManagerImpl(
            context.deviceId, loginInfoManager, analyticsManager, crashReportManager, timeHelper, sessionEventsManager)

    @Provides
    open fun getConsentDataManager(prefs: ImprovedSharedPreferences, remoteConfigWrapper: RemoteConfigWrapper): ConsentLocalDataSource =
        ConsentLocalDataSourceImpl(prefs, remoteConfigWrapper)

    @Provides
    open fun provideConsentTextManager(context: Context,
                                       consentLocalDataSource: ConsentLocalDataSource,
                                       crashReportManager: CrashReportManager,
                                       preferencesManager: PreferencesManager) : ConsentRepository =
        ConsentRepositoryImpl(context, consentLocalDataSource, crashReportManager,
            preferencesManager.programName, preferencesManager.organizationName, preferencesManager.language)

    @Provides
    open fun provideConsentViewModelFactory(consentTextManager: ConsentRepository,
                                            sessionEventsManager: SessionEventsManager,
                                            timeHelper: TimeHelper) =
        ConsentViewModelFactory(consentTextManager, sessionEventsManager)

    @Provides
    open fun provideCoreExitFormViewModelFactory(sessionEventsManager: SessionEventsManager) =
        CoreExitFormViewModelFactory(sessionEventsManager)

    @Provides
    open fun provideFingerprintExitFormViewModelFactory(sessionEventsManager: SessionEventsManager) =
        FingerprintExitFormViewModelFactory(sessionEventsManager)
}

