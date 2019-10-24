package com.simprints.id.di

import android.content.Context
import com.simprints.id.Application
import com.simprints.id.activities.alert.AlertActivity
import com.simprints.id.activities.alert.AlertPresenter
import com.simprints.id.activities.checkLogin.CheckLoginPresenter
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentPresenter
import com.simprints.id.activities.checkLogin.openedByMainLauncher.CheckLoginFromMainLauncherActivity
import com.simprints.id.activities.checkLogin.openedByMainLauncher.CheckLoginFromMainLauncherPresenter
import com.simprints.id.activities.consent.ConsentActivity
import com.simprints.id.activities.coreexitform.CoreExitFormActivity
import com.simprints.id.activities.dashboard.DashboardActivity
import com.simprints.id.activities.dashboard.DashboardCardsFactory
import com.simprints.id.activities.dashboard.DashboardPresenter
import com.simprints.id.activities.dashboard.viewModels.syncCard.DashboardSyncCardViewModel
import com.simprints.id.activities.dashboard.viewModels.syncCard.DashboardSyncCardViewModelHelper
import com.simprints.id.activities.dashboard.views.DashboardSyncCardView
import com.simprints.id.activities.debug.DebugActivity
import com.simprints.id.activities.debug.DebugViewModel
import com.simprints.id.activities.faceexitform.FaceExitFormActivity
import com.simprints.id.activities.fetchguid.FetchGuidActivity
import com.simprints.id.activities.fingerprintexitform.FingerprintExitFormActivity
import com.simprints.id.activities.login.LoginActivity
import com.simprints.id.activities.login.LoginPresenter
import com.simprints.id.activities.longConsent.LongConsentPresenter
import com.simprints.id.activities.longConsent.PrivacyNoticeActivity
import com.simprints.id.activities.orchestrator.OrchestratorActivity
import com.simprints.id.activities.requestLogin.RequestLoginActivity
import com.simprints.id.activities.settings.SettingsAboutActivity
import com.simprints.id.activities.settings.SettingsActivity
import com.simprints.id.activities.settings.fragments.settingsAbout.SettingsAboutFragment
import com.simprints.id.activities.settings.fragments.settingsAbout.SettingsAboutPresenter
import com.simprints.id.activities.settings.fragments.settingsPreference.SettingsPreferenceFragment
import com.simprints.id.activities.settings.fragments.settingsPreference.SettingsPreferencePresenter
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.crashreport.CoreCrashReportManager
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.guidselection.GuidSelectionWorker
import com.simprints.id.orchestrator.FlowManager
import com.simprints.id.secure.ProjectAuthenticator
import com.simprints.id.services.scheduledSync.SyncSchedulerHelperImpl
import com.simprints.id.services.scheduledSync.peopleDownSync.tasks.CountTaskImpl
import com.simprints.id.services.scheduledSync.peopleDownSync.tasks.DownSyncTaskImpl
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.CountWorker
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.DownSyncMasterWorker
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.SubDownSyncWorker
import com.simprints.id.services.scheduledSync.peopleUpsync.periodicFlusher.PeopleUpSyncPeriodicFlusherWorker
import com.simprints.id.services.scheduledSync.peopleUpsync.uploader.PeopleUpSyncUploaderWorker
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsMasterWorker
import com.simprints.id.tools.AndroidResourcesHelper
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.utils.SimNetworkUtils
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Component(modules = [AppModule::class, DataModule::class, PreferencesModule::class, SerializerModule::class, OrchestratorModule::class])
@Singleton
interface AppComponent {

    @Component.Builder interface Builder {

        @BindsInstance
        fun application(app: Application): Builder

        fun appModule(appModule: AppModule): Builder
        fun dataModule(dataModule: DataModule): Builder
        fun preferencesModule(preferencesModule: PreferencesModule): Builder
        fun serializerModule(serializerModule: SerializerModule): Builder
        fun orchestratorModule(orchestratorModule: OrchestratorModule): Builder

        fun build(): AppComponent
    }

    fun inject(app: Application)
    fun inject(guidSelectionWorker: GuidSelectionWorker)
    fun inject(alertActivity: AlertActivity)
    fun inject(aboutActivity: DebugActivity)
    fun inject(privacyNoticeActivity: PrivacyNoticeActivity)
    fun inject(loginActivity: LoginActivity)
    fun inject(checkLoginActivity: CheckLoginFromIntentActivity)
    fun inject(checkLoginActivity: CheckLoginFromMainLauncherActivity)
    fun inject(dashboardActivity: DashboardActivity)
    fun inject(checkLoginPresenter: CheckLoginPresenter)
    fun inject(checkLoginFromIntentPresenter: CheckLoginFromIntentPresenter)
    fun inject(checkLoginFromMainLauncherPresenter: CheckLoginFromMainLauncherPresenter)
    fun inject(aboutPresenter: DebugViewModel)
    fun inject(dashboardCardsFactory: DashboardCardsFactory)
    fun inject(dashboardSyncCardViewModel: DashboardSyncCardViewModel)
    fun inject(loginPresenter: LoginPresenter)
    fun inject(requestLoginActivity: RequestLoginActivity)
    fun inject(projectAuthenticator: ProjectAuthenticator)
    fun inject(dashboardPresenter: DashboardPresenter)
    fun inject(alertPresenter: AlertPresenter)
    fun inject(peopleUpSyncUploaderWorker: PeopleUpSyncUploaderWorker)
    fun inject(peopleUpSyncPeriodicFlusherWorker: PeopleUpSyncPeriodicFlusherWorker)
    fun inject(settingsPreferencePresenter: SettingsPreferencePresenter)
    fun inject(longConsentPresenter: LongConsentPresenter)
    fun inject(syncSchedulerHelper: SyncSchedulerHelperImpl)
    fun inject(dashboardSyncCardView: DashboardSyncCardView)
    fun inject(sessionsSyncMasterWorker: SessionEventsMasterWorker)
    fun inject(countTask: CountTaskImpl)
    fun inject(downSyncTask: DownSyncTaskImpl)
    fun inject(countWorker: CountWorker)
    fun inject(subDownSyncWorker: SubDownSyncWorker)
    fun inject(syncWorker: DownSyncMasterWorker)
    fun inject(dashboardSyncCardViewModelManager: DashboardSyncCardViewModelHelper)
    fun inject(settingsAboutPresenter: SettingsAboutPresenter)
    fun inject(settingsActivity: SettingsActivity)
    fun inject(settingsPreferenceFragment: SettingsPreferenceFragment)
    fun inject(settingsAboutFragment: SettingsAboutFragment)
    fun inject(settingsAboutActivity: SettingsAboutActivity)
    fun inject(consentActivity: ConsentActivity)
    fun inject(coreExitFormActivity: CoreExitFormActivity)
    fun inject(fingerprintExitFormActivity: FingerprintExitFormActivity)
    fun inject(faceExitFormActivity: FaceExitFormActivity)
    fun inject(fetchGuidActivity: FetchGuidActivity)
    fun inject(orchestratorActivity: OrchestratorActivity)
    fun inject(flowManager: FlowManager)

    fun getSessionEventsManager(): SessionEventsManager
    fun getCrashReportManager(): CoreCrashReportManager
    fun getTimeHelper(): TimeHelper
    fun getPersonRepository(): PersonRepository
    fun getPreferencesManager(): PreferencesManager
    fun getAnalyticsManager(): AnalyticsManager
    fun getSimNetworkUtils(): SimNetworkUtils
    fun getImprovedSharedPreferences(): ImprovedSharedPreferences
    fun getRemoteConfigWrapper(): RemoteConfigWrapper
    fun getContext(): Context
    fun getFlowManager(): FlowManager
    fun getAndroidResourcesHelper(): AndroidResourcesHelper
}
