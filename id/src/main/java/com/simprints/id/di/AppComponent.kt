package com.simprints.id.di

import com.simprints.core.sharedpreferences.ImprovedSharedPreferences
import com.simprints.core.sharedpreferences.PreferencesManager
import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.EventSystemModule
import com.simprints.eventsystem.event.EventRepository
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
import com.simprints.id.activities.debug.DebugActivity
import com.simprints.id.activities.enrollast.EnrolLastBiometricsActivity
import com.simprints.id.activities.faceexitform.FaceExitFormActivity
import com.simprints.id.activities.fetchguid.FetchGuidActivity
import com.simprints.id.activities.fingerprintexitform.FingerprintExitFormActivity
import com.simprints.id.activities.guidselection.GuidSelectionActivity
import com.simprints.id.activities.login.LoginActivity
import com.simprints.id.activities.longConsent.PrivacyNoticeActivity
import com.simprints.id.activities.qrcapture.QrCaptureActivity
import com.simprints.id.activities.requestLogin.RequestLoginActivity
import com.simprints.id.activities.settings.ModuleSelectionActivity
import com.simprints.id.activities.settings.SettingsAboutActivity
import com.simprints.id.activities.settings.SettingsActivity
import com.simprints.id.activities.settings.fingerselection.FingerSelectionActivity
import com.simprints.id.activities.settings.fragments.moduleselection.ModuleSelectionFragment
import com.simprints.id.activities.settings.fragments.settingsAbout.SettingsAboutFragment
import com.simprints.id.activities.settings.fragments.settingsPreference.SettingsPreferenceFragment
import com.simprints.id.activities.settings.syncinformation.SyncInformationActivity
import com.simprints.id.activities.setup.SetupActivity
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.secure.ProjectAuthenticatorImpl
import com.simprints.id.services.config.RemoteConfigWorker
import com.simprints.id.services.location.StoreUserLocationIntoCurrentSessionWorker
import com.simprints.id.services.securitystate.SecurityStateWorker
import com.simprints.id.services.sync.SyncSchedulerImpl
import com.simprints.id.services.sync.events.down.workers.EventDownSyncCountWorker
import com.simprints.id.services.sync.events.down.workers.EventDownSyncDownloaderWorker
import com.simprints.id.services.sync.events.master.workers.EventEndSyncReporterWorker
import com.simprints.id.services.sync.events.master.workers.EventStartSyncReporterWorker
import com.simprints.id.services.sync.events.master.workers.EventSyncMasterWorker
import com.simprints.id.services.sync.events.up.workers.EventUpSyncCountWorker
import com.simprints.id.services.sync.events.up.workers.EventUpSyncUploaderWorker
import com.simprints.id.services.sync.images.up.ImageUpSyncWorker
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.ConfigManagerModule
import com.simprints.infra.config.DataStoreModule
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import com.simprints.infra.enrolment.records.EnrolmentRecordsModule
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.images.ImagesModule
import com.simprints.infra.license.LicenseModule
import com.simprints.infra.license.LicenseRepository
import com.simprints.infra.login.LoginManager
import com.simprints.infra.login.LoginManagerModule
import com.simprints.infra.login.SafetyNetModule
import com.simprints.infra.network.NetworkModule
import com.simprints.infra.realm.RealmModule
import com.simprints.infra.recent.user.activity.RecentUserActivityDataStoreModule
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.recent.user.activity.RecentUserActivityModule
import com.simprints.infra.security.SecurityManager
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton
import com.simprints.infra.security.SecurityModule as SecurityManagerModule

@Component(
    modules = [
        AppModule::class,
        DataModule::class,
        SecurityModule::class,
        PreferencesModule::class,
        SerializerModule::class,
        SyncModule::class,
        DashboardActivityModule::class,
        ViewModelModule::class,
        LoginManagerModule::class,
        NetworkModule::class,
        SafetyNetModule::class,
        SecurityManagerModule::class,
        LicenseModule::class,
        ImagesModule::class,
        RealmModule::class,
        ConfigManagerModule::class,
        DataStoreModule::class,
        EnrolmentRecordsModule::class,
        RecentUserActivityModule::class,
        RecentUserActivityDataStoreModule::class,
        EventSystemModule::class
    ]
)
@Singleton
interface AppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(app: Application): Builder

        fun appModule(appModule: AppModule): Builder
        fun dataModule(dataModule: DataModule): Builder
        fun preferencesModule(preferencesModule: PreferencesModule): Builder
        fun securityModule(securityModule: SecurityModule): Builder
        fun serializerModule(serializerModule: SerializerModule): Builder
        fun syncModule(syncModule: SyncModule): Builder
        fun dashboardActivityModule(dashboardActivityModule: DashboardActivityModule): Builder
        fun viewModelModule(viewModelModule: ViewModelModule): Builder

        fun build(): AppComponent
    }

    fun getOrchestratorComponent(): OrchestratorComponent.Builder

    fun inject(app: Application)
    fun inject(alertActivity: AlertActivity)
    fun inject(privacyNoticeActivity: PrivacyNoticeActivity)
    fun inject(loginActivity: LoginActivity)
    fun inject(checkLoginActivity: CheckLoginFromIntentActivity)
    fun inject(checkLoginActivity: CheckLoginFromMainLauncherActivity)
    fun inject(dashboardActivity: DashboardActivity)
    fun inject(checkLoginPresenter: CheckLoginPresenter)
    fun inject(checkLoginFromIntentPresenter: CheckLoginFromIntentPresenter)
    fun inject(checkLoginFromMainLauncherPresenter: CheckLoginFromMainLauncherPresenter)
    fun inject(requestLoginActivity: RequestLoginActivity)
    fun inject(projectAuthenticator: ProjectAuthenticatorImpl)
    fun inject(alertPresenter: AlertPresenter)
    fun inject(syncSchedulerHelper: SyncSchedulerImpl)
    fun inject(moduleSelectionActivity: ModuleSelectionActivity)
    fun inject(moduleSelectionActivity: ModuleSelectionFragment)
    fun inject(settingsActivity: SettingsActivity)
    fun inject(settingsPreferenceFragment: SettingsPreferenceFragment)
    fun inject(settingsAboutFragment: SettingsAboutFragment)
    fun inject(settingsAboutActivity: SettingsAboutActivity)
    fun inject(fingerSelectionActivity: FingerSelectionActivity)
    fun inject(consentActivity: ConsentActivity)
    fun inject(coreExitFormActivity: CoreExitFormActivity)
    fun inject(fingerprintExitFormActivity: FingerprintExitFormActivity)
    fun inject(faceExitFormActivity: FaceExitFormActivity)
    fun inject(fetchGuidActivity: FetchGuidActivity)
    fun inject(guidSelectionActivity: GuidSelectionActivity)
    fun inject(debugActivity: DebugActivity)
    fun inject(eventDownSyncCountWorker: EventDownSyncCountWorker)
    fun inject(eventDownSyncDownloaderWorker: EventDownSyncDownloaderWorker)
    fun inject(eventSyncMasterWorker: EventSyncMasterWorker)
    fun inject(eventUpSyncCountWorker: EventUpSyncCountWorker)
    fun inject(imageUpSyncWorker: ImageUpSyncWorker)
    fun inject(syncInformationActivity: SyncInformationActivity)
    fun inject(eventEndSyncReporterWorker: EventEndSyncReporterWorker)
    fun inject(eventStartSyncWorker: EventStartSyncReporterWorker)
    fun inject(qrCaptureActivity: QrCaptureActivity)
    fun inject(enrolLastBiometricsActivity: EnrolLastBiometricsActivity)
    fun inject(setupActivity: SetupActivity)
    fun inject(securityStateWorker: SecurityStateWorker)
    fun inject(eventUpSyncUploaderWorker: EventUpSyncUploaderWorker)
    fun inject(preferencesManager: IdPreferencesManager)
    fun inject(remoteConfigWorker: RemoteConfigWorker)
    fun inject(storeUserLocationIntoCurrentSessionWorker: StoreUserLocationIntoCurrentSessionWorker)

    fun getSessionEventsManager(): EventRepository
    fun getTimeHelper(): TimeHelper
    fun getEnrolmentRecordManager(): EnrolmentRecordManager
    fun getPreferencesManager(): PreferencesManager
    fun getIdPreferencesManager(): IdPreferencesManager
    fun getImprovedSharedPreferences(): ImprovedSharedPreferences
    fun getImageRepository(): ImageRepository
    fun getLicenseRepository(): LicenseRepository
    fun getLoginManager(): LoginManager
    fun getConfigManager(): ConfigManager
    fun getSecurityManager(): SecurityManager
    fun getRecentUserActivityManager(): RecentUserActivityManager
}
