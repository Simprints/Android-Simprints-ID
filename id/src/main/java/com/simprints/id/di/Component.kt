package com.simprints.id.di

import com.simprints.id.Application
import com.simprints.id.activities.SyncSchedulerHelper
import com.simprints.id.activities.TutorialActivity
import com.simprints.id.activities.about.AboutActivity
import com.simprints.id.activities.about.AboutPresenter
import com.simprints.id.activities.alert.AlertActivity
import com.simprints.id.activities.alert.AlertPresenter
import com.simprints.id.activities.checkLogin.CheckLoginPresenter
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentPresenter
import com.simprints.id.activities.checkLogin.openedByMainLauncher.CheckLoginFromMainLauncherActivity
import com.simprints.id.activities.checkLogin.openedByMainLauncher.CheckLoginFromMainLauncherPresenter
import com.simprints.id.activities.collectFingerprints.CollectFingerprintsPresenter
import com.simprints.id.activities.collectFingerprints.fingers.CollectFingerprintsFingerDisplayHelper
import com.simprints.id.activities.collectFingerprints.scanning.CollectFingerprintsScanningHelper
import com.simprints.id.activities.dashboard.DashboardActivity
import com.simprints.id.activities.dashboard.DashboardCardsFactory
import com.simprints.id.activities.dashboard.DashboardPresenter
import com.simprints.id.activities.dashboard.models.DashboardSyncCardViewModel
import com.simprints.id.activities.dashboard.views.DashboardSyncCardView
import com.simprints.id.activities.launch.LaunchPresenter
import com.simprints.id.activities.login.LoginActivity
import com.simprints.id.activities.login.LoginPresenter
import com.simprints.id.activities.longConsent.LongConsentActivity
import com.simprints.id.activities.longConsent.LongConsentPresenter
import com.simprints.id.activities.matching.MatchingActivity
import com.simprints.id.activities.matching.MatchingPresenter
import com.simprints.id.activities.refusal.RefusalPresenter
import com.simprints.id.activities.requestLogin.RequestLoginActivity
import com.simprints.id.activities.settings.fragments.settingsPreference.SettingsPreferencePresenter
import com.simprints.id.scanner.ScannerManager
import com.simprints.id.secure.ProjectAuthenticator
import com.simprints.id.services.GuidSelectionService
import com.simprints.id.services.scheduledSync.peopleDownSync.PeopleDownSyncWorker
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.tasks.CountTask
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.tasks.DownSyncTask
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.workers.CountWorker
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.workers.DownSyncWorker
import com.simprints.id.services.scheduledSync.peopleDownSync.peopleCount.SyncCountWorker
import com.simprints.id.services.scheduledSync.peopleUpsync.periodicFlusher.PeopleUpSyncPeriodicFlusherWorker
import com.simprints.id.services.scheduledSync.peopleUpsync.uploader.PeopleUpSyncUploaderWorker
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsMasterWorker
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsUploaderWorker
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [(AppModule::class), (PreferencesModule::class), (SerializerModule::class), (AndroidInjectionModule::class)])
interface AppComponent {
    fun inject(app: Application)
    fun inject(guidSelectionService: GuidSelectionService)
    fun inject(alertActivity: AlertActivity)
    fun inject(aboutActivity: AboutActivity)
    fun inject(longConsentActivity: LongConsentActivity)
    fun inject(refusalPresenter: RefusalPresenter)
    fun inject(tutorialActivity: TutorialActivity)
    fun inject(matchingActivity: MatchingActivity)
    fun inject(loginActivity: LoginActivity)
    fun inject(checkLoginActivity: CheckLoginFromIntentActivity)
    fun inject(checkLoginActivity: CheckLoginFromMainLauncherActivity)
    fun inject(dashboardActivity: DashboardActivity)
    fun inject(checkLoginPresenter: CheckLoginPresenter)
    fun inject(checkLoginFromIntentPresenter: CheckLoginFromIntentPresenter)
    fun inject(checkLoginFromMainLauncherPresenter: CheckLoginFromMainLauncherPresenter)
    fun inject(matchingPresenter: MatchingPresenter)
    fun inject(aboutPresenter: AboutPresenter)
    fun inject(dashboardCardsFactory: DashboardCardsFactory)
    fun inject(dashboardSyncCardViewModel: DashboardSyncCardViewModel)
    fun inject(loginPresenter: LoginPresenter)
    fun inject(collectFingerprintsPresenter: CollectFingerprintsPresenter)
    fun inject(collectFingerprintsScanningHelper: CollectFingerprintsScanningHelper)
    fun inject(collectFingerprintsFingerDisplayHelper: CollectFingerprintsFingerDisplayHelper)
    fun inject(requestLoginActivity: RequestLoginActivity)
    fun inject(projectAuthenticator: ProjectAuthenticator)
    fun inject(dashboardPresenter: DashboardPresenter)
    fun inject(alertPresenter: AlertPresenter)
    fun inject(launchPresenter: LaunchPresenter)
    fun inject(peopleUpSyncUploaderWorker: PeopleUpSyncUploaderWorker)
    fun inject(peopleUpSyncPeriodicFlusherWorker: PeopleUpSyncPeriodicFlusherWorker)
    fun inject(settingsPreferencePresenter: SettingsPreferencePresenter)
    fun inject(longConsentPresenter: LongConsentPresenter)
    fun inject(scannerManager: ScannerManager)
    fun inject(syncSchedulerHelper: SyncSchedulerHelper)
    fun inject(peopleDownSyncWorker: PeopleDownSyncWorker)
    fun inject(dashboardSyncCardView: DashboardSyncCardView)
    fun inject(sessionsSyncMasterWorker: SessionEventsMasterWorker)
    fun inject(sessionSyncUploaderWorker: SessionEventsUploaderWorker)
    fun inject(syncCountWorker: SyncCountWorker)
    fun inject(countTask: CountTask)
    fun inject(downSyncTask: DownSyncTask)
    fun inject(countWorker: CountWorker)
    fun inject(downSyncWorker: DownSyncWorker)
}
