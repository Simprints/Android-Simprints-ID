package com.simprints.id.di

import com.simprints.id.Application
import com.simprints.id.activities.PrivacyActivity
import com.simprints.id.activities.RefusalActivity
import com.simprints.id.activities.SettingsActivity
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
import com.simprints.id.activities.dashboard.DashboardActivity
import com.simprints.id.activities.dashboard.DashboardCardsFactory
import com.simprints.id.activities.dashboard.DashboardPresenter
import com.simprints.id.activities.dashboard.models.DashboardSyncCard
import com.simprints.id.activities.launch.LaunchActivity
import com.simprints.id.activities.login.LoginActivity
import com.simprints.id.activities.login.LoginPresenter
import com.simprints.id.activities.collectFingerprints.CollectFingerprintsActivity
import com.simprints.id.activities.collectFingerprints.CollectFingerprintsPresenter
import com.simprints.id.activities.collectFingerprints.fingers.CollectFingerprintsFingerDisplayHelper
import com.simprints.id.activities.collectFingerprints.scanning.CollectFingerprintsScanningHelper
import com.simprints.id.activities.collectFingerprints.sync.CollectFingerprintsSyncHelper
import com.simprints.id.activities.matching.MatchingActivity
import com.simprints.id.activities.matching.MatchingPresenter
import com.simprints.id.activities.requestLogin.RequestLoginActivity
import com.simprints.id.secure.ProjectAuthenticator
import com.simprints.id.services.GuidSelectionService
import com.simprints.id.services.sync.SyncService
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(AppModule::class, PreferencesModule::class, SerializerModule::class, AndroidInjectionModule::class))
interface AppComponent {
    fun inject(app: Application)
    fun inject(launchActivity: LaunchActivity)
    fun inject(guidSelectionService: GuidSelectionService)
    fun inject(collectFingerprintsActivity: CollectFingerprintsActivity)
    fun inject(alertActivity: AlertActivity)
    fun inject(aboutActivity: AboutActivity)
    fun inject(refusalActivity: RefusalActivity)
    fun inject(privacyActivity: PrivacyActivity)
    fun inject(tutorialActivity: TutorialActivity)
    fun inject(settingsActivity: SettingsActivity)
    fun inject(matchingActivity: MatchingActivity)
    fun inject(loginActivity: LoginActivity)
    fun inject(checkLoginActivity: CheckLoginFromIntentActivity)
    fun inject(checkLoginActivity: CheckLoginFromMainLauncherActivity)
    fun inject(dashboardActivity: DashboardActivity)
    fun inject(checkLoginPresenter: CheckLoginPresenter)
    fun inject(checkLoginFromIntentPresenter: CheckLoginFromIntentPresenter)
    fun inject(checkLoginFromMainLauncherPresenter: CheckLoginFromMainLauncherPresenter)
    fun inject(syncService: SyncService)
    fun inject(matchingPresenter: MatchingPresenter)
    fun inject(aboutPresenter: AboutPresenter)
    fun inject(dashboardCardsFactory: DashboardCardsFactory)
    fun inject(dashboardSyncCard: DashboardSyncCard)
    fun inject(loginPresenter: LoginPresenter)
    fun inject(collectFingerprintsPresenter: CollectFingerprintsPresenter)
    fun inject(collectFingerprintsSyncHelper: CollectFingerprintsSyncHelper)
    fun inject(collectFingerprintsScanningHelper: CollectFingerprintsScanningHelper)
    fun inject(collectFingerprintsFingerDisplayHelper: CollectFingerprintsFingerDisplayHelper)
    fun inject(requestLoginActivity: RequestLoginActivity)
    fun inject(projectAuthenticator: ProjectAuthenticator)
    fun inject(dashboardPresenter: DashboardPresenter)
    fun inject(alertPresenter: AlertPresenter)
}
