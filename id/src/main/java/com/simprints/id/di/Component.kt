package com.simprints.id.di

import com.simprints.id.Application
import com.simprints.id.activities.*
import com.simprints.id.activities.about.AboutActivity
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.activities.checkLogin.openedByMainLauncher.CheckLoginFromMainLauncherActivity
import com.simprints.id.activities.dashboard.DashboardActivity
import com.simprints.id.activities.launch.LaunchActivity
import com.simprints.id.activities.login.LoginActivity
import com.simprints.id.activities.main.MainActivity
import com.simprints.id.activities.matching.MatchingActivity
import com.simprints.id.activities.requestLogin.RequestLoginActivity
import com.simprints.id.services.GuidSelectionService
import com.simprints.id.services.sync.SyncService
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

/**
 * Created by fabiotuzza on 16/01/2018.
 */
@Singleton
@Component(modules = arrayOf(AppModule::class, PreferencesModule::class, SerializerModule::class, AndroidInjectionModule::class))
interface AppComponent {
    fun inject(app: Application)
    fun inject(app: SyncService) 
    fun inject(launchActivity: LaunchActivity) 
    fun inject(guidSelectionService: GuidSelectionService) 
    fun inject(mainActivity: MainActivity)
    fun inject(alertActivity: AlertActivity) 
    fun inject(aboutActivity: AboutActivity) 
    fun inject(refusalActivity: RefusalActivity) 
    fun inject(privacyActivity: PrivacyActivity) 
    fun inject(tutorialActivity: TutorialActivity) 
    fun inject(settingsActivity: SettingsActivity) 
    fun inject(matchingActivity: MatchingActivity) 
    fun inject(loginActivity: LoginActivity) 
    fun inject(requestLoginActivity: RequestLoginActivity)
    fun inject(checkLoginActivity: CheckLoginFromIntentActivity)
    fun inject(checkLoginActivity: CheckLoginFromMainLauncherActivity)
    fun inject(dashboardActivity: DashboardActivity) 

}
