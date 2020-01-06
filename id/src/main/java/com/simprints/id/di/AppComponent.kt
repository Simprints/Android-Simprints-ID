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
import com.simprints.id.activities.debug.DebugActivity
import com.simprints.id.activities.faceexitform.FaceExitFormActivity
import com.simprints.id.activities.fetchguid.FetchGuidActivity
import com.simprints.id.activities.fingerprintexitform.FingerprintExitFormActivity
import com.simprints.id.activities.guidselection.GuidSelectionActivity
import com.simprints.id.activities.login.LoginActivity
import com.simprints.id.activities.login.LoginPresenter
import com.simprints.id.activities.longConsent.PrivacyNoticeActivity
import com.simprints.id.activities.longConsent.PrivacyNoticePresenter
import com.simprints.id.activities.requestLogin.RequestLoginActivity
import com.simprints.id.activities.settings.ModuleSelectionActivity
import com.simprints.id.activities.settings.SettingsAboutActivity
import com.simprints.id.activities.settings.SettingsActivity
import com.simprints.id.activities.settings.fragments.moduleselection.ModuleSelectionFragment
import com.simprints.id.activities.settings.fragments.settingsAbout.SettingsAboutFragment
import com.simprints.id.activities.settings.fragments.settingsAbout.SettingsAboutPresenter
import com.simprints.id.activities.settings.fragments.settingsPreference.SettingsPreferenceFragment
import com.simprints.id.activities.settings.fragments.settingsPreference.SettingsPreferencePresenter
import com.simprints.id.activities.settings.syncinformation.SyncInformationActivity
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.crashreport.CoreCrashReportManager
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.person.local.FingerprintIdentityLocalDataSource
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.guidselection.GuidSelectionWorker
import com.simprints.id.secure.ProjectAuthenticator
import com.simprints.id.services.scheduledSync.SyncSchedulerImpl
import com.simprints.id.services.scheduledSync.imageUpSync.ImageUpSyncWorker
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncCountWorker
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderWorker
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker
import com.simprints.id.services.scheduledSync.people.up.workers.PeopleUpSyncCountWorker
import com.simprints.id.services.scheduledSync.people.up.workers.PeopleUpSyncUploaderWorker
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsMasterWorker
import com.simprints.id.tools.AndroidResourcesHelper
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.utils.SimNetworkUtils
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Component(modules = [AppModule::class, DataModule::class, PreferencesModule::class, SerializerModule::class, SyncModule::class])
@Singleton
interface AppComponent {

    @Component.Builder interface Builder {

        @BindsInstance
        fun application(app: Application): Builder

        fun appModule(appModule: AppModule): Builder
        fun dataModule(dataModule: DataModule): Builder
        fun preferencesModule(preferencesModule: PreferencesModule): Builder
        fun serializerModule(serializerModule: SerializerModule): Builder
        fun syncModule(syncModule: SyncModule): Builder

        fun build(): AppComponent
    }

    fun getOrchestratorComponent(): OrchestratorComponent.Builder

    fun inject(app: Application)
    fun inject(guidSelectionWorker: GuidSelectionWorker)
    fun inject(alertActivity: AlertActivity)
    fun inject(privacyNoticeActivity: PrivacyNoticeActivity)
    fun inject(loginActivity: LoginActivity)
    fun inject(checkLoginActivity: CheckLoginFromIntentActivity)
    fun inject(checkLoginActivity: CheckLoginFromMainLauncherActivity)
    fun inject(dashboardActivity: DashboardActivity)
    fun inject(checkLoginPresenter: CheckLoginPresenter)
    fun inject(checkLoginFromIntentPresenter: CheckLoginFromIntentPresenter)
    fun inject(checkLoginFromMainLauncherPresenter: CheckLoginFromMainLauncherPresenter)
    fun inject(dashboardCardsFactory: DashboardCardsFactory)
    fun inject(dashboardSyncCardViewModel: DashboardSyncCardViewModel)
    fun inject(loginPresenter: LoginPresenter)
    fun inject(requestLoginActivity: RequestLoginActivity)
    fun inject(projectAuthenticator: ProjectAuthenticator)
    fun inject(dashboardPresenter: DashboardPresenter)
    fun inject(alertPresenter: AlertPresenter)
    fun inject(settingsPreferencePresenter: SettingsPreferencePresenter)
    fun inject(privacyNoticePresenter: PrivacyNoticePresenter)
    fun inject(syncSchedulerHelper: SyncSchedulerImpl)
    fun inject(sessionsSyncMasterWorker: SessionEventsMasterWorker)
    fun inject(settingsAboutPresenter: SettingsAboutPresenter)
    fun inject(moduleSelectionActivity: ModuleSelectionActivity)
    fun inject(moduleSelectionActivity: ModuleSelectionFragment)
    fun inject(settingsActivity: SettingsActivity)
    fun inject(settingsPreferenceFragment: SettingsPreferenceFragment)
    fun inject(settingsAboutFragment: SettingsAboutFragment)
    fun inject(settingsAboutActivity: SettingsAboutActivity)
    fun inject(consentActivity: ConsentActivity)
    fun inject(coreExitFormActivity: CoreExitFormActivity)
    fun inject(fingerprintExitFormActivity: FingerprintExitFormActivity)
    fun inject(faceExitFormActivity: FaceExitFormActivity)
    fun inject(fetchGuidActivity: FetchGuidActivity)
    fun inject(guidSelectionActivity: GuidSelectionActivity)
    fun inject(debugActivity: DebugActivity)
    fun inject(peopleDownSyncCountWorker: PeopleDownSyncCountWorker)
    fun inject(peopleDownSyncDownloaderWorker: PeopleDownSyncDownloaderWorker)
    fun inject(peopleSyncMasterWorker: PeopleSyncMasterWorker)
    fun inject(peopleUpSyncUploaderWorker: PeopleUpSyncUploaderWorker)
    fun inject(peopleUpSyncCountWorker: PeopleUpSyncCountWorker)
    fun inject(syncInformationActivity: SyncInformationActivity)
    fun inject(imageUpSyncWorker: ImageUpSyncWorker)

    fun getSessionEventsManager(): SessionEventsManager
    fun getCrashReportManager(): CoreCrashReportManager
    fun getTimeHelper(): TimeHelper
    fun getPersonRepository(): PersonRepository
    fun getFingerprintRecordLocalDataSource(): FingerprintIdentityLocalDataSource
    fun getPreferencesManager(): PreferencesManager
    fun getAnalyticsManager(): AnalyticsManager
    fun getSimNetworkUtils(): SimNetworkUtils
    fun getImprovedSharedPreferences(): ImprovedSharedPreferences
    fun getRemoteConfigWrapper(): RemoteConfigWrapper
    fun getContext(): Context
    fun getAndroidResourcesHelper(): AndroidResourcesHelper
}
